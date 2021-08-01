package cf.tinkerit.generator.grpc.impl.grpc;

import cf.tinkerit.generator.grpc.impl.grpc.model.Type;
import cf.tinkerit.generator.grpc.logging.Logger;
import cf.tinkerit.generator.antlr.JavaLexer;
import cf.tinkerit.generator.antlr.JavaParser;
import cf.tinkerit.generator.grpc.impl.DummyLogger;
import cf.tinkerit.generator.grpc.AbstractCompositeGenerator;
import cf.tinkerit.generator.grpc.Pair;
import cf.tinkerit.generator.grpc.impl.JavaClassCategory;
import cf.tinkerit.generator.grpc.impl.GrpcBaseGenerator;
import cf.tinkerit.generator.grpc.impl.grpc.antlr4.DomainModelListener;
import cf.tinkerit.generator.grpc.impl.grpc.antlr4.DubboInterfaceListener;
import cf.tinkerit.generator.grpc.impl.grpc.model.Interface;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DubboToGrpcCodeGenerator extends GrpcBaseGenerator {
    private final JavaClassCategory mainCategory = new JavaClassCategory(
        JavaClassCategory.Constants.CAT_DUBBO_INTERFACE,
        this::parseDubboServiceInterface
    );

    public final JavaClassCategory modelCategory = new JavaClassCategory(
        JavaClassCategory.Constants.CAT_DUBBO_MODEL,
        this::parseDomainModel
    );

    private String dubboPattern;
    private String rootPackage;
    private String packageInfix;
    private boolean generateProtobufIDL;
    private boolean generateConverters;

    public String getDubboPattern() {
        return dubboPattern;
    }

    @Override
    public String getRootPackage() {
        if (StringUtils.isNoneBlank(rootPackage)) return rootPackage;
        return super.getRootPackage();
    }

    public void setRootPackage(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    @Override
    public String getPackageInfix() {
        if (StringUtils.isNoneBlank(packageInfix)) return packageInfix;
        return super.getPackageInfix();
    }

    public void setPackageInfix(String packageInfix) {
        this.packageInfix = packageInfix;
    }

    @Override
    public boolean isGenerateProtobufIDL() {
        return generateProtobufIDL;
    }

    public void setGenerateProtobufIDL(boolean generateProtobufIDL) {
        this.generateProtobufIDL = generateProtobufIDL;
    }

    @Override
    public boolean isGenerateConverters() {
        return generateConverters;
    }

    public void setGenerateConverters(boolean generateConverters) {
        this.generateConverters = generateConverters;
    }

    @Override
    public String getModuleName() {
        return "protobuf";
    }

    @Override
    public Map<String, List<Pair>> prepareTemplateData() {
        if (dubboPattern == null) {
            dubboPattern = ".*Service\\.java$";
        }
        Pattern dubboPatternCompiled = Pattern.compile(dubboPattern);
        return super.walk(
            path -> {
                try {
                    getLogger().debug("Identifying source to convert on path: " + path);
                     if (dubboPatternCompiled.matcher(path.toString()).matches()
                        && Files.readAllLines((Path) path).stream().noneMatch(line -> line.contains("HttpApi"))) {
                         return getMainCategory();
                     }
                     else {
                         return getModelCategory();
                     }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.err.printf("Fail to read file %s ", path);
                    return JavaClassCategory.DUMMY;
                }
            }
        );
    }

    private Interface parseDubboServiceInterface(Path path, String cls) {
        try {
            InputStream is = Files.newInputStream(path);
            CharStream input = CharStreams.fromStream(is);
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            DubboInterfaceListener listener = new DubboInterfaceListener(getRegistry(), tokens);
            walker.walk(listener, tree);
            return listener.getTheInterface();
        }
        catch (IOException e) {
            getLogger().error("Fail to parse source for: " + cls, e);
        }
        return null;
    }

    private Type parseDomainModel(Path path, String cls) {
        try {
            InputStream is = Files.newInputStream(path);
            CharStream input = CharStreams.fromStream(is);
            JavaLexer lexer = new JavaLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            JavaParser parser = new JavaParser(tokens);
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            DomainModelListener listener = new DomainModelListener(getRegistry(), tokens);
            walker.walk(listener, tree);
            Type clazz = listener.getType();
            clazz.setQualifiedName(cls);
            return clazz;
        }
        catch (IOException e) {
            getLogger().error("Fail to parse source for: " + cls, e);
        }
        return null;
    }

    public void setDubboPattern(String dubboPattern) {
        this.dubboPattern = dubboPattern;
    }

    @Override
    public JavaClassCategory getMainCategory() {
        return mainCategory;
    }

    @Override
    public JavaClassCategory getModelCategory() {
        return modelCategory;
    }

    public static void main(String[] args) {
        DubboToGrpcCodeGenerator generator = new DubboToGrpcCodeGenerator();
        generator.setGenerateProtobufIDL(true);
        generator.setGenerateConverters(true);
        generator.setSourceRootDir("/Users/zhangfeng/virtualenv/backends/rhino/rhino-api/src/main/java");
        generator.setParentGenerator(new AbstractCompositeGenerator() {
            @Override
            public String getTargetDirectory() {
                return "rhino";
            }

            @Override
            public Logger getLogger() {
                return new DummyLogger();
            }
        });
        generator.generate();
    }

}
