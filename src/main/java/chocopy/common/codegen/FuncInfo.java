package chocopy.common.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.astnodes.Stmt;

/**
 * A descriptor for function and method definitions.
 *
 * This class stores information required for code generation
 * such as the information about a function's parameters, local variables,
 * the local symbol table, the function body, and the label where the code
 * for the body is generated.
 */
public class FuncInfo extends SymbolInfo {

    /**
     * The fully-qualified name of the function.
     *
     * All functions in a ChocoPy program have a unique fully-qualified name.
     * Global functions defined with name `f` have fully-qualified name `f`.
     * Methods `m` in a class `C` have fully-qualified name `C.m`.
     * Functions `f` nested inside another function
     * with fully-qualified name `F` have a fully-qualified name of `F.f`.
     */
    protected final String funcName;

    /**
     * The static depth of a function.
     *
     * Global functions and class methods have a static depth of 0.
     * Nested functions that are defined in the body of a function
     * with static depth `D` have a static depth of `D+1`.
     */
    protected final int depth;

    /** A list of parameter names. */
    protected final List<String> params = new ArrayList<>();

    /** A list of local variable descriptors. */
    protected final List<StackVarInfo> locals = new ArrayList<>();

    /** The function body. */
    protected final List<Stmt> statements = new ArrayList<>();

    /** The local symbol table that binds identifiers seen in the function's body. */
    protected final SymbolTable<SymbolInfo> symbolTable;

    /** The label of the generated code for the function's body. */
    protected final Label codeLabel;

    /** The descriptor of the enclosing function (this is only non-null for nested functions). */
    protected final FuncInfo parentFuncInfo;

    /**
     * A method that is invoked to emit the function's body.
     *
     * The method should accept one parameter of type `FuncInfo`.
     */
    protected Consumer<FuncInfo> emitter;

    /**
     * Creates a descriptor for a function or method.
     *
     * @param funcName the fully-qualified name of the function
     * @param depth the nesting depth of the function
     * @param parentSymbolTable the symbol table of the containing scope
     * @param parentFuncInfo descriptor of the enclosing function (`null` for global functions and methods)
     * @param emitter a method that emits the function's body
     *                (this is usually a generic emitter for user-defined functions/methods,
     *                and a special emitter for pre-defined functions/methods)
     */
    public FuncInfo(String funcName, int depth, SymbolTable<SymbolInfo> parentSymbolTable, FuncInfo parentFuncInfo, Consumer<FuncInfo> emitter) {
        this.funcName = funcName;
        this.codeLabel = new Label(String.format("$%s", funcName)); // prepend $ sign to prevent collisions
        this.depth = depth;
        this.symbolTable = new SymbolTable<>(parentSymbolTable);
        this.parentFuncInfo = parentFuncInfo;
        this.emitter = emitter;
    }

    /**
     * Adds a parameter to this function.
     *
     * @param paramInfo the parameter's descriptor
     */
    public void addParam(StackVarInfo paramInfo) {
        this.params.add(paramInfo.getVarName());
        this.symbolTable.put(paramInfo.getVarName(), paramInfo);
    }


    /**
     * Adds a local variable to this function.
     *
     * @param stackVarInfo the variable's descriptor
     */
    public void addLocal(StackVarInfo stackVarInfo) {
        this.locals.add(stackVarInfo);
        this.symbolTable.put(stackVarInfo.getVarName(), stackVarInfo);
    }

    /**
     * Adds the statements in the function.
     *
     * @param stmts the statements in the function's body
     */
    public void addBody(List<Stmt> stmts) {
        statements.addAll(stmts);
    }

    /**
     * Returns the index of a parameter or local variable
     * in the function's activation record.
     *
     * The convention is that for a function with `N` params
     * and `K` local vars, the `i`th param is at index `i`
     * and the `j`th local var is at index `N+j`. In all,
     * a function stores `N+K` variables contiguously in
     * its activation record.
     *
     * Note: this is an index (starting at 0), and not an offset in
     * number of bytes.
     *
     * @param name
     * @return
     */
    public int getVarIndex(String name) {
        // First search in params
        int idx = params.indexOf(name);
        if (idx >= 0) {
            return idx;
        }
        // Otherwise search in locals
        for (int i = 0; i < locals.size(); i++) {
            if(locals.get(i).getVarName().equals(name)) {
                // Makes sure to offset from param size
                return i + params.size();
            }
        }
        // Should never happen, since semantic analysis is done
        throw new IllegalArgumentException(String.format("%s is not a var defined in function %s", name, funcName));
    }

    /**
     * Returns the label corresponding to the function's body
     * in assembly.
     *
     * @return the function's code label
     */
    public Label getCodeLabel() {
        return codeLabel;
    }

    /**
     * Returns the function's defined name in the program.
     *
     * This is the last component of the dot-separated
     * fully-qualified name.
     *
     * @return the function's defined name
     */
    public String getBaseName() {
        int rightmostDotIndex = funcName.lastIndexOf('.');
        if (rightmostDotIndex == -1) {
            return funcName;
        } else {
            return funcName.substring(rightmostDotIndex+1);
        }
    }

    /**
     * Returns the function's fully-qualified name.
     *
     * @return the function's fully-qualified name
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * Returns the function's static depth.
     *
     * @return the function's static depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Returns the function's parameters in order of definition.
     *
     * @return the function's parameters
     */
    public List<String> getParams() {
        return params;
    }

    /**
     * Returns the function's explicitly defined local variables.
     * Excludes parameters.
     *
     * This list is mainly used in generating code for
     * initializing local variables that are not parameters.
     *
     * @return the function's explicitly defined local variables
     */
    public List<StackVarInfo> getLocals() {
        return locals;
    }

    /**
     * Returns the function's body.
     *
     * @return list of statements in the function's body
     */
    public List<Stmt> getStatements() {
        return statements;
    }

    /**
     * Returns the function's local symbol table.
     *
     * @return the function's local symbol table
     */
    public SymbolTable<SymbolInfo> getSymbolTable() {
        return symbolTable;
    }

    /**
     * Returns the parent function's descriptor for nested functions.
     *
     * @return the parent function's descriptor (or `null` if this is not a nested function)
     */
    public FuncInfo getParentFuncInfo() {
        return parentFuncInfo;
    }

    /**
     * Emits a function's body.
     */
    public void emitBody() {
        emitter.accept(this);
    }
}
