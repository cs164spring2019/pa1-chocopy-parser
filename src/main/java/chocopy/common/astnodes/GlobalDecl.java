package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Declaration of global variable. */
public class GlobalDecl extends Declaration {

    /** The declared variable. */
    public final Identifier variable;

    /** The AST for the declaration
     *      global VARIABLE
     *  spanning source locations [LEFT..RIGHT].
     */
    public GlobalDecl(Location left, Location right, Identifier variable) {
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
