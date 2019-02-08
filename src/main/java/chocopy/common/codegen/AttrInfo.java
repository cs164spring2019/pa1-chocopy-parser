package chocopy.common.codegen;

/** Information concerning an instance variable. */
public class AttrInfo extends VarInfo {

    /**
     * A descriptor for an attribute named ATTRNAME whose initial value, if
     * any, is a constant located at INITIALVALUE (it is otherwise null).
     */
    public AttrInfo(String attrName, Label initialValue) {
        super(attrName, initialValue);
    }
}
