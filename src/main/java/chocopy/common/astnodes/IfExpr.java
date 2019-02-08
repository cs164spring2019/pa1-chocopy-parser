package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Conditional expressions. */
public class IfExpr extends Expr {
    /** Boolean condition. */
    public final Expr condition;
    /** True branch. */
    public final Expr thenExpr;
    /** False branch. */
    public final Expr elseExpr;

    /** The AST for
     *     THENEXPR if CONDITION else ELSEEXPR
     *  spanning source locations [LEFT..RIGHT].
     */
    public IfExpr(Location left, Location right,
                  Expr condition, Expr thenExpr, Expr elseExpr) {
        super(left, right);
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
