package cf.tinkerit.generator.grpc.impl.grpc.model;

import cf.tinkerit.generator.grpc.impl.model.Base;

import java.util.List;

public class Method extends Base {
    private String signature;
    private List<String> invocationSample;
    private List<Parameter> parameters;
    private Type returns;
    private Type unifiedParameter;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returns.getName();
    }

    public Type getReturns() {
        return returns;
    }

    public void setReturns(Type returns) {
        this.returns = returns;
    }

    public List<String> getInvocationSample() {
        return invocationSample;
    }

    public void setInvocationSample(List<String> invocationSample) {
        this.invocationSample = invocationSample;
    }

    /*public String getParameterType() {
        if (parameters == null || parameters.isEmpty()) {
            return "Nil";
        }
        // TODO: always wrap parameters inside a message
        return this.getParameters().get(0).getType();
    }
     */

    public Type getUnifiedParameter() {
        return unifiedParameter;
    }

    public void setUnifiedParameter(Type unifiedParameter) {
        this.unifiedParameter = unifiedParameter;
    }

    @Override
    public String toString() {
        return "Method{" +
            "signature='" + signature + '\'' +
            ", invocationSample=" + invocationSample +
            ", parameters=" + parameters +
            ", returns=" + returns +
            ", unifiedParameter=" + unifiedParameter +
            "} " + super.toString();
    }
}
