package chocopy.common.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import chocopy.common.analysis.SymbolTable;
import chocopy.common.astnodes.ClassDef;
import chocopy.common.astnodes.Declaration;
import chocopy.common.astnodes.FuncDef;
import chocopy.common.astnodes.GlobalDecl;
import chocopy.common.astnodes.NonLocalDecl;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.Stmt;
import chocopy.common.astnodes.TypedVar;
import chocopy.common.astnodes.VarDef;

import static chocopy.common.codegen.RiscVBackend.Register.*;

/**
 * The code generator for a ChocoPy program.
 *
 * This class implements logic to analyze all declarations
 * in a program and create descriptors for classes, functions,
 * methods, variables (global and local), and attributes. This
 * logic also builds symbol tables for globals and individual functions.
 *
 * This class also implements logic to emit global variables, object
 * prototypes and dispatch tables, as well as int/str/bool constants.
 *
 * However, this class is abstract because it does not implement logic
 * for emitting executable code in bodies of user-defined functions
 * as well as in top-level statements. This class should be extended with
 * implementations for such logic.
 *
 * All non-public members of this class are `protected`, and can be
 * overridden by sub-classes to extend change functionality.
 *
 * The SymbolInfo classes can also be overridden. If say you want to use
 * your own extended FuncInfo called MyFuncInfo (that extends FuncInfo),
 * then override the makeFuncInfo() method of this class to
 * `return new MyFuncInfo(...)` instead. This is probably not needed, though.
 */
public abstract class CodeGenBase {

    /** The backend that emits assembly. */
    protected final RiscVBackend backend;

    /** A counter for generating unique class type tags. */
    protected int nextTypeTag = 0;

    /** A counter used to generate unique local label names. */
    protected int nextLabelSuffix = 0;

    /** Predefined classes. The list "class" is a fake class; we use it only
     *  to emit a prototype object for empty lists. */
    protected final ClassInfo
        objectClass, intClass, boolClass, strClass, listClass;

    /** Predefined functions. */
    protected final FuncInfo printFunc, lenFunc, inputFunc;

    /**
     * A list of global variables, whose initial values are
     * emitted in the backend.
     */
    protected final List<GlobalVarInfo> globalVars = new ArrayList<>();

    /**
     * A list of program classes, whose prototype objects and dispatch
     * tables are emitted in the backend.
     */
    protected final List<ClassInfo> classes = new ArrayList<>();

    /**
     * A list of functions (including methods and nested functions) whose
     * bodies are emitted in the backend.
     */
    protected final List<FuncInfo> functions = new ArrayList<>();

    /** Label for built-in routine: alloc. */
    protected final Label objectAllocLabel = new Label("alloc");

    /** Label for built-in routine: alloc2. */
    protected final Label objectAllocResizeLabel = new Label("alloc2");

    /** Label for built-in routine: abort. */
    protected final Label abortLabel = new Label("abort");

    /** Label for built-in routine: heap.init. */
    protected final Label heapInitLabel = new Label("heap.init");

    /** Error codes. */
    protected final int ERROR_ARG = 1, ERROR_DIV_ZERO = 2, ERROR_OOB = 3,
        ERROR_NONE = 4, ERROR_OOM = 5, ERROR_NYI = 6;

    /** Size of heap memory. */
    protected final int HEAP_SIZE_BYTES = 1024 * 1024 * 32;

    /**
     * The symbol table that maps global names to information about
     * the bound global variables, global functions, or classes.
     */
    protected final SymbolTable<SymbolInfo> globalSymbols = new SymbolTable<>();

    /**
     * A utility for caching constants and generating labels for constants.
     */
    protected final Constants constants = new Constants();

    /** The object header size, in words (includes type tag, size,
     *  and dispatch table pointer). */
    public static final int OBJECT_HEADER_SIZE = 3;

    /**
     * Initializes a code generator for ChocoPy that uses BACKEND to emit
     * assembly code.
     *
     * The constructor creates Info objects for predefined functions,
     * classes, methods, and built-in routines.
     */
    public CodeGenBase(RiscVBackend backend) {
        this.backend = backend;

        FuncInfo objectInit = makeFuncInfo("object.__init__", 0,
                globalSymbols, null, this::emitObjectInit);
        objectInit.addParam(makeStackVarInfo("self", null, objectInit));
        functions.add(objectInit);

        objectClass = makeClassInfo("object", getNextTypeTag(), null);
        objectClass.addMethod(objectInit);
        classes.add(objectClass);
        globalSymbols.put(objectClass.getClassName(), objectClass);

        intClass = makeClassInfo("int", getNextTypeTag(), objectClass);
        intClass.addAttribute(makeAttrInfo("__int__", null));
        classes.add(intClass);
        globalSymbols.put(intClass.getClassName(), intClass);

        boolClass = makeClassInfo("bool", getNextTypeTag(), objectClass);
        boolClass.addAttribute(makeAttrInfo("__bool__", null));
        classes.add(boolClass);
        globalSymbols.put(boolClass.getClassName(), boolClass);

        strClass = makeClassInfo("str", getNextTypeTag(), objectClass);
        strClass.addAttribute(makeAttrInfo("__len__",
                                           constants.getIntConstant(0)));
        strClass.addAttribute(makeAttrInfo("__str__", null));
        classes.add(strClass);
        globalSymbols.put(strClass.getClassName(), strClass);

        listClass = makeClassInfo(".list", -1, objectClass);
        listClass.addAttribute(makeAttrInfo("__len__",
                                            constants.getIntConstant(0)));
        classes.add(listClass);
        listClass.dispatchTableLabel = null;

        printFunc = makeFuncInfo("print", 0,
                globalSymbols, null, this::emitPrint);
        printFunc.addParam(makeStackVarInfo("arg", null, printFunc));
        functions.add(printFunc);
        globalSymbols.put(printFunc.getBaseName(), printFunc);

        lenFunc = makeFuncInfo("len", 0,
                globalSymbols, null, this::emitLen);
        lenFunc.addParam(makeStackVarInfo("arg", null, lenFunc));
        functions.add(lenFunc);
        globalSymbols.put(lenFunc.getBaseName(), lenFunc);

        inputFunc = makeFuncInfo("input", 0,
                globalSymbols, null, this::emitInput);
        functions.add(inputFunc);
        globalSymbols.put(inputFunc.getBaseName(), inputFunc);
    }

    /** Return a fresh type tag. */
    protected int getNextTypeTag() {
        return nextTypeTag++;
    }

    /** Returns the next unique label suffix. */
    protected int getNextLabelSuffix() {
        return nextLabelSuffix++;
    }

    /**
     * Return a fresh label.
     *
     * This label is guaranteed to be unique amongst labels
     * generated by invoking this method. All such labels
     * have a prefix of `label_`.
     *
     * This is useful to generate local labels in
     * function bodies (e.g. for targets of jumps),
     * where the name does not matter in general.
     */
    protected Label generateLocalLabel() {
        return new Label(String.format("label_%d", getNextLabelSuffix()));
    }

    /**
     * Generates assembly code for PROGRAM.
     *
     * This is the main driver that calls internal methods for
     * emitting DATA section (globals, constants, prototypes, etc)
     * as well as the the CODE section (predefined functions, built-in
     * routines, and user-defined functions).
     */
    public void generate(Program program) {
        analyzeProgram(program);

        backend.startData();

        for (ClassInfo classInfo : this.classes) {
            emitPrototype(classInfo);
        }

        for (ClassInfo classInfo : this.classes) {
            emitDispatchTable(classInfo);
        }

        for (GlobalVarInfo global : this.globalVars) {
            backend.emitGlobalLabel(global.getLabel());
            backend.emitWordAddress(global.getInitialValue(),
                    String.format("Initial value of global var: %s",
                                  global.getVarName()));
        }

        backend.startCode();

        Label mainLabel = new Label("main");
        backend.emitGlobalLabel(mainLabel);
        backend.emitLUI(A0, HEAP_SIZE_BYTES >> 12,
                        "Initialize heap size (in multiples of 4KB)");
        backend.emitADD(S11, S11, A0, "Save heap size");
        backend.emitJAL(heapInitLabel, "Call heap.init routine");
        backend.emitMV(GP, A0, "Initialize heap pointer");
        backend.emitMV(S10, GP, "Set beginning of heap");
        backend.emitADD(S11, S10, S11,
                        "Set end of heap (= start of heap + heap size)");
        backend.emitADDI(FP, SP, 1 * backend.getWordSize(),
                         "New fp is just below stack top");

        emitTopLevel(program.statements);

        backend.emitLI(A0, 10, "Code for ecall: exit");
        backend.emitEcall(null);

        for (FuncInfo funcInfo : this.functions) {
            funcInfo.emitBody();
        }

        emitBuiltinAlloc();
        emitBuiltinAllocResize();
        emitBuiltinAbort();
        emitBuiltinHeapInit();

        emitCustomCode();

        backend.startData();
        emitConstants();
    }

    /*-----------------------------------------------------------*/
    /*                                                           */
    /*          FACTORY METHODS TO CREATE INFO OBJECTS           */
    /*                                                           */
    /*-----------------------------------------------------------*/

    /**
     * Return a descriptor for function or method FUNCNAME at nesting level
     * DEPTH in the region corresponding to PARENTSYMBOLTABLE.

     * PARENTFUNCINFO is a descriptor of the enclosing function and is `null`
     * for global functions and methods.

     * EMITTER is a method that emits the function's body (usually a
     * generic emitter for user-defined functions/methods, and a
     * special emitter for pre-defined functions/methods).
     *
     * Sub-classes of CodeGenBase can override this method
     * if they wish to use a sub-class of FuncInfo with more
     * functionality.
     */
    protected FuncInfo makeFuncInfo(String funcName, int depth,
                                    SymbolTable<SymbolInfo> parentSymbolTable,
                                    FuncInfo parentFuncInfo,
                                    Consumer<FuncInfo> emitter) {
        return new FuncInfo(funcName, depth, parentSymbolTable,
                            parentFuncInfo, emitter);
    }

    /**
     * Return a descriptor for a class named CLASSNAME having type tag
     * TYPETAG and superclass SUPERCLASSINFO (null for `object' only).
     *
     * Sub-classes of CodeGenBase can override this method
     * if they wish to use a sub-class of ClassInfo with more
     * functionality.
     */
    public ClassInfo makeClassInfo(String className, int typeTag,
                                   ClassInfo superClassInfo) {
        return new ClassInfo(className, typeTag, superClassInfo);
    }

    /**
     * Return a descriptor for an attribute named ATTRNAME and an initial
     * value located at INITIALVALUE, which must be either `null`
     * or a {@link Label} referencing a constant).
     *
     * Sub-classes of CodeGenBase can override this method
     * if they wish to use a sub-class of AttrInfo with more
     * functionality.
     */
    public AttrInfo makeAttrInfo(String attrName, Label initialValue) {
        return new AttrInfo(attrName, initialValue);
    }

    /**
     * Creates a descriptor for a local variable or parameter.
     *
     * These variables are allocated on the stack in activation
     * frames.
     *
     * Sub-classes of CodeGenBase can override this method
     * if they wish to use a sub-class of StackVarInfo with more
     * functionality.
     *
     * @param varName the name of the variable (just basename; not FQN)
     * @param initialValue the initial value of the variable or attribute
     *                     (must be either `null` or a {@link Label} referencing a constant)
     * @param funcInfo the descriptor of the function in which this var is defined
     */
    public StackVarInfo makeStackVarInfo(String varName, Label initialValue, FuncInfo funcInfo) {
        return new StackVarInfo(varName, initialValue, funcInfo);
    }

    /**
     * Creates a descriptor for a global variable.
     *
     * Sub-classes of CodeGenBase can override this method
     * if they wish to use a sub-class of GlobalVarInfo with more
     * functionality.
     *
     * @param varName the name of the variable (just basename; not FQN)
     * @param initialValue the initial value of the variable or attribute
     *                     (must be either `null` or a {@link Label} referencing a constant)
     */
    public GlobalVarInfo makeGlobalVarInfo(String varName, Label initialValue) {
        return new GlobalVarInfo(varName, initialValue);
    }

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //             ANALYSIS OF AST INTO INFO OBJECTS             //
    //   (Students can ignore these methods as all the work has  //
    //    been done and does not need to be modified/extended)   //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    /**
     * Analyzes an AST and creates Info objects for all symbols,
     * as well as populates the global symbol table.
     *
     * @param program the program to analyze
     */
    protected void analyzeProgram(Program program) {
        // First, analyze all global variable declarations
        // We do this first so that global variables are in the symbol
        // table before we encounter `global x` declarations in functions/methods
        for (Declaration decl : program.declarations) {
            if (decl instanceof VarDef) {
                // Create GlobalVarInfo object from VarDef
                VarDef varDef = (VarDef) decl;
                GlobalVarInfo globalVar = makeGlobalVarInfo(varDef.var.identifier.name,
                        constants.fromLiteral(varDef.value));

                // Add to list of global vars (for future codgen)
                this.globalVars.add(globalVar);

                // Add to global symbol table
                this.globalSymbols.put(globalVar.getVarName(), globalVar);
            }
        }

        // Now, analyze classes and global functions since
        // the global variables have been added to the symbol table
        for (Declaration decl : program.declarations) {
            if (decl instanceof ClassDef) {
                // Create ClassInfo object from ClassDef
                ClassDef classDef = (ClassDef) decl;
                ClassInfo classInfo = analyzeClass(classDef);

                // Add to list of classes (for future codegen of prototypes)
                this.classes.add(classInfo);

                // Add to global symbol table
                this.globalSymbols.put(classInfo.getClassName(), classInfo);
            } else if (decl instanceof FuncDef) {
                // Create FuncInfo object from FuncDef
                FuncDef funcDef = (FuncDef) decl;
                FuncInfo funcInfo = analyzeFunction(null, funcDef, 0,
                        globalSymbols, null); // global funcs have depth=0

                // Add to list of functions (for future codgen of body)
                this.functions.add(funcInfo);

                // Add to global symbol table
                this.globalSymbols.put(funcInfo.getBaseName(), funcInfo);
            }
        }
    }

    /**
     * Analyzes a class definition and returns an Info object.
     *
     * Also creates Info objects for attributes/methods and stores
     * them in the ClassInfo. Methods are recursively analyzed using
     * analyzeFunction().
     *
     * @param classDef the class definition to analyze
     * @return the ClassInfo object for the defined class
     */
    protected ClassInfo analyzeClass(ClassDef classDef) {
        // First, get classInfo for superclass
        String className = classDef.name.name;
        String superClassName = classDef.superClass.name;
        SymbolInfo superSymbolInfo = globalSymbols.get(superClassName);
        assert superSymbolInfo instanceof ClassInfo : "Semantic analysis should ensure that super-class is defined";
        ClassInfo superClassInfo = (ClassInfo) superSymbolInfo;
        ClassInfo classInfo = makeClassInfo(className, getNextTypeTag(), superClassInfo);

        // Analyze all attributes and methods
        for (Declaration decl : classDef.declarations) {
            if (decl instanceof VarDef) {
                // Create AttrInfo object from VarDef
                VarDef attrDef = (VarDef) decl;
                AttrInfo attrInfo = makeAttrInfo(attrDef.var.identifier.name, constants.fromLiteral(attrDef.value));

                // Add to class info
                classInfo.addAttribute(attrInfo);
            } else if (decl instanceof FuncDef) {
                // Create FuncInfo object from FuncDef
                FuncDef funcDef = (FuncDef) decl;
                FuncInfo methodInfo = analyzeFunction(className, funcDef, 0,
                        globalSymbols, null); // methods have depth=0

                // Add to list of functions (for codgen of body)
                this.functions.add(methodInfo);

                // Add to class info
                classInfo.addMethod(methodInfo);
            }
        }

        return classInfo;
    }


    /**
     * Analyzes a function or method definition and returns an Info object.
     *
     * Also analyzes any nested functions recursively. The FuncInfo's symbol
     * table is completely populated by analyzing all the params, local vars,
     * global and nonlocal var declarations.
     *
     * @param container the FQN of the containing function/class (`null` for global functions)
     * @param funcDef the function definition to analyze
     * @param depth 0 for global functions/methods, and D+1 if nested inside function with depth D
     * @param parentSymbolTable symbol table to inherit from
     *                          (is that of outer function/method for nested function definitions, and is
     *                          the same as the global symbol table for global function / method definitions)
     * @param parentFuncInfo the Info object for the parent function/method if this definition is nested,
     *                       or `null` otherwise
     * @return the FuncInfo object corresponding to the function definition
     */
    protected FuncInfo analyzeFunction(String container, FuncDef funcDef, int depth,
                                     SymbolTable<SymbolInfo> parentSymbolTable, FuncInfo parentFuncInfo) {
        // Fully-qualified name of a method or nested function is <container>.funcBaseName
        String funcBaseName = funcDef.name.name;
        String funcQualifiedName = container != null ? String.format("%s.%s", container, funcBaseName) : funcBaseName;

        // Create the function by providing the user-defined function body emitter
        FuncInfo funcInfo = makeFuncInfo(funcQualifiedName, depth,
                parentSymbolTable, parentFuncInfo, this::emitUserDefinedFunction);

        // First, analyze all the parameters and local var definitions
        // This is so that when we analyze nested functions later, the
        // `nonlocal x` statements can refer to params/locals that we
        // add here in the symbol table
        for (TypedVar param : funcDef.params) {
            // Param is a local variable with no initial value
            StackVarInfo paramInfo = makeStackVarInfo(param.identifier.name, null, funcInfo);

            // Add to func info (note: also adds to the function's internal symbol table)
            funcInfo.addParam(paramInfo);
        }

        for (Declaration decl : funcDef.declarations) {
            if (decl instanceof VarDef) {
                // Create StackVarInfo object from VarDef
                VarDef localVarDef = (VarDef) decl;
                StackVarInfo localVar = makeStackVarInfo(localVarDef.var.identifier.name,
                        constants.fromLiteral(localVarDef.value), funcInfo);

                // Add to func info (note: also adds to the function's internal symbol table)
                funcInfo.addLocal(localVar);
            } else if (decl instanceof GlobalDecl) {
                // in case of `global x` declaration, we override the mapping in the function's
                // internal symbol table
                SymbolInfo symInfo = globalSymbols.get(decl.getIdentifier().name);
                assert symInfo instanceof GlobalVarInfo : "Semantic analysis should ensure that global var exists";
                GlobalVarInfo globalVar = (GlobalVarInfo) symInfo;
                funcInfo.getSymbolTable().put(globalVar.getVarName(), globalVar);
            } else if (decl instanceof NonLocalDecl) {
                // Nothing needs to be done here
                // this declaration is mostly for use in semantic analysis.
                // Nonlocal vars are automatically inherited from the symbol table
                assert funcInfo.getSymbolTable().get(decl.getIdentifier().name) instanceof StackVarInfo
                        : "Semantic analysis should ensure that nonlocal var exists";
            }
        }

        // Now that the function's symbol table is built up, we can
        // analyze nested function definitions
        for (Declaration decl : funcDef.declarations) {
            if (decl instanceof FuncDef) {
                // Create FuncInfo object from FuncDef
                FuncDef nestedFuncDef = (FuncDef) decl;
                FuncInfo nestedFuncInfo = analyzeFunction(funcQualifiedName, nestedFuncDef,
                        depth + 1, funcInfo.getSymbolTable(), funcInfo);

                // Add to list of functions (for codgen of body)
                this.functions.add(nestedFuncInfo);

                // Add to symbol table
                funcInfo.getSymbolTable().put(nestedFuncInfo.getBaseName(), nestedFuncInfo);
            }
        }

        // Finally, add the body to the function descriptor for code gen
        funcInfo.addBody(funcDef.statements);

        return funcInfo;
    }


    ///////////////////////////////////////////////////////////////
    //                                                           //
    //  EMITING DATA SECTION FOR GLOBALS+PROTOTYPES+CONSTANTS    //
    //   (Students can ignore these methods as all the work has  //
    //    been done and does not need to be modified/extended)   //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    protected void alignObject() {
        int wordSizeLog2 = 31 - Integer.numberOfLeadingZeros(backend.getWordSize());
        backend.alignNext(wordSizeLog2); // alignment to word-size in power-of-2
    }


    protected void emitPrototype(ClassInfo classInfo) {
        backend.emitGlobalLabel(classInfo.getPrototypeLabel());
        backend.emitWordLiteral(classInfo.getTypeTag(), String.format("Type tag for class: %s", classInfo.getClassName()));
        backend.emitWordLiteral(classInfo.attributes.size() + OBJECT_HEADER_SIZE, "Object size");
        backend.emitWordAddress(classInfo.getDispatchTableLabel(), "Pointer to dispatch table");
        for (VarInfo attr : classInfo.attributes) {
            backend.emitWordAddress(attr.getInitialValue(), String.format("Initial value of attribute: %s", attr.getVarName()));
        }
        alignObject();
    }

    protected void emitConstants() {
        // Emit bool constants
        backend.emitGlobalLabel(constants.falseConstant);
        backend.emitWordLiteral(boolClass.getTypeTag(), "Type tag for class: bool");
        backend.emitWordLiteral(boolClass.attributes.size() + OBJECT_HEADER_SIZE, "Object size");
        backend.emitWordAddress(boolClass.getDispatchTableLabel(), "Pointer to dispatch table");
        backend.emitWordLiteral(0, "Constant value of attribute: __bool__");
        alignObject();

        backend.emitGlobalLabel(constants.trueConstant);
        backend.emitWordLiteral(boolClass.getTypeTag(), "Type tag for class: bool");
        backend.emitWordLiteral(boolClass.attributes.size() + OBJECT_HEADER_SIZE, "Object size");
        backend.emitWordAddress(boolClass.getDispatchTableLabel(), "Pointer to dispatch table");
        backend.emitWordLiteral(1, "Constant value of attribute: __bool__");
        alignObject();

        // Emit string constants
        for (Map.Entry<String, Label> e : constants.strConstants.entrySet()) {
            String value = e.getKey();
            Label label = e.getValue();
            // #words taken by chars = round-up (length+1) to nearest multiple of word-size
            int numWordsForCharacters = value.length()/backend.getWordSize() + 1;
            backend.emitGlobalLabel(label);
            backend.emitWordLiteral(strClass.getTypeTag(), "Type tag for class: str");
            backend.emitWordLiteral(3 + 1 + numWordsForCharacters, "Object size");
            backend.emitWordAddress(strClass.getDispatchTableLabel(), "Pointer to dispatch table");
            backend.emitWordAddress(constants.getIntConstant(value.length()), "Constant value of attribute: __len__");
            backend.emitString(value, "Constant value of attribute: __str__");
            alignObject();
        }

        // Emit integer constants
        for (Map.Entry<Integer, Label> e : constants.intConstants.entrySet()) {
            Integer value = e.getKey();
            Label label = e.getValue();
            backend.emitGlobalLabel(label);
            backend.emitWordLiteral(intClass.getTypeTag(), "Type tag for class: int");
            backend.emitWordLiteral(intClass.attributes.size() + OBJECT_HEADER_SIZE, "Object size");
            backend.emitWordAddress(intClass.getDispatchTableLabel(), "Pointer to dispatch table");
            backend.emitWordLiteral(value, "Constant value of attribute: __int__");
            alignObject();
        }
    }


    protected void emitDispatchTable(ClassInfo classInfo) {
        Label dispatchTableLabel = classInfo.getDispatchTableLabel();
        if (dispatchTableLabel == null) return;
        backend.emitGlobalLabel(dispatchTableLabel);
        for (FuncInfo method : classInfo.methods) {
            backend.emitWordAddress(method.getCodeLabel(),
                    String.format("Implementation for method: %s.%s", classInfo.getClassName(), method.getBaseName()));
        }
    }

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //   UTILITY METHODS TO GET BYTE OFFSETS IN OBJECT LAYOUT    //
    //   (Students will find these methods helpful to use in     //
    //   their sub-class when generating code for expressions)   //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    protected int getTypeTagOffset() {
        return 0 * backend.getWordSize();
    }

    protected int getObjectSizeOffset() {
        return 1 * backend.getWordSize();
    }

    protected int getDispatchTableOffset() {
        return 2 * backend.getWordSize();
    }

    protected int getAttrOffset(ClassInfo classInfo, String attrName) {
        int attrIndex = classInfo.getAttributeIndex(attrName);
        assert attrIndex >= 0 : "Type checker ensures that attributes are valid";
        return backend.getWordSize() * (OBJECT_HEADER_SIZE + attrIndex);
    }

    protected int getMethodOffset(ClassInfo classInfo, String methodName) {
        int methodIndex = classInfo.getMethodIndex(methodName);
        assert methodIndex >= 0 : "Type checker ensures that attributes are valid";
        return backend.getWordSize() * methodIndex;
    }

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //        UNIMPLEMENTED METHODS (should be extended)         //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    /**
     * Emits code for the top level.
     *
     * @param statements top level statements
     */
    protected abstract void emitTopLevel(List<Stmt> statements);

    /**
     * Emits code for the body of a user-defined function.
     *
     * @param funcInfo the function's descriptor
     */
    protected abstract void emitUserDefinedFunction(FuncInfo funcInfo);

    /**
     * Emits code outside the ChocoPy program.
     *
     * Custom assembly routines (that may be jumpable from
     * program statements) can be emitted here.
     */
    protected abstract void emitCustomCode();

    ///////////////////////////////////////////////////////////////
    //                                                           //
    //             PREDEFINED FUNCTIONS AND ROUTINES             //
    //   (Students may find a cursory read of these methods to   //
    //    be useful to get an idea for how code can be emitted)  //
    //                                                           //
    ///////////////////////////////////////////////////////////////

    /**
     * Emits code for the predefined `print` function
     *
     * @param funcInfo the function's descriptor
     */
    protected void emitPrint(FuncInfo funcInfo) {
        // Define function with a global label
        backend.emitGlobalLabel(funcInfo.getCodeLabel());

        // Note: We do not save fp/ra for this function
        // because we know that it does not use the stack or does not
        // call other functions

        // Set up labels
        Label epilogue = generateLocalLabel();
        Label illegalArg = generateLocalLabel();
        Label printInt = generateLocalLabel();
        Label printStr = generateLocalLabel();
        Label printBool = generateLocalLabel();
        Label putsA1 = generateLocalLabel();

        // Get arg in A0
        backend.emitLW(A0, SP, backend.getWordSize(), "Load arg");

        // Abort if None
        backend.emitBEQ(A0, ZERO, illegalArg, "None is an illegal argument");

        // Get type tag in T1
        backend.emitLW(T0, A0, getTypeTagOffset(), "Get type tag of arg");

        // Handle case arg is int
        backend.emitLI(T1, intClass.getTypeTag(), "Load type tag of `int`");
        backend.emitBEQ(T0, T1, printInt, "Go to print(int)");

        // Handle case arg is str
        backend.emitLI(T1, strClass.getTypeTag(), "Load type tag of `str`");
        backend.emitBEQ(T0, T1, printStr, "Go to print(str)");

        // Handle case arg is bool
        backend.emitLI(T1, boolClass.getTypeTag(), "Load type tag of `bool`");
        backend.emitBEQ(T0, T1, printBool, "Go to print(bool)");


        // Fallthrough: Invalid argument handler
        backend.emitLocalLabel(illegalArg, "Invalid argument");
        backend.emitLI(A0, ERROR_ARG, "Exit code for: Invalid argument");
        backend.emitLA(A1, constants.getStrConstant("Invalid argument"),
                "Load error message as str");
        int strAttrOffset = getAttrOffset(strClass, "__str__");
        backend.emitADDI(A1, A1, strAttrOffset, "Load address of attribute __str__");
        backend.emitJ(abortLabel, "Abort");

        // Handle: bool
        backend.emitLocalLabel(printBool, "Print bool object in A0");
        int boolAttrOffset = getAttrOffset(boolClass, "__bool__");
        backend.emitLW(A0, A0, boolAttrOffset, "Load attribute __bool__");
        Label printFalse = generateLocalLabel();
        backend.emitBEQ(A0, ZERO, printFalse, "Go to: print(False)");
        backend.emitLA(A0, constants.getStrConstant("True"), "String representation: True");
        backend.emitJ(printStr, "Go to: print(str)");
        backend.emitLocalLabel(printFalse, "Print False object in A0");
        backend.emitLA(A0, constants.getStrConstant("False"), "String representation: False");
        backend.emitJ(printStr, "Go to: print(str)");

        // Handle: str
        backend.emitLocalLabel(printStr, "Print str object in A0");
        backend.emitADDI(A1, A0, strAttrOffset, "Load address of attribute __str__");
        backend.emitJ(putsA1, "Print the null-terminated string is now in A1");
        backend.emitMV(A0, ZERO, "Load None");
        backend.emitJ(epilogue, "Go to return");

        // Handle: int
        backend.emitLocalLabel(printInt, "Print int object in A0");
        int intAttrOffset = getAttrOffset(intClass, "__int__");
        backend.emitLW(A1, A0, intAttrOffset, "Load attribute __int__");
        backend.emitLI(A0, 1, "Code for ecall: print_int");
        backend.emitEcall("Print integer");
        backend.emitLI(A1, (int) '\n', "Load newline character");
        backend.emitLI(A0, 11, "Code for ecall: print_char");
        backend.emitEcall("Print character");
        backend.emitMV(A0, ZERO, "Load None");
        backend.emitJ(epilogue, "Go to return");

        // Finally, the real print
        backend.emitLocalLabel(putsA1, "Print null-terminated string in A1");
        backend.emitLI(A0, 4, "Code for ecall: print_string");
        backend.emitEcall("Print string");
        backend.emitLI(A1, (int) '\n', "Load newline character");
        backend.emitLI(A0, 11, "Code for ecall: print_char");
        backend.emitEcall("Print character");

        // The end of a function's body is implicit `return None`
        backend.emitMV(A0, ZERO, "Load None");

        // Function epilogue
        // Note: No need to reset fp/sp here because we did not modify it at all
        // Note: RA has always been in the register; no stack pops required
        backend.emitLocalLabel(epilogue, "End of function");
        backend.emitJR(RA, "Return to caller");

    }

    /**
     * Emits code for the predefined `len` function
     *
     * @param funcInfo the function's descriptor
     */
    protected void emitLen(FuncInfo funcInfo) {
        backend.emitGlobalLabel(funcInfo.getCodeLabel());
        // Note: We do not save/restore fp/ra for this function
        // because we know that it does not use the stack or does not
        // call other functions

        // Set up labels
        Label illegalArg = generateLocalLabel();
        Label strLen = generateLocalLabel();
        Label listLen = generateLocalLabel();

        // Get argument
        backend.emitLW(A0, SP, backend.getWordSize(), "Load arg");

        // Abort if None
        backend.emitBEQ(A0, ZERO, illegalArg, "None is an illegal argument");

        // Get type tag in T1
        backend.emitLW(T0, A0, getTypeTagOffset(), "Get type tag of arg");

        // Handle case arg is str
        backend.emitLI(T1, strClass.getTypeTag(), "Load type tag of `str`");
        backend.emitBEQ(T0, T1, strLen, "Go to len(str)");

        // Handle case arg is list
        backend.emitLI(T1, listClass.getTypeTag(), "Load type tag for list objects");
        backend.emitBEQ(T0, T1, listLen, "Go to len(list)");

        // Fallthrough: Invalid argument handler
        backend.emitLocalLabel(illegalArg, "Invalid argument");
        backend.emitLI(A0, ERROR_ARG, "Exit code for: Invalid argument");
        backend.emitLA(A1, constants.getStrConstant("Invalid argument"),
                "Load error message as str");
        int strAttrOffset = getAttrOffset(strClass, "__str__");
        backend.emitADDI(A1, A1, strAttrOffset, "Load address of attribute __str__");
        backend.emitJ(abortLabel, "Abort");

        // Length of str in A0
        backend.emitLocalLabel(strLen, "Get length of string");
        int strLenAttrOffset = getAttrOffset(strClass, "__len__");
        backend.emitLW(A0, A0, strLenAttrOffset, "Load attribute: __len__");
        backend.emitJR(RA, "Return to caller");

        // Length of list in A0
        backend.emitLocalLabel(listLen, "Get length of list");
        int listLenAttrOffset = getAttrOffset(listClass, "__len__");
        backend.emitLW(A0, A0, listLenAttrOffset, "Load attribute: __len__");
        backend.emitJR(RA, "Return to caller");
    }

    /**
     * Emits code for the predefined `object.__init__` method
     *
     * @param funcInfo the method's descriptor
     */
    protected void emitObjectInit(FuncInfo funcInfo) {
        backend.emitGlobalLabel(funcInfo.getCodeLabel());
        // For efficiency, we do not save/restore the FP and RA,
        // since we know that nothing else happens in this method
        backend.emitMV(A0, ZERO, "`None` constant");
        backend.emitJR(RA, "Return");
    }

    /**
     * Emits code for the predefined `input` function
     *
     * @param funcInfo the function's descriptor
     */
    protected void emitInput(FuncInfo funcInfo) {
        backend.emitGlobalLabel(funcInfo.getCodeLabel());

        // XXX: input() has not been implemented in the current release
        backend.emitLI(A0, ERROR_NYI, "Exit code for: Unsupported operation");
        backend.emitLA(A1, constants.getStrConstant("Unsupported operation"),
                "Load error message as str");
        int strAttrOffset = getAttrOffset(strClass, "__str__");
        backend.emitADDI(A1, A1, strAttrOffset, "Load address of attribute __str__");
        backend.emitJ(abortLabel, "Abort");
    }

    /**
     * Emits code for the built-in `alloc` routine.
     */
    protected void emitBuiltinAlloc() {
        // Define a global label
        backend.emitGlobalLabel(objectAllocLabel);

        // Address of prototype is already in A0
        // Get its size in A1
        backend.emitLW(A1, A0, 1 * backend.getWordSize(), "Get size of object in words");

        // Allocate and resize to exactly object size
        backend.emitJ(objectAllocResizeLabel, "Allocate object with exact size");
    }

    /**
     * Emits code for the built-in `alloc2` routine.
     */
    protected void emitBuiltinAllocResize() {
        // Define a global label
        backend.emitGlobalLabel(objectAllocResizeLabel);

        // A1 contains the requested number of words in the new object
        // Let's estimate (in A2) where the new heap pointer will be after the copy
        Label outOfMemory = generateLocalLabel();
        backend.emitLI(A2, backend.getWordSize(), "Word size in bytes");
        backend.emitMUL(A2, A1, A2, "Calculate number of bytes to allocate");
        backend.emitADD(A2, GP, A2, "Estimate where GP will move");
        backend.emitBGEU(A2, S11, outOfMemory, "Go to OOM handler if too large");

        // We hold on to the value in A2 until the end, since that's where we want
        // the new GP to be after allocation

        /*
         * The object-copy loop uses the following registers:
         * T0 --> Number of words left to copy (initialized with object size)
         * T1 --> Temporary storage for copying words
         * T2 --> Address of next word to copy from
         * T3 --> Address of next word to copy to
         */
        RiscVBackend.Register cnt = T0, tmp = T1, src = T2, dest = T3;

        // Address of prototype is already in A0
        // Get its size in cnt
        backend.emitLW(cnt, A0, getObjectSizeOffset(), "Get size of object in words");

        // Initialize src and dest ptr
        backend.emitMV(src, A0, "Initialize src ptr");
        backend.emitMV(dest, GP, "Initialize dest ptr");

        // Copy loop (cnt words from src to dest using tmp)
        Label loopHeader = generateLocalLabel();
        backend.emitLocalLabel(loopHeader, "Copy-loop header");
        backend.emitLW(tmp, src, 0, "Load next word from src");
        backend.emitSW(tmp, dest, 0, "Store next word to dest");
        backend.emitADDI(src, src, backend.getWordSize(), "Increment src");
        backend.emitADDI(dest, dest, backend.getWordSize(), "Increment dest");
        backend.emitADDI(cnt, cnt, -1, "Decrement counter");
        backend.emitBNE(cnt, ZERO, loopHeader, "Loop if more words left to copy");

        // Set the return value of this routine to the old value of GP
        // since that is where we allocated the new object
        backend.emitMV(A0, GP, "Save new object's address to return");

        // Set the new object's size to the requested size
        backend.emitSW(A1, A0, getObjectSizeOffset(), "Set size of new object in words (same as requested size)");

        // Set new value of GP to the next free slot in memory, which we calculated in A2
        // Note that A1 = A2 if we came here from simple alloc, but A2 may be larger
        // if more words were requested
        backend.emitMV(GP, A2, "Set next free slot in the heap");

        // Return to caller
        backend.emitJR(RA, "Return to caller");

        // OOM handler
        backend.emitLocalLabel(outOfMemory, "OOM handler");
        backend.emitLI(A0, ERROR_OOM, "Exit code for: Out of memory");
        backend.emitLA(A1, constants.getStrConstant("Out of memory"),
                "Load error message as str");

        int strAttrIndex = strClass.getAttributeIndex("__str__");
        int strAttrOffset = backend.getWordSize() * (OBJECT_HEADER_SIZE + strAttrIndex);
        backend.emitADDI(A1, A1, strAttrOffset, "Load address of attribute __str__");
        backend.emitJ(abortLabel, "Abort");
    }

    /**
     * Emits code for the built-in `abort` routine.
     *
     * TODO: Direct this to puts and exit if linking with libc instead of Venus
     */
    protected void emitBuiltinAbort() {
        // Define a global label
        backend.emitGlobalLabel(abortLabel);

        // Save exit code in A0
        backend.emitMV(T0, A0, "Save exit code in temp");

        // Print error message
        backend.emitLI(A0, 4, "Code for ecall: print_string");
        backend.emitEcall("Print error message in a1");

        backend.emitLI(A1, (int) '\n', "Load newline character");
        backend.emitLI(A0, 11, "Code for ecall: print_char");
        backend.emitEcall("Print newline");

        // Exit with code
        backend.emitMV(A1, T0, "Move exit code to a1");
        backend.emitLI(A0, 17, "Code for ecall: exit2");
        backend.emitEcall("Exit with code");

        // Infinite loop to prevent fallthrough
        Label loop = generateLocalLabel();
        backend.emitLocalLabel(loop, "Infinite loop");
        backend.emitJ(loop, "Prevent fallthrough");
    }

    /**
     * Emits code for the built-in `heap.init` routine.
     */
    protected void emitBuiltinHeapInit() {
        // Define a global label
        backend.emitGlobalLabel(heapInitLabel);

        // Save exit code in A0
        backend.emitMV(A1, A0, "Move requested size to A1");

        // Print error message
        backend.emitLI(A0, 9, "Code for ecall: sbrk");
        backend.emitEcall("Request A1 bytes");

        backend.emitJR(RA, "Return to caller");
    }
}
