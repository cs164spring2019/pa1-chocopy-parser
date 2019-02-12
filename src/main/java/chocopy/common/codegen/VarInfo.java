package chocopy.common.codegen;

/** Information about a variable or attribute. */
public abstract class VarInfo extends SymbolInfo {

    /** Name of variable or attribute. */
    protected final String varName;
    /** Runtime location of initial value for this variable or attribute. */
    protected final Label initialValue;

    /**
     * A descriptor for variable or attribute VARNAME with INITIALVALUE as
     * the location of its initial value (or null if none).
     * The initial value may be null (for `None` values)
     * or a {@link Label} referencing a constant int/str/bool.
     */
    public VarInfo(String varName, Label initialValue) {
        this.varName = varName;
        this.initialValue = initialValue;
    }

    /** Returns the name of this variable or attribute. */
    public String getVarName() {
        return varName;
    }

    /**
     * Returns label of the initial value of this variable or attribute.
     */
    public Label getInitialValue() {
        return initialValue;
    }
}
