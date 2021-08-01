package cf.tinkerit.generator.grpc.impl;

import cf.tinkerit.generator.antlr.JavaParser;
import cf.tinkerit.generator.antlr.JavaParserBaseListener;
import cf.tinkerit.generator.grpc.AppDefinedTypeRegistry;
import cf.tinkerit.generator.grpc.impl.grpc.model.Type;
import org.antlr.v4.runtime.BufferedTokenStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaInterfaceListener extends JavaParserBaseListener {
    private AppDefinedTypeRegistry registry;
    private static final Map<String, Integer> simpleBuiltInTypes = new HashMap<>();
    static {
        simpleBuiltInTypes.put("long", 1);
        simpleBuiltInTypes.put("int", 1);
        simpleBuiltInTypes.put("boolean", 1);
        simpleBuiltInTypes.put("float", 1);
        simpleBuiltInTypes.put("double", 1);
        simpleBuiltInTypes.put("Long", 1);
        simpleBuiltInTypes.put("Integer", 1);
        simpleBuiltInTypes.put("Boolean", 1);
        simpleBuiltInTypes.put("Float", 1);
        simpleBuiltInTypes.put("Double", 1);
        simpleBuiltInTypes.put("String", 1);
        simpleBuiltInTypes.put("java.util.Date", 1);
        simpleBuiltInTypes.put("java.sql.Date", 1);
    }
    protected BufferedTokenStream tokens;
    protected Map<String, String> imports = new HashMap<>(50);
    protected List<String> wildcardImports = new ArrayList<>(20);
    protected String thisPackage;

    public AppDefinedTypeRegistry getRegistry() {
        return registry;
    }

    public String getThisPackage() {
        return thisPackage;
    }

    public JavaInterfaceListener(AppDefinedTypeRegistry registry, BufferedTokenStream tokens) {
        this.registry = registry;
        this.tokens = tokens;
    }

    @Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        thisPackage = ctx.qualifiedName().getText();
    }

    @Override
    public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        String importStmt = ctx.getText();
        String symbol = ctx.qualifiedName().getText();
        if (importStmt.contains("*")) {
            wildcardImports.add(symbol);
        }
        else {
            int idx = symbol.lastIndexOf(".");
            imports.put(symbol.substring(idx + 1), symbol);
        }
    }

    public String getQualifiedClassName(String baseName) {
        if (getThisPackage() == null) {
            return baseName;
        }
        return String.format("%s.%s", getThisPackage(), baseName);
    }

    protected boolean looksLikeAppDefinedType(String qualifiedType) {
        return !qualifiedType.startsWith("java.lang")
                && !simpleBuiltInTypes.containsKey(qualifiedType)
                && !qualifiedType.endsWith("Nil")
                && !qualifiedType.endsWith("Void")
                ;
    }

    protected Type resolveType(String type) {
        // TODO: refine here later check if type is array
        // TODO: refine here later check if type is List<>
        Pattern pat = Pattern.compile("(?:List|Set)\\s*<\\s*(.*)\\s*>");
        Matcher m = pat.matcher(type);
        Type richType = new Type();
        if (m.matches()) {
            type = m.group(1);
            richType.setPluralType(true);
        }
        richType.setName(type);

        if (simpleBuiltInTypes.containsKey(type)) {
            richType.setQualifiedName(type);
            richType.setComplexType(false);
            return richType;
        }

        // already declared as fully-qualified type
        if (type.contains(".")) {
            richType.setQualifiedName(type);
            richType.setComplexType(looksLikeAppDefinedType(type));
            return richType;
        }

        if (imports.containsKey(type)) {
            richType.setQualifiedName(imports.get(type));
            richType.setComplexType(looksLikeAppDefinedType(richType.getQualifiedName()));
            return richType;
        }

        // resolve unqualified reference type
        // by concatenating wild card import package
        for (String pkg : this.wildcardImports) {
            String fqCls = pkg + "." + type;
            if (simpleBuiltInTypes.containsKey(fqCls)) {
                richType.setQualifiedName(fqCls);
                richType.setComplexType(false);
                return richType;
            }
            if (registry.isTypeDefined(fqCls)) {
                richType.setQualifiedName(fqCls);
                richType.setComplexType(looksLikeAppDefinedType(richType.getQualifiedName()));
                return richType;
            }
        }

        // must be complex type as long as reaching here
        richType.setComplexType(true);
        // finally we have to assume type defined in the same package as
        // current class since the source code should at least compile
        if (thisPackage != null) {
            richType.setQualifiedName(thisPackage + "." + type);
        }

        return richType;
    }

    protected String getInnermostType(JavaParser.TypeTypeContext typeType) {
        if (typeType.primitiveType() != null) {
            return typeType.primitiveType().getText();
        }
        JavaParser.ClassOrInterfaceTypeContext cit = typeType.classOrInterfaceType();
        if (cit != null && cit.typeArguments() != null && !cit.typeArguments().isEmpty()) {
            return getInnermostType(cit.typeArguments().get(0).typeArgument(0).typeType());
        }
        else {
            return cit.IDENTIFIER().get(0).getText();
        }
    }


}
