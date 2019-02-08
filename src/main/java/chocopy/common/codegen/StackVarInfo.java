package chocopy.common.codegen;

public class StackVarInfo extends VarInfo {

    protected final FuncInfo funcInfo;

    /**
     * Creates a descriptor for a local variable or parameter.
     *
     * These variables are allocated on the stack in activation
     * frames.
     *
     * @param varName the name of the variable (just basename; not FQN)
     * @param initialValue the initial value of the variable or attribute
     *                     (must be either `null` or a {@link Label} referencing a constant)
     * @param funcInfo the descriptor of the function in which this var is defined
     */
    public StackVarInfo(String varName, Label initialValue, FuncInfo funcInfo) {
        super(varName, initialValue);
        this.funcInfo = funcInfo;
    }

    /**
     * Returns the descriptor of the function in which this var is defined.
     *
     * @return the descriptor of the function in which this var is defined
     */
    public FuncInfo getFuncInfo() {
        return funcInfo;
    }
}
