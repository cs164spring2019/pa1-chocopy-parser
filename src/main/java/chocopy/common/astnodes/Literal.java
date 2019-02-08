package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all the literal nodes.
 *
 * There is nothing in this class, but it is useful to isolate
 * expressions that are constant literals.
 */
public abstract class Literal extends Expr {
    /** A literal spanning source locations [LEFT..RIGHT]. */
    public Literal(Location left, Location right) {
        super(left, right);
    }
}
