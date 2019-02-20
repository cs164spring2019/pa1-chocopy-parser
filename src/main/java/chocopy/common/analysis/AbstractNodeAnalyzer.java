package chocopy.common.analysis;

import chocopy.common.astnodes.*;

/**
 * An empty implementation of the {@link NodeAnalyzer} that
 * simply returns does nothing and returns null for every
 * AST node type.
 *
 * T is the type of analysis result.
 */
public class AbstractNodeAnalyzer<T> implements NodeAnalyzer<T> {
    @Override
    public T analyze(AssignStmt node) {
        return defaultValue;
    }

    @Override
    public T analyze(BinaryExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(BooleanLiteral node) {
        return defaultValue;
    }

    @Override
    public T analyze(CallExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(ClassDef node) {
        return defaultValue;
    }

    @Override
    public T analyze(ClassType node) {
        return defaultValue;
    }

    @Override
    public T analyze(CompilerError node) {
        return defaultValue;
    }

    @Override
    public T analyze(Errors node) {
        return defaultValue;
    }

    @Override
    public T analyze(ExprStmt node) {
        return defaultValue;
    }

    @Override
    public T analyze(ForStmt node) {
        return defaultValue;
    }

    @Override
    public T analyze(FuncDef node) {
        return defaultValue;
    }

    @Override
    public T analyze(GlobalDecl node) {
        return defaultValue;
    }

    @Override
    public T analyze(Identifier node) {
        return defaultValue;
    }

    @Override
    public T analyze(IfExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(IfStmt node) {
        return defaultValue;
    }

    @Override
    public T analyze(IndexExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(IntegerLiteral node) {
        return defaultValue;
    }

    @Override
    public T analyze(ListExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(ListType node) {
        return defaultValue;
    }

    @Override
    public T analyze(MemberExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(MethodCallExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(NoneLiteral node) {
        return defaultValue;
    }

    @Override
    public T analyze(NonLocalDecl node) {
        return defaultValue;
    }

    @Override
    public T analyze(Program node) {
        return defaultValue;
    }

    @Override
    public T analyze(ReturnStmt node) {
        return defaultValue;
    }

    @Override
    public T analyze(StringLiteral node) {
        return defaultValue;
    }

    @Override
    public T analyze(TypedVar node) {
        return defaultValue;
    }

    @Override
    public T analyze(UnaryExpr node) {
        return defaultValue;
    }

    @Override
    public T analyze(VarDef node) {
        return defaultValue;
    }

    @Override
    public T analyze(WhileStmt node) {
        return defaultValue;
    }

    @Override
    public void setDefault(T value) {
        defaultValue = value;
    }

    private T defaultValue = null;

}
