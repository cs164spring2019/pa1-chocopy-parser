package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Indefinite repetition construct. */
public class WhileStmt extends Stmt {
    /** Test for whether to continue. */
    public final Expr condition;
    /** Loop body. */
    public final List<Stmt> body;

    /** The AST for
     *      while CONDITION:
     *          BODY
     *  spanning source locations [LEFT..RIGHT].
     */
    public WhileStmt(Location left, Location right,
                     Expr condition, List<Stmt> body) {
        super(left, right);
        this.condition = condition;
        this.body = body;
    }


    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
