package chocopy.common.astnodes;

import chocopy.common.analysis.NodeAnalyzer;
import java_cup.runtime.ComplexSymbolFactory.Location;

/** A simple class type name. */
public final class ClassType extends TypeAnnotation {

    /** The denotation of the class in source. */
    public final String className;

    /** An AST denoting a type named CLASSNAME0 at [LEFT..RIGHT]. */
    public ClassType(Location left, Location right, String className0) {
        super(left, right);
        className = className0;
    }

    public <T> T dispatch(NodeAnalyzer<T> analyzer) {
        return analyzer.analyze(this);
    }

}
