package chocopy.common.codegen;

/** Code-generation information about a local variable or parameter. */
public class StackVarInfo extends VarInfo {

    /** Information about the enclosing function. */
    protected final FuncInfo funcInfo;

    /**
     * A descriptor for a local variable or parameter VARNAME, whose initial
     * value is stored at INITIALVALUE (null if no initial value), and which
     * is nested immediately within the function described by FUNCINFO.
     */
    public StackVarInfo(String varName, Label initialValue,
                        FuncInfo funcInfo) {
        super(varName, initialValue);
        this.funcInfo = funcInfo;
    }

    /**
     * Returns the descriptor of the function in which this var is defined.
     */
    public FuncInfo getFuncInfo() {
        return funcInfo;
    }
}
