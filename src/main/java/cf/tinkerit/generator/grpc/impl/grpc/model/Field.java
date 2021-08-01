package cf.tinkerit.generator.grpc.impl.grpc.model;

import cf.tinkerit.generator.grpc.impl.model.Base;

public class Field extends Base {
    private String type;
    private String protobufType;
    private int sequenceNo = 1;
    private Type richType;
    private String singularName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSingularName() {
        return singularName;
    }

    public void setSingularName(String singularName) {
        this.singularName = singularName;
    }

    public Type getRichType() {
        return richType;
    }

    public void setRichType(Type richType) {
        this.richType = richType;
    }

    public String getProtobufType() {
        return protobufType;
    }

    public void setProtobufType(String protobufType) {
        this.protobufType = protobufType;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    @Override
    public String toString() {
        return "Field{" +
            "type='" + type + '\'' +
            ", protobufType='" + protobufType + '\'' +
            ", sequenceNo=" + sequenceNo +
            ", richType=" + richType +
            ", singularName='" + singularName + '\'' +
            "} " + super.toString();
    }
}
