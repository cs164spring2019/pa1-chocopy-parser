package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** A simple identifier. */
public class Identifier extends Expr {

    /** Text of the identifier. */
    public final String name;

    /** An AST for the variable, method, or parameter named NAME, spanning
     *  source locations [LEFT..RIGHT]. */
    public Identifier(Location left, Location right, String name) {
        super(left, right);
        this.name = name;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
