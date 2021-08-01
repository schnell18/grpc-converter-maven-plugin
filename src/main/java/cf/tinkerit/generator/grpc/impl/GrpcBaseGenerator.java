package cf.tinkerit.generator.grpc.impl;

import cf.tinkerit.generator.grpc.AbstractSubGenerator;
import cf.tinkerit.generator.grpc.AppDefinedTypeRegistry;
import cf.tinkerit.generator.grpc.Pair;
import cf.tinkerit.generator.grpc.impl.grpc.model.*;
import cf.tinkerit.generator.grpc.impl.utils.CommonUtil;
import cf.tinkerit.generator.grpc.logging.Logger;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.groupingBy;

abstract public class GrpcBaseGenerator extends AbstractSubGenerator {

    private static final String JAVA_HASH_SET = "java.util.HashSet";
    private static final String JAVA_ARRAY_LIST = "java.util.ArrayList";
    private static final String JAVA_LIST = "java.util.List";
    private static final String GOOGLE_PROTOBUF_TIMESTAMP_STATIC = "com.google.protobuf.util.Timestamps.fromMillis";
    private static final String GOOGLE_PROTOBUF_TIMESTAMP = "com.google.protobuf.Timestamp";
    private static final String GOOGLE_PROTOBUF_TIMESTAMP_IDL = "google.protobuf.Timestamp";

    private static final Map<String, String> builtIns = new HashMap<>();
    static {
        builtIns.put("long", "int64");
        builtIns.put("int", "int32");
        builtIns.put("boolean", "bool");
        builtIns.put("float", "float");
        builtIns.put("double", "double");
        builtIns.put("Long", "int64");
        builtIns.put("Integer", "int32");
        builtIns.put("Boolean", "bool");
        builtIns.put("Float", "float");
        builtIns.put("Double", "double");
        builtIns.put("String", "string");
        builtIns.put("java.util.Date", "google.protobuf.Timestamp");
        builtIns.put("java.sql.Date", "google.protobuf.Timestamp");
    }

    private String currentModule;
    private String sourceRootDir;
    private static final Pattern javaPattern = Pattern.compile(".*\\.java$");
    private static final Type NIL = Type.Nil();

    private final AppDefinedTypeRegistry registry = new AppDefinedTypeRegistry(200);
    private final Map<String, Boolean> importGoogleTimestampFor = new HashMap<>();
    private final Map<String, Set<String>> convertImportsFor = new HashMap<>();
    private final Map<String, Set<String>> convertStaticImportsFor = new HashMap<>();

    public abstract JavaClassCategory getMainCategory();
    public abstract JavaClassCategory getModelCategory();

    /**
     * By default, root package is the first three packages connected by dot.
     * Override this method to customize the root package for non-standard project.
     */
    public String getRootPackage() {
        return "";
    }

    public String getPackageInfix() {
        return "";
    }

    public boolean isGenerateProtobufIDL() {
        return true;
    }

    public boolean isGenerateConverters() {
        return true;
    }

    public AppDefinedTypeRegistry getRegistry() {
        return registry;
    }

    protected Map<String, List<Pair>> walk(Function<Path, JavaClassCategory> classifier) {
        Map<String, List<Pair>> tplData = new HashMap<>();
        if (sourceRootDir == null || "".equalsIgnoreCase(sourceRootDir)) {
            return tplData;
        }
        Path root = Paths.get(sourceRootDir);
        if (!Files.isDirectory(root)) {
            return tplData;
        }
        Logger logger = getLogger();
        try  {
            Map<JavaClassCategory, List<Path>> map = Files.walk(root)
                .filter(Files::isRegularFile)
                .filter(p -> javaPattern.matcher(p.toString()).matches())
                .collect(groupingBy(classifier));

            List<Path> interfacePaths = map.get(getMainCategory());
            if (interfacePaths == null || interfacePaths.isEmpty()) {
                return tplData;
            }

            map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    e.getValue().forEach(path -> {
                        logger.debug(String.format("Parsing java files using ANTLR on %s", path));
                        String cls = CommonUtil.toClassName(
                            CommonUtil.forwardSlashPath(path.subpath(root.getNameCount(), path.getNameCount()))
                        );
                        Object parsedModel = e.getKey().getParser().apply(path, cls);
                        if (parsedModel != null) {
                            // some java file is invalid, so no model is generate
                            // we ignore such model here
                            registry.registerType(e.getKey(), cls, parsedModel);
                        }
                    });
                });

            logger.info("Prepare interface and referenced type model ...");
            // harvest interface now
            // group interfaces by module as indicated by the right-most sub package
            Map<String, Object> subRegistry = registry.getSubRegistry(getMainCategory());
            if (subRegistry == null) {
                return tplData;
            }

            String rootPackage = null;
            String appName = null;
            Map<String, List<Interface>> interfacesByGroup = new HashMap<>();
            for (String qName : subRegistry.keySet()) {
                Interface intf = (Interface) subRegistry.get(qName);
               // categorize interface by module
                List<Interface> list =
                    interfacesByGroup.computeIfAbsent(destructName(qName)[1], k -> new ArrayList<>());
                list.add(intf);
                if (rootPackage == null || appName == null) {
                    String[] comps = destructName(qName);
                    rootPackage = comps[2];
                    appName = comps[0];
                }
            }

            List<Pair> pbIdlPairs = new ArrayList<>(100);
            List<Pair> javaPairs = new ArrayList<>(100);
            for (String module : interfacesByGroup.keySet()) {
                this.currentModule = module;
                Map<String, Object> objectModel = new HashMap<>();

                // setup packages
                if (StringUtils.isNoneBlank(this.getRootPackage())) rootPackage = getRootPackage();
                String protobufPackage = null;
                String javaPackage = null;
                if (StringUtils.isNoneBlank(this.getPackageInfix())) {
                    protobufPackage = String.format("%s.%s.%s", appName, getPackageInfix(), module);
                    javaPackage = String.format("%s.%s.%s", rootPackage, getPackageInfix(), module);
                }
                else {
                    protobufPackage = String.format("%s.%s", appName, module);
                    javaPackage = String.format("%s.%s", rootPackage, module);
                }

                objectModel.put("outerClassName", StringUtils.capitalize(module) + "Proto" );
                objectModel.put("protobufPackage", protobufPackage );
                objectModel.put("javaPackage",  javaPackage);

                // setup services
                objectModel.put("services", prepareService(interfacesByGroup.get(module)));

                // setup messages
                Set<Type> types = prepareMessage(interfacesByGroup.get(module));
                objectModel.put("messages", types);
                objectModel.put("importGoogleTimestamp", importGoogleTimestampFor.getOrDefault(module, Boolean.FALSE));
                Pair protoPair = new Pair(objectModel, module + ".proto");
                pbIdlPairs.add(protoPair);

                if (isGenerateConverters()) {
                    // setup imports for converters
                    addImportsForGrpcModels(javaPackage, types);
                    objectModel.put("converterImports", convertImportsFor.get(module));
                    objectModel.put("converterStaticImports", convertStaticImportsFor.get(module));
                    Pair javaPair = new Pair(objectModel, genConverterJavaPath(javaPackage));
                    javaPairs.add(javaPair);
                }
            }
            this.currentModule = null;

            if (isGenerateProtobufIDL()) {
                getLogger().info("Generating protobuf IDL ...");
                tplData.put("proto", pbIdlPairs);
            }
            if (isGenerateConverters()) {
                getLogger().info("Generating model converters ...");
                tplData.put("java", javaPairs);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(
                String.format("Fail to read %s", sourceRootDir),
                e
            );
        }
        return tplData;
    }

    private void addImportsForGrpcModels(String javaPackage, Set<Type> types) {
        Set<String> imports = convertImportsFor.computeIfAbsent(currentModule, k -> new TreeSet<>());
        for (Type type: types) {
            if (!type.isVirtualType()) {
                imports.add(String.format("%s.%s", javaPackage, type.getName()));
            }
            else if (type.isPluralType()) {
                //imports.add(JAVA_ARRAY_LIST);
                //imports.add(JAVA_LIST);
            }
            else if (type.isPluralType() && type.getName().contains("Set<")) {
                //imports.add(JAVA_HASH_SET);
            }
        }
    }
    private String genConverterJavaPath(String javaPackage) {
        return CommonUtil.concat(
            CommonUtil.concat(javaPackage.split("\\.")),
            "converter",
            "Converters.java"
        );
    }

    private Set<Type> prepareMessage(List<Interface> interfaces) {
        // reduce types referenced in the interfaces
        // convert java type to protobuf type
        Set<Type> referredTypes = new HashSet<>(1000);
        for (Interface intf : interfaces) {
            for (Method method : intf.getMethods()) {
                Type returnType = method.getReturns();
                doResolveType(returnType, referredTypes);
                if (returnType.isComplexType()) {
                    referredTypes.add(returnType);
                }
                else {
                    // create a wrapper complex type for primitive type
                    if ("Nil".equalsIgnoreCase(returnType.getName())
                        || "Void".equalsIgnoreCase(returnType.getName())) {
                        referredTypes.add(NIL);
                    }
                    else {
                        Type respType = Type.wrapPrimitiveType("Resp", returnType.getName());
                        doResolveFields(respType.getFields(), referredTypes);
                        method.setReturns(respType);
                        referredTypes.add(respType);
                    }
                }
                if (method.getParameters() == null || method.getParameters().isEmpty()) {
                    // create a nil type
                    method.setUnifiedParameter(NIL);
                    referredTypes.add(NIL);
                }
                else {
                    Parameter first = method.getParameters().get(0);
                    if (method.getParameters().size() > 1) {
                        Type compoundType = mkCompoundParamType(intf, method, referredTypes);
                        method.setUnifiedParameter(compoundType);
                        referredTypes.add(compoundType);
                    }
                    else if (!first.getParamType().isComplexType()) {
                        Type reqType = Type.wrapPrimitiveType("Req", first.getParamType().getName());
                        doResolveFields(reqType.getFields(), referredTypes);
                        method.setUnifiedParameter(reqType);
                        referredTypes.add(reqType);
                    }
                    else {
                        doResolveType(first.getParamType(), referredTypes);
                        method.setUnifiedParameter(first.getParamType());
                    }
                }
            }
        }
        return referredTypes;
    }

    private Type mkCompoundParamType(Interface intf, Method method, Set<Type> referredTypes) {
        Type compoundType = new Type();
        compoundType.setVirtualType(true);
        compoundType.setName(
            String.format(
                "%s%sReq",
                intf.getClassName(),
                StringUtils.capitalize(method.getName())
            )
        );
        compoundType.setQualifiedName(compoundType.getName());
        compoundType.setComplexType(true);
        List<Field> fields = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.getParamType().isComplexType()) {
                doResolveType(parameter.getParamType(), referredTypes);
                referredTypes.add(parameter.getParamType());
            }
            Field field = new Field();
            field.setRichType(parameter.getParamType());
            field.setName(parameter.getParaName());
            field.setType(basename(field.getRichType().getQualifiedName()));
            fields.add(field);
        }
        doResolveFields(fields, referredTypes);
        compoundType.setFields(fields);
        return compoundType;
    }

    private List<Interface> prepareService(List<Interface> interfaces) {
        return interfaces;
    }

    private void doResolveType(Type type, Set<Type> referredTypes) {
        // deal with return type
        if (type.isComplexType()) {
            Type resolvedType = this.registry.resolveType(type.getQualifiedName());
            if (resolvedType != null) {
                List<Field> fieldList = new ArrayList<>(100);
                fieldList.addAll(resolvedType.getFields());
                // Add parent's fields, only support one level
                if (resolvedType.getParent() != null) {
                    Type resolvedParentType = this.registry.resolveType(resolvedType.getParent().getQualifiedName());
                    if (resolvedParentType != null) {
                        // parent type is shared, make a clone so that we can
                        // sort fields in one type w/o affecting other type
                        fieldList.addAll(cloneFields(resolvedParentType.getFields()));
                    }
                }
                type.setFields(fieldList);
                referredTypes.add(type);
                doResolveFields(type.getFields(), referredTypes);
            }
        }
    }

    private List<Field> cloneFields(List<Field> fields) {
        List<Field> clones = new ArrayList<>();
        if (fields != null && !fields.isEmpty()) {
            for (Field field: fields) {
                Field clone = new Field();
                clone.setName(field.getName());
                clone.setType(field.getType());
                clone.setRichType(field.getRichType());
                clone.setSequenceNo(field.getSequenceNo());
                clone.setProtobufType(field.getProtobufType());
                clone.setComment(field.getComment());
                clone.setDescription(field.getDescription());
                clones.add(clone);
            }
        }
        return clones;
    }

    public void doResolveFields(List<Field> fields, Set<Type> referredTypes) {
        if (fields != null && !fields.isEmpty()) {
            for (int i = 1; i <= fields.size(); i++) {
                Field field = fields.get(i - 1);
                field.setSequenceNo(i);

                if (field.getRichType().isComplexType() && !referredTypes.contains(field.getRichType())) {
                    doResolveType(field.getRichType(), referredTypes);
                }
                if (field.getRichType().isPluralType()) {
                    field.setSingularName(CommonUtil.singularize(field.getName()));
                }
                field.setProtobufType(toProtobufType(field.getRichType()));
            }
        }
    }

    private String toProtobufType(Type type) {
        // discard common rpc result wrapper types such as CallResult<T>, Result<T>

        // complex type will be defined in the generated IDL
        // so we use the type name as-is
        if (type.isComplexType()) {
            return type.getName();
        }

        // map array and list to repeated type


        // map Java primitive types to protobuf
        // map java Date (java.util.Date, java.sql.Date) to google.protobuf.Timestamp
        String protobufType = builtIns.get(type.getQualifiedName());
        if (GOOGLE_PROTOBUF_TIMESTAMP_IDL.equals(protobufType)) {
            // instruct converter to import "google.protobuf.Timestamp"
            if (this.currentModule != null) {
                this.importGoogleTimestampFor.put(currentModule, Boolean.TRUE);
                Set<String> staticImports =
                        convertStaticImportsFor.computeIfAbsent(currentModule, k -> new TreeSet<>());
                staticImports.add(GOOGLE_PROTOBUF_TIMESTAMP_STATIC);
                Set<String> imports = convertImportsFor.computeIfAbsent(currentModule, k -> new TreeSet<>());
                imports.add(GOOGLE_PROTOBUF_TIMESTAMP);
                imports.add(type.getQualifiedName());
            }

        }
        if (protobufType != null) return protobufType;
        return "TODO";
    }

    private String basename(String qName) {
        String[] comps = qName.split("\\.");
        if (comps.length >= 1) {
            return comps[comps.length - 1];
        }
        return qName;
    }

    private String[] destructName(String qName) {
        if (StringUtils.isBlank(qName)) {
            return new String[] {"unknown", "unknown", "unknown"};
        }
        String[] comps = qName.split("\\.");
        if (comps.length >= 4) {
            return new String[] {
                comps[2],
                comps[comps.length - 2],
                String.format("%s.%s.%s", comps[0], comps[1], comps[2])
            };
        }
        else {
            return new String[] {"unknown", "unknown", "unknown"};
        }
    }

    public String getSourceRootDir() {
        return sourceRootDir;
    }

    public void setSourceRootDir(String sourceRootDir) {
        this.sourceRootDir = sourceRootDir;
    }

}
