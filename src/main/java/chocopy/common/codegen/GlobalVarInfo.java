package chocopy.common.codegen;

public class GlobalVarInfo extends VarInfo {

    protected final Label label;

    /**
     * Creates a descriptor for a global variable.
     *
     * @param varName the name of the variable (just basename; not FQN)
     * @param initialValue the initial value of the variable or attribute
     *                     (must be either `null` or a {@link Label} referencing a constant)
     */
    public GlobalVarInfo(String varName, Label initialValue) {
        super(varName, initialValue);
        this.label = new Label(String.format("$%s", varName)); // prepend $ sign to prevent collisions
    }

    /**
     * Returns the label where the global variable is defined.
     *
     * @return the definition label
     */
    public Label getLabel() {
        return label;
    }
}
