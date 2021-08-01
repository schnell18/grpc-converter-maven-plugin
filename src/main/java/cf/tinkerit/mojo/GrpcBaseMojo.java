package cf.tinkerit.mojo;

import cf.tinkerit.generator.grpc.impl.grpc.DubboToGrpcCodeGenerator;
import cf.tinkerit.generator.grpc.impl.utils.CommonUtil;
import cf.tinkerit.generator.grpc.logging.Logger;
import cf.tinkerit.generator.grpc.impl.grpc.ParentGrpcCodeGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 * Dubbo interface to gRPC IDL converter.
 *
 * @author Zhang Feng
 * @version v1.0 2021/6/4
 **/
public abstract class GrpcBaseMojo extends AbstractMojo {

    /**
     * The project of grpc service definition
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter
    private String dubboPattern;

    @Parameter
    private String rootPackage;

    @Parameter
    private String packageInfix;

    /**
     * Build context that tracks changes to the source and target files.
     *
     * @since 0.3.0
     */
    @Component
    protected BuildContext buildContext;


    protected abstract String getOutputDirectory();
    protected abstract boolean generateProtobufIDL();
    protected abstract boolean generateConverters();

    @Override
    public void execute() {
        // make sure the awt window does not show up
        System.setProperty("java.awt.headless", "true");

        ParentGrpcCodeGenerator parentGenerator = new ParentGrpcCodeGenerator();
        parentGenerator.setLogger(new MavenLogger(getLog()));
        parentGenerator.setTargetDirectory(getOutputDirectory());

        getLog().info("Running gRPC conversion tool ...");
        // locate grpc service interface
        for (Object obj: project.getCompileSourceRoots()) {
            DubboToGrpcCodeGenerator dubboGenerator = new DubboToGrpcCodeGenerator();
            dubboGenerator.setSourceRootDir(obj.toString());
            dubboGenerator.setDubboPattern(dubboPattern);
            dubboGenerator.setRootPackage(rootPackage);
            dubboGenerator.setPackageInfix(packageInfix);
            dubboGenerator.setGenerateConverters(generateConverters());
            dubboGenerator.setGenerateProtobufIDL(generateProtobufIDL());
            dubboGenerator.setParentGenerator(parentGenerator);
            dubboGenerator.generate();
        }
        if (generateConverters()) {
            String generatedJavaSourceDir = CommonUtil.concat(getOutputDirectory(), "java");
            project.addCompileSourceRoot(generatedJavaSourceDir);
            buildContext.refresh(new File(generatedJavaSourceDir));
        }

    }

    public static class MavenLogger implements Logger {
        private Log log;

        public MavenLogger(Log log) {
            this.log = log;
        }

        @Override
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        @Override
        public void debug(CharSequence var1) {
            log.debug(var1);
        }

        @Override
        public void debug(CharSequence var1, Throwable var2) {
            log.debug(var1, var2);
        }

        @Override
        public void debug(Throwable var1) {
            log.debug(var1);
        }

        @Override
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        @Override
        public void info(CharSequence var1) {
            log.info(var1);
        }

        @Override
        public void info(CharSequence var1, Throwable var2) {
            log.info(var1, var2);
        }

        @Override
        public void info(Throwable var1) {
            log.info(var1);
        }

        @Override
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        @Override
        public void warn(CharSequence var1) {
            log.warn(var1);
        }

        @Override
        public void warn(CharSequence var1, Throwable var2) {
            log.warn(var1, var2);
        }

        @Override
        public void warn(Throwable var1) {
            log.warn(var1);
        }

        @Override
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        @Override
        public void error(CharSequence var1) {
            log.error(var1);
        }

        @Override
        public void error(CharSequence var1, Throwable var2) {
            log.error(var1, var2);
        }

        @Override
        public void error(Throwable var1) {
            log.error(var1);
        }
    }
}
