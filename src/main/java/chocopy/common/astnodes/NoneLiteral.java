package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** The expression 'None'. */
public final class NoneLiteral extends Literal {

    /** The AST for None, spanning source locations [LEFT..RIGHT]. */
    public NoneLiteral(Location left, Location right) {
        super(left, right);
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }
}
