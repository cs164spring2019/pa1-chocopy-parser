package chocopy.common.codegen;

import java.util.ArrayList;
import java.util.List;

/** Information for code generation a class. */
public class ClassInfo extends SymbolInfo {

    /** Name of class. */
    protected final String className;

    /** Information about instance variables of the class. */
    public final List<AttrInfo> attributes;
    /** Information about methods of the class. */
    public final List<FuncInfo> methods;

    /** Tag indicating type of value:
     *      0: (reserved)
     *      1: int
     *      2: bool
     *      3: str
     *     -1: [T] for any T
     *     >3: User-defined types.
     */
    protected final int typeTag;
    /** Label of area containing initial instance values. */
    protected Label prototypeLabel;
    /** Label of area containing method-dispatching table. */
    protected Label dispatchTableLabel;

    /**
     * A descriptor for a class named CLASSNAME identified by runtime tag
     * TYPETAG, and having the class denoted by SUPERCLASSINFO as its
     * superclass.  The latter is null iff the class is object.
     */
    public ClassInfo(String className, int typeTag, ClassInfo superClassInfo) {
        this.className = className;
        this.typeTag = typeTag;
        prototypeLabel =
            new Label(String.format("$%s$%s", className, "prototype"));
        dispatchTableLabel =
            new Label(String.format("$%s$%s", className, "dispatchTable"));
        attributes = new ArrayList<>();
        methods = new ArrayList<>();
        if (superClassInfo != null) {
            attributes.addAll(superClassInfo.attributes);
            methods.addAll(superClassInfo.methods);
        }
    }

    /** Add an attribute described by ATTRINFO. */
    public void addAttribute(AttrInfo attrInfo) {
        this.attributes.add(attrInfo);
    }

    /** Add a method described by FUNCINFO, overriding any inherited method of
     *  that name if necessary. */
    public void addMethod(FuncInfo funcInfo) {
        String methodName = funcInfo.getBaseName();
        int idx = this.getMethodIndex(methodName);
        if (idx >= 0) {
            this.methods.set(idx, funcInfo);
        } else {
            this.methods.add(funcInfo);
        }
    }

    /** Return my type tag. */
    public int getTypeTag() {
        return typeTag;
    }

    /** Returns the address of this class's prototype object (a label). */
    public Label getPrototypeLabel() {
        return prototypeLabel;
    }

    /** Returns the address of this class's dispatch table (a label). */
    public Label getDispatchTableLabel() {
        return dispatchTableLabel;
    }

    /**
     * Returns the index of the attribute named ATTRNAME in order of
     * definition.
     *
     * This index takes into account inherited attribute and returns
     * the index of an attribute as a slot index in its object
     * layout (exlcuding the object header).  Attributes are numbered
     * from 0; the result is an index, and not a byte offset.
     */
    public int getAttributeIndex(String attrName) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).getVarName().equals(attrName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the method named METHODNAME in order of
     * definition.
     *
     * This index takes into account inherited and overridden methods
     * and returns the index of the method as a slot number (not a byte
     * offset) in the dispatch table.
     */
    public int getMethodIndex(String methodName) {
        for (int i = 0; i < methods.size(); i++) {
            if (methods.get(i).getBaseName().equals(methodName)) {
                return i;
            }
        }
        return -1;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Returns the list of attributes of this class,
     * in order of the object's layout.
     */
    public List<AttrInfo> getAttributes() {
        return attributes;
    }

    /**
     * Returns the list of methods of this class,
     * in order of the object's dispatch table.
     */
    public List<FuncInfo> getMethods() {
        return methods;
    }
}
