package cf.tinkerit.generator.grpc.impl.model;

import java.io.Serializable;

public class Base implements Serializable {
    private String name;
    private String comment;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRefName() {
        if (name.contains("_")) {
            return name.replaceAll("_", "-");
        }
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Base{" +
            "name='" + name + '\'' +
            ", comment='" + comment + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
