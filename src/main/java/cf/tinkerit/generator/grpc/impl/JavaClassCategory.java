package cf.tinkerit.generator.grpc.impl;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.BiFunction;

public class JavaClassCategory implements Serializable, Comparable<JavaClassCategory> {
    public static final JavaClassCategory DUMMY = new JavaClassCategory(0, (p, c) -> null);
    private int category;

    private BiFunction<Path, String, Object> parser;

    public int getCategory() {
        return category;
    }

    public BiFunction<Path, String, Object> getParser() {
        return parser;
    }

    public JavaClassCategory(int category, BiFunction<Path, String, Object> parser) {
        this.category = category;
        this.parser = parser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaClassCategory that = (JavaClassCategory) o;
        return category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    @Override
    public String toString() {
        return "JavaClassCategory{" +
                "parseOrder=" + category +
                ", parser=" + parser +
                '}';
    }

    @Override
    public int compareTo(JavaClassCategory o) {
        if (o == null) {
            return 1;
        }
        else {
            return Integer.compare(this.category, o.category);
        }
    }

    public interface Constants {
        int CAT_DUBBO_MODEL     = 10;
        int CAT_MAPI_MODEL      = 20;
        int CAT_MAPI_ERROR_CODE = 25;
        int CAT_DUBBO_INTERFACE = 1050;
        int CAT_MAPI_INTERFACE  = 1100;
    }
}
