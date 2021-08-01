package cf.tinkerit.generator.grpc;

import cf.tinkerit.generator.grpc.logging.Logger;

abstract public class AbstractSubGenerator extends AbstractGenerator implements SubGenerator {

    private CompositeGenerator parentGenerator;

    @Override
    public CompositeGenerator getParentGenerator() {
        return parentGenerator;
    }

    @Override
    public void setParentGenerator(CompositeGenerator parentGenerator) {
        this.parentGenerator = parentGenerator;
    }

    @Override
    public String getTargetDirectory() {
        return this.getParentGenerator().getTargetDirectory();
    }

    @Override
    public boolean isTopLevel() {
        return false;
    }

    @Override
    public Logger getLogger() {
        return parentGenerator.getLogger();
    }
}
