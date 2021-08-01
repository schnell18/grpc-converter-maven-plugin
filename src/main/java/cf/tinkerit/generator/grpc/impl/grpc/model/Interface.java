package cf.tinkerit.generator.grpc.impl.grpc.model;

import java.io.Serializable;
import java.util.List;

public class Interface implements Serializable {
    private String className;
    private String qualifiedClassName;
    private List<Method> methods;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public void setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "Interface{" +
                "className='" + className + '\'' +
                ", qualifiedClassName='" + qualifiedClassName + '\'' +
                ", methods=" + methods +
                '}';
    }
}
