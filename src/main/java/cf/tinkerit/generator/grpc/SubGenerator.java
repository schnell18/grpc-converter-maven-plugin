package cf.tinkerit.generator.grpc;

public interface SubGenerator extends Generator {
    String getModuleName();
    CompositeGenerator getParentGenerator();
    void setParentGenerator(CompositeGenerator parentGenerator);
}
