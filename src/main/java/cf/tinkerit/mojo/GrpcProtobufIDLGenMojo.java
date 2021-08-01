package cf.tinkerit.mojo;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Dubbo interface to gRPC IDL converter.
 *
 * @author Zhang Feng
 * @version v1.0 2021/6/4
 **/
@Mojo(name = "gen-proto")
public class GrpcProtobufIDLGenMojo extends GrpcBaseMojo {

    @Parameter(
            required = true,
            property = "javaOutputDirectory",
            defaultValue = "${project.basedir}/src/main"
    )
    private String outputDirectory;

    @Override
    public String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected boolean generateProtobufIDL() {
        return true;
    }

    @Override
    protected boolean generateConverters() {
        return false;
    }

    @Override
    public void execute() {
        super.execute();
    }
}
