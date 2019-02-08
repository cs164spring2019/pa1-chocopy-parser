package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Integer numerals. */
public final class IntegerLiteral extends Literal {

    /** Value denoted. */
    public final int value;

    /** The AST for the literal VALUE, spanning source
     *  locations [LEFT..RIGHT]. */
    public IntegerLiteral(Location left, Location right, int value) {
        super(left, right);
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
