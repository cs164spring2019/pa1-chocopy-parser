package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** <operand> <operator> <operand>. */
public class BinaryExpr extends Expr {

    /** Left operand. */
    public final Expr left;
    /** Operator name. */
    public final String operator;
    /** Right operand. */
    public final Expr right;

    /** An AST for expressions of the form LEFTEXPR OP RIGHTEXPR
     *  from text in range [LEFTLOC..RIGHTLOC]. */
    public BinaryExpr(Location leftLoc, Location rightLoc, Expr leftExpr,
                      String op, Expr rightExpr) {
        super(leftLoc, rightLoc);
        left = leftExpr;
        operator = op;
        right = rightExpr;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
