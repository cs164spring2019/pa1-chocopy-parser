package chocopy.common.astnodes;

import chocopy.common.analysis.types.ValueType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing expressions.
 *
 * There is nothing in this class, but there will be many AST
 * node types that have fields that are *any expression*. For those
 * cases, having a field of this type will encompass all types of
 * expressions such as binary expressions and literals that subclass
 * this class.
 */
public abstract class Expr extends Node {

    /** A Python expression spanning source locations [LEFT..RIGHT]. */
    public Expr(Location left, Location right) {
        super(left, right);
    }

    /**
     * The type of the value that this expression evaluates to.
     *
     * This field is always <tt>null</tt> after the parsing stage,
     * but is populated by the typechecker in the semantic analysis
     * stage.
     *
     * After typechecking this field may be <tt>null</tt> only for
     * expressions that cannot be assigned a type. In particular,
     * {@link NoneLiteral} expressions will not have a typed assigned
     * to them.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ValueType inferredType;

    /** Set getInferredType() to TYPE, returning TYPE. */
    public ValueType setInferredType(ValueType type) {
        inferredType = type;
        return type;
    }

    public ValueType getInferredType() {
        return inferredType;
    }

}
