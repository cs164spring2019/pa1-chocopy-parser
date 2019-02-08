package chocopy.common.astnodes;

import java.util.List;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** Def statements. */
public class FuncDef extends Declaration {

    /** Defined name. */
    public final Identifier name;
    /** Formal parameters. */
    public final List<TypedVar> params;
    /** Return type annotation. */
    public final TypeAnnotation returnType;
    /** Local-variable,inner-function, global, and nonlocal declarations. */
    public final List<Declaration> declarations;
    /** Other statements. */
    public final List<Stmt> statements;

    /** The AST for
     *     def NAME(PARAMS) -> RETURNTYPE:
     *         DECLARATIONS
     *         STATEMENTS
     *  spanning source locations [LEFT..RIGHT].
     */
    public FuncDef(Location left, Location right,
                   Identifier name, List<TypedVar> params,
                   TypeAnnotation returnType,
                   List<Declaration> declarations, List<Stmt> statements) {
        super(left, right);
        this.name = name;
        this.params = params;
        this.returnType = returnType;
        this.declarations = declarations;
        this.statements = statements;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    @Override
    public Identifier getIdentifier() {
        return this.name;
    }
}
