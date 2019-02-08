package chocopy.common.astnodes;

import java.io.IOException;

import chocopy.common.analysis.NodeAnalyzer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Root of the AST class hierarchy.  Every node has a left and right
 * location, indicating the start and end of the represented construct
 * in the source text.
 *
 * Every node can be marked with an error message, which serves two purposes:
 *   1. It indicates that an error message has been issued for this
 *      Node, allowing tne program to reduce cascades of error
 *      messages.
 *   2. It aids in debugging by making it convenient to see which
 *      Nodes have caused an error.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.EXISTING_PROPERTY,
              property = "kind")
/* List of all concrete subclasses of Node. */
@JsonSubTypes({
        @JsonSubTypes.Type(AssignStmt.class),
        @JsonSubTypes.Type(BinaryExpr.class),
        @JsonSubTypes.Type(BooleanLiteral.class),
        @JsonSubTypes.Type(CallExpr.class),
        @JsonSubTypes.Type(ClassDef.class),
        @JsonSubTypes.Type(ClassType.class),
        @JsonSubTypes.Type(CompilerError.class),
        @JsonSubTypes.Type(Errors.class),
        @JsonSubTypes.Type(ExprStmt.class),
        @JsonSubTypes.Type(ForStmt.class),
        @JsonSubTypes.Type(FuncDef.class),
        @JsonSubTypes.Type(GlobalDecl.class),
        @JsonSubTypes.Type(Identifier.class),
        @JsonSubTypes.Type(IfExpr.class),
        @JsonSubTypes.Type(IfStmt.class),
        @JsonSubTypes.Type(IndexExpr.class),
        @JsonSubTypes.Type(IntegerLiteral.class),
        @JsonSubTypes.Type(ListExpr.class),
        @JsonSubTypes.Type(ListType.class),
        @JsonSubTypes.Type(MemberExpr.class),
        @JsonSubTypes.Type(MethodCallExpr.class),
        @JsonSubTypes.Type(NoneLiteral.class),
        @JsonSubTypes.Type(NonLocalDecl.class),
        @JsonSubTypes.Type(Program.class),
        @JsonSubTypes.Type(ReturnStmt.class),
        @JsonSubTypes.Type(StringLiteral.class),
        @JsonSubTypes.Type(TypedVar.class),
        @JsonSubTypes.Type(UnaryExpr.class),
        @JsonSubTypes.Type(VarDef.class),
        @JsonSubTypes.Type(WhileStmt.class),
})
public abstract class Node {

    /** Node-type indicator for JSON form. */
    public final String kind;

    /** Source position information: 0: line number of start, 1: column number
     *  of start, 2: line number of end, 3: column number of end. */
    private final int[] location = new int[4];

    /** First error message "blamed" on this Node. When non-null, indicates
     *  that an error has been found in this Node. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String errorMsg;

    /** A Node corresponding to source text between LEFT and RIGHT. */
    public Node(Location left, Location right) {
        if (left != null) {
            location[0] = left.getLine();
            location[1] = left.getColumn();
        }
        if (right != null) {
            location[2] = right.getLine();
            location[3] = right.getColumn();
        }
        this.kind = getClass().getSimpleName();
        this.errorMsg = null;
    }

    /** Return my source location as
     *     { <first line>, <first column>, <last line>, <last column> }.
     *  Result should not be modified, and contents will change after
     *  setLocation(). */
    public int[] getLocation() {
        return location;
    }

    /** Copy LOCATION as getLocation(). */
    public void setLocation(final int[] location) {
        System.arraycopy(location, 0, this.location, 0, 4);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String msg) {
        this.errorMsg = msg;
    }

    /** Return true iff I have been marked with an error message. */
    @JsonIgnore
    public boolean hasError() {
        return this.errorMsg != null;
    }

    /** Invoke ANALYZER on me as a node of static type T.  See the comment
     *  on NodeAnalyzer. Returns modified Node. */
    public abstract <T> T dispatch(NodeAnalyzer<T> analyzer);

    /** Print out the AST in JSON format. */
    @Override
    public String toString() {
        try {
            return toJSON();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Return a serialization of this node in JSON fprmat. */
    public String toJSON() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    /** Mapper to-and-from serialized JSON. */
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new ParameterNamesModule());
    }

    /** Returns a T from JSON, a JSON-eerialized T value with class
     *  CLAS. */
    public static <T> T fromJSON(String json, Class<T> clas)
        throws IOException {
        return mapper.readValue(json, clas);
    }

    /** Returns the result of converting JSON, a JSon-serialization of
     *  a Node value, into the value it serializes. */
    public static Node fromJSON(String json)
        throws IOException {
        return fromJSON(json, Node.class);
    }

    /** Returns the result of converting TREE to the value of type T
     *  that it represents, where CLAS reflects T. */
    public static <T> T fromJSON(JsonNode tree, Class<T> clas)
        throws IOException {
        return mapper.treeToValue(tree, clas);
    }

    /** Returns the translation of serialized value SRC into the
     *  corresponding JSON tree. */
    public static JsonNode readTree(String src) throws IOException {
        return mapper.readTree(src);
    }

}
