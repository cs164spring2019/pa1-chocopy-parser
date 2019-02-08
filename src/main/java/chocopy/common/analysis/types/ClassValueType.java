package chocopy.common.analysis.types;

import java.util.Objects;

import chocopy.common.astnodes.ClassType;
import com.fasterxml.jackson.annotation.JsonCreator;

/** Represents the semantic value of a simple class reference. */
public class ClassValueType extends ValueType {

    /** The name of the class. */
    public final String className;

    /** A class type for the class named CLASSNAME. */
    @JsonCreator
    public ClassValueType(String className) {
        this.className = className;
    }

    /** A class type for the class referenced by CLASSTYPEANNOTATION. */
    public ClassValueType(ClassType classTypeAnnotation) {
        this.className = classTypeAnnotation.className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassValueType classType = (ClassValueType) o;
        return Objects.equals(className, classType.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public String toString() {
        return className;
    }
}
