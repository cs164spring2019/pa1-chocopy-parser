package chocopy.common.astnodes;

import java.util.Arrays;
import java.util.Objects;

import java_cup.runtime.ComplexSymbolFactory.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import chocopy.common.analysis.NodeAnalyzer;

/** Represents a single error.  Does not correspond to any Python source
 *  construct. */
public class CompilerError extends Node {

    /** Represents an error with message MESSAGE.  Iff SYNTAX, it is a
     *  syntactic error.  The error applies to source text at [LEFT..RIGHT]. */
    public CompilerError(Location left, Location right, String message,
                         boolean syntax) {
        super(left, right);
        this.message = message;
        this.syntax = syntax;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isSyntax() {
        return syntax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompilerError that = (CompilerError) o;
        return Objects.equals(message, that.message)
               && Arrays.equals(getLocation(), that.getLocation());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(message);
        result = 31 * result + Arrays.hashCode(getLocation());
        return result;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

    /** The error message. */
    public final String message;
    /** True if this is a syntax error. */
    private final boolean syntax;
}
