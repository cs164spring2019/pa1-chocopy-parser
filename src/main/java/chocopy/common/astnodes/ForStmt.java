package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** For statements. */
public class ForStmt extends Stmt {
    /** Control variable. */
    public final Identifier identifier;
    /** Source of values of control statement. */
    public final Expr iterable;
    /** Repeated statements. */
    public final List<Stmt> body;

    /** The AST for
     *      for IDENTIFIER in ITERABLE:
     *          BODY
     *  spanning source locations [LEFT..RIGHT].
     */
    public ForStmt(Location left, Location right,
                   Identifier identifier, Expr iterable, List<Stmt> body) {
        super(left, right);
        this.identifier = identifier;
        this.iterable = iterable;
        this.body = body;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
