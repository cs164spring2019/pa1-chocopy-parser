package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Literals True or False. */
public final class BooleanLiteral extends Literal {

    /** True iff I represent True. */
    public final boolean value;

    /** An AST for the token True or False at [LEFT..RIGHT], depending on
     *  VALUE. */
    public BooleanLiteral(Location left, Location right, boolean value) {
        super(left, right);
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
