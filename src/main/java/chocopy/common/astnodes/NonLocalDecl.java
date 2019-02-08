package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Nonlocal declaration. */
public class NonLocalDecl extends Declaration {

    /** Name of identifier being declared. */
    public final Identifier variable;

    /** The AST for
     *      nonlocal VARIABLE
     *  spanning source locations [LEFT..RIGHT].
     */
    public NonLocalDecl(Location left, Location right, Identifier variable) {
        super(left, right);
        this.variable = variable;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public Identifier getIdentifier() {
        return this.variable;
    }
}
