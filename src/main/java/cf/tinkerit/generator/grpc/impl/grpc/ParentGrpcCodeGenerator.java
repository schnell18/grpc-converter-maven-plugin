package cf.tinkerit.generator.grpc.impl.grpc;

import cf.tinkerit.generator.grpc.AbstractCompositeGenerator;
import cf.tinkerit.generator.grpc.logging.Logger;

public class ParentGrpcCodeGenerator extends AbstractCompositeGenerator {

    private Logger logger;

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
