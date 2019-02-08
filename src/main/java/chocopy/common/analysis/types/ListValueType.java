package chocopy.common.analysis.types;

import java.util.Objects;

import chocopy.common.astnodes.ListType;
import com.fasterxml.jackson.annotation.JsonCreator;

/** Represents a semantic value of a list type denotation. */
public class ListValueType extends ValueType {

    /** This ListValueType represents [ELEMENTTYPE]. */
    public final ValueType elementType;

    /** Represents [ELEMENTTYPE]. */
    @JsonCreator
    public ListValueType(ValueType elementType) {
        this.elementType = elementType;
    }

    /** Represents [<type>], where <type> is that denoted in TYPEANNOTATION. */
    public ListValueType(ListType typeAnnotation) {
        elementType
            = ValueType.annotationToValueType(typeAnnotation.elementType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListValueType listType = (ListValueType) o;
        return Objects.equals(elementType, listType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }

    @Override
    public String toString() {
        return "[" + elementType.toString() + "]";
    }

    /** Returns true iff I represent [T]. */
    @Override
    public boolean isListType() {
        return true;
    }

    @Override
    public ValueType elementType() {
        return elementType;
    }
}
