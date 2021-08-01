package cf.tinkerit.generator.grpc.impl.grpc.model;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Type implements Serializable {
    private String name;
    private String qualifiedName;
    private boolean complexType;
    private boolean pluralType;
    private boolean virtualType;
    private Type parent;
    private List<Field> fields;

    public static Type Nil() {
        Type type = new Type();
        type.setName("Nil");
        type.setComplexType(false);
        type.setQualifiedName(type.getName());
        type.setVirtualType(true);
        return type;
    }

    public static Type wrapPrimitiveType(String suffix, String type) {
        Field field = new Field();
        field.setType(type);
        field.setName("value");
        Type fieldType = new Type();
        fieldType.setComplexType(false);
        fieldType.setName(type);
        fieldType.setQualifiedName(type);
        field.setRichType(fieldType);
        Type richType = new Type();
        richType.setComplexType(false);
        richType.setFields(Arrays.asList(field));
        richType.setName(StringUtils.capitalize(type) + suffix);
        richType.setQualifiedName(richType.getName());
        richType.setVirtualType(true);
        return richType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedType) {
        this.qualifiedName = qualifiedType;
    }

    public boolean isComplexType() {
        return complexType;
    }

    public void setComplexType(boolean complexType) {
        this.complexType = complexType;
    }

    public boolean isPluralType() {
        return pluralType;
    }

    public void setPluralType(boolean pluralType) {
        this.pluralType = pluralType;
    }

    public boolean isVirtualType() {
        return virtualType;
    }

    public void setVirtualType(boolean virtualType) {
        this.virtualType = virtualType;
    }

    public Type getParent() {
        return parent;
    }

    public void setParent(Type parent) {
        this.parent = parent;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;

        return name.equals(type.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Type{" +
                "name='" + name + '\'' +
                ", qualifiedName='" + qualifiedName + '\'' +
                ", complexType=" + complexType +
                ", pluralType=" + pluralType +
                ", virtualType=" + virtualType +
                ", parent=" + parent +
                ", fields=" + fields +
                '}';
    }
}
