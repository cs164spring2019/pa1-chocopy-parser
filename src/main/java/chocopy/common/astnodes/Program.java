package chocopy.common.astnodes;

import java.util.List;
import java.util.ArrayList;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;

/** An entire Chocopy program. */
public class Program extends Node {

    /** Initial variable, class, and function declarations. */
    public final List<Declaration> declarations;
    /** Trailing statements. */
    public final List<Stmt> statements;
    /** Accumulated errors. */
    public final Errors errors;

    /** The AST for the program
     *     DECLARATIONS
     *     STATEMENTS
     *  spanning source locations [LEFT..RIGHT].
     *
     *  ERRORS is the container for all error messages applying to the
     *  program. */
    public Program(Location left, Location right,
                   List<Declaration> declarations, List<Stmt> statements,
                   Errors errors) {
        super(left, right);
        this.declarations = declarations;
        this.statements = statements;
        if (errors == null) {
            this.errors = new Errors(new ArrayList<CompilerError>());
        } else {
            this.errors = errors;
        }
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    /** Returns true iff there is at least one error in the program. */
    @JsonIgnore
    public boolean hasErrors() {
        return errors.hasErrors();
    }

    /** A convenience method returning the list of all CompilerErrors for
     *  this program. */
    @JsonIgnore
    public List<CompilerError> getErrorList() {
        return errors.errors;
    }
}
