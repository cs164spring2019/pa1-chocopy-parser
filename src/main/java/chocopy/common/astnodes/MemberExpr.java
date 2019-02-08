package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Attribute accessor. */
public class MemberExpr extends Expr {

    /** Object selected from. */
    public final Expr object;
    /** Name of attribute (instance variable or method). */
    public final Identifier member;

    /** The AST for
     *     OBJECT.MEMBER.
     *  spanning source locations [LEFT..RIGHT].
     */
    public MemberExpr(Location left, Location right,
                      Expr object, Identifier member) {
        super(left, right);
        this.object = object;
        this.member = member;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
