package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Return from function. */
public class ReturnStmt extends Stmt {

    /** Returned value. */
    public final Expr value;

    /** The AST for
     *     return VALUE
     *  spanning source locations [LEFT..RIGHT].
     */
    public ReturnStmt(Location left, Location right, Expr value) {
        super(left, right);
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
