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
        return null;
    }

    @Override
    public T analyze(BinaryExpr node) {
        return null;
    }

    @Override
    public T analyze(BooleanLiteral node) {
        return null;
    }

    @Override
    public T analyze(CallExpr node) {
        return null;
    }

    @Override
    public T analyze(ClassDef node) {
        return null;
    }

    @Override
    public T analyze(ClassType node) {
        return null;
    }

    @Override
    public T analyze(CompilerError node) {
        return null;
    }

    @Override
    public T analyze(Errors node) {
        return null;
    }

    @Override
    public T analyze(ExprStmt node) {
        return null;
    }

    @Override
    public T analyze(ForStmt node) {
        return null;
    }

    @Override
    public T analyze(FuncDef node) {
        return null;
    }

    @Override
    public T analyze(GlobalDecl node) {
        return null;
    }

    @Override
    public T analyze(Identifier node) {
        return null;
    }

    @Override
    public T analyze(IfExpr node) {
        return null;
    }

    @Override
    public T analyze(IfStmt node) {
        return null;
    }

    @Override
    public T analyze(IndexExpr node) {
        return null;
    }

    @Override
    public T analyze(IntegerLiteral node) {
        return null;
    }

    @Override
    public T analyze(ListExpr node) {
        return null;
    }

    @Override
    public T analyze(ListType node) {
        return null;
    }

    @Override
    public T analyze(MemberExpr node) {
        return null;
    }

    @Override
    public T analyze(MethodCallExpr node) {
        return null;
    }

    @Override
    public T analyze(NoneLiteral node) {
        return null;
    }

    @Override
    public T analyze(NonLocalDecl node) {
        return null;
    }

    @Override
    public T analyze(Program node) {
        return null;
    }

    @Override
    public T analyze(ReturnStmt node) {
        return null;
    }

    @Override
    public T analyze(StringLiteral node) {
        return null;
    }

    @Override
    public T analyze(TypedVar node) {
        return null;
    }

    @Override
    public T analyze(UnaryExpr node) {
        return null;
    }

    @Override
    public T analyze(VarDef node) {
        return null;
    }

    @Override
    public T analyze(WhileStmt node) {
        return null;
    }
}
