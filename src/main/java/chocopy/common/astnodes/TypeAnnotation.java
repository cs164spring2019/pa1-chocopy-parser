package chocopy.common.astnodes;

import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing type annotations (list or class
 * types.
 */
public abstract class TypeAnnotation extends Node {
    /** An annotation spanning source locations [LEFT..RIGHT]. */
    public TypeAnnotation(Location left, Location right) {
        super(left, right);
    }
}
