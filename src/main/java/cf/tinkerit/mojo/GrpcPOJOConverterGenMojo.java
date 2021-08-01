package cf.tinkerit.mojo;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Dubbo interface to gRPC IDL converter.
 *
 * @author Zhang Feng
 * @version v1.0 2021/6/4
 **/
@Mojo(name = "gen-converter", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GrpcPOJOConverterGenMojo extends GrpcBaseMojo {

    @Parameter(
            required = true,
            property = "javaOutputDirectory",
            defaultValue = "${project.build.directory}/generated-sources/protobuf/"
    )
    private String outputDirectory;

    @Override
    public String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected boolean generateProtobufIDL() {
        return false;
    }

    @Override
    protected boolean generateConverters() {
        return true;
    }

    @Override
    public void execute() {
        super.execute();
    }
}
