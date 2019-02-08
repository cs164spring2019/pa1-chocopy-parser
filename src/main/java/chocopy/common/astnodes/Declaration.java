package chocopy.common.astnodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Base of all AST nodes representing definitions or declarations.
 */
public abstract class Declaration extends Node {

    /** A definition or declaration spanning source locations [LEFT..RIGHT]. */
    public Declaration(Location left, Location right) {
        super(left, right);
    }

    /** Return the identifier defined by this Declaration. */
    @JsonIgnore
    public abstract Identifier getIdentifier();
}
