package cf.tinkerit.generator.grpc.impl.grpc.model;

public class Parameter {
    private String paramName;
    private Type paramType ;

    public String getParamName() {
        return paramName;
    }

    public Type getParamType() {
        return paramType;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
    }

    public String getParaName() {
        return paramName;
    }

    public void setParamName(String name) {
        this.paramName = name;
    }

    @Override
    public String toString() {
        return "Parameter{" +
            "paramName='" + paramName + '\'' +
            ", paramType=" + paramType +
            "} " + super.toString();
    }
}
