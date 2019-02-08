package chocopy.common.codegen;

public abstract class VarInfo extends SymbolInfo {

    protected final String varName;
    protected final Label initialValue;

    /**
     * Creates a descriptor for a variable or attribute.
     *
     * @param varName the name of the variable or attribute
     * @param initialValue the initial value of the variable or attribute
     *                     (must be either `null` or a {@link Label} referencing a constant)
     */
    public VarInfo(String varName, Label initialValue) {
        this.varName = varName;
        this.initialValue = initialValue;
    }

    /**
     * Returns the name of the variable or attribute
     *
     * @return the name of the variable or attribute
     */
    public String getVarName() {
        return varName;
    }

    /**
     * Returns initial value of the variable or attribute.
     *
     * The initial value may be `null` (for `None` values)
     * or a {@link Label} referencing a constant int/str/bool.
     *
     * @return the initial value of the variable or attribute
     */
    public Label getInitialValue() {
        return initialValue;
    }
}
