package chocopy.common.analysis.types;

import chocopy.common.astnodes.ClassType;
import chocopy.common.astnodes.ListType;
import chocopy.common.astnodes.TypeAnnotation;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A ValueType references types that are assigned to variables and
 * expressions.
 *
 * In particular, ValueType can be a {@link ClassValueType} (e.g. "int") or
 * a {@link ListValueType} (e.g. "[int]").
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(ClassValueType.class),
        @JsonSubTypes.Type(ListValueType.class)})
public abstract class ValueType extends SymbolType {

    /** The type object. */
    public static final ClassValueType OBJECT_TYPE =
        new ClassValueType("object");
    /** The type int. */
    public static final ClassValueType INT_TYPE = new ClassValueType("int");
    /** The type str. */
    public static final ClassValueType STR_TYPE = new ClassValueType("str");
    /** The type bool. */
    public static final ClassValueType BOOL_TYPE = new ClassValueType("bool");

    /** The type of None. */
    public static final ClassValueType NONE_TYPE =
        new ClassValueType("<None>");
    /** The type of []. */
    public static final ClassValueType EMPTY_TYPE =
        new ClassValueType("<Empty>");


    /** Returns the ValueType corresponding to ANNOTATION. */
    public static ValueType annotationToValueType(TypeAnnotation annotation) {
        if (annotation instanceof ClassType) {
            return new ClassValueType((ClassType) annotation);
        } else {
            assert annotation instanceof ListType;
            return new ListValueType((ListType) annotation);
        }
    }

    /** Return true iff this is a type that does not include the value None.
     */
    @JsonIgnore
    public boolean isSpecialType() {
        return equals(INT_TYPE) || equals(BOOL_TYPE) || equals(STR_TYPE);
    }

    @JsonIgnore
    public boolean isListType() {
        return false;
    }

    /** For list types, return the type of the elements; otherwise null. */
    @JsonIgnore
    public ValueType elementType() {
        return null;
    }
}
