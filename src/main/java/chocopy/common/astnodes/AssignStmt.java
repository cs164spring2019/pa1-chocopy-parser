package chocopy.common.astnodes;

import java.util.List;
import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Single and multiple assignments. */
public class AssignStmt extends Stmt {
    /** List of left-hand sides. */
    public final List<Expr> targets;
    /** Right-hand-side value to be assigned. */
    public final Expr value;

    /** AST for TARGETS[0] = TARGETS[1] = ... = VALUE spanning source locations
     *  [LEFT..RIGHT].
     */
    public AssignStmt(Location left, Location right,
                      List<Expr> targets, Expr value) {
        super(left, right);
        this.targets = targets;
        this.value = value;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
