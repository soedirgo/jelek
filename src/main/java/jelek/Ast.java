package jelek;

import java.util.ArrayList;
import java.util.List;
import jelek.StaticCheck.StaticCheckException;

class Ast {
    static class Program {
        final String _class = "Program";
        List<Class> classes;

        Program(List<Class> classes) { this.classes = classes; }
    }

    static class Class {
        final String _class = "Class";
        final String name;
        final List<Var> vars;
        final List<Method> methods;

        Class(String name, List<Var> vars, List<Method> methods) {
            this.name = name;
            this.vars = vars;
            this.methods = methods;
        }
    }

    static class Method {
        final String _class = "Method";
        final String id;
        final Type returnType;
        final List<Var> params;
        final List<Var> vars;
        final List<Stmt> stmts;

        Method(String id, Type returnType, List<Var> params, List<Var> vars,
               List<Stmt> stmts) {
            this.id = id;
            this.returnType = returnType;
            this.params = params;
            this.vars = vars;
            this.stmts = stmts;
        }
    }

    abstract static class Stmt {
        final String _class = this.getClass().getName();

        abstract <R> R accept(Visitor<R> visitor) throws StaticCheckException;

        interface Visitor<R> {
            R visitIf(If stmt) throws StaticCheckException;
            R visitWhile(While stmt) throws StaticCheckException;
            R visitReadln(Readln stmt) throws StaticCheckException;
            R visitPrintln(Println stmt) throws StaticCheckException;
            R visitAssign(Assign stmt) throws StaticCheckException;
            R visitFieldAssign(FieldAssign stmt) throws StaticCheckException;
            R visitCall(Call stmt) throws StaticCheckException;
            R visitReturn(Return stmt) throws StaticCheckException;
        }

        static class If extends Stmt {
            final Expr cond;
            final List<Stmt> thenStmts;
            final List<Stmt> elseStmts;

            If(Expr cond, List<Stmt> thenStmts, List<Stmt> elseStmts) {
                this.cond = cond;
                this.thenStmts = thenStmts;
                this.elseStmts = elseStmts;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitIf(this);
            }
        }

        static class While extends Stmt {
            final Expr cond;
            final List<Stmt> stmts;

            While(Expr cond, List<Stmt> stmts) {
                this.cond = cond;
                this.stmts = stmts;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitWhile(this);
            }
        }

        static class Readln extends Stmt {
            final String id;

            Readln(String id) { this.id = id; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitReadln(this);
            }
        }

        static class Println extends Stmt {
            final Expr expr;

            Println(Expr expr) { this.expr = expr; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitPrintln(this);
            }
        }

        static class Assign extends Stmt {
            final String lhs;
            final Expr rhs;

            Assign(String lhs, Expr rhs) {
                this.lhs = lhs;
                this.rhs = rhs;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitAssign(this);
            }
        }

        static class FieldAssign extends Stmt {
            final Expr lhsExpr;
            final String lhsField;
            final Expr rhs;

            FieldAssign(Expr lhs, String lhsField, Expr rhs) {
                this.lhsExpr = lhs;
                this.lhsField = lhsField;
                this.rhs = rhs;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitFieldAssign(this);
            }
        }

        static class Call extends Stmt {
            final Expr callee;
            final List<Expr> args;

            Call(Expr callee, List<Expr> args) {
                this.callee = callee;
                this.args = args;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitCall(this);
            }
        }

        static class Return extends Stmt {
            final Expr expr;

            Return(Expr expr) { this.expr = expr; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitReturn(this);
            }
        }
    }

    abstract static class Expr {
        final String _class = this.getClass().getName();
        Type type; // Populated during StaticCheck

        abstract <R> R accept(Visitor<R> visitor) throws StaticCheckException;

        interface Visitor<R> {
            R visitStr(Str expr) throws StaticCheckException;
            R visitInt(Int expr) throws StaticCheckException;
            R visitBool(Bool expr) throws StaticCheckException;
            R visitId(Id expr) throws StaticCheckException;
            R visitUnary(Unary expr) throws StaticCheckException;
            R visitBinary(Binary expr) throws StaticCheckException;
            R visitDot(Dot expr) throws StaticCheckException;
            R visitCall(Call expr) throws StaticCheckException;
            R visitNew(New expr) throws StaticCheckException;
            R visitThis(This expr) throws StaticCheckException;
            R visitNull(Null expr) throws StaticCheckException;
        }

        static class Str extends Expr {
            final String value;

            Str(String value) { this.value = value; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitStr(this);
            }
        }

        static class Int extends Expr {
            Integer value;

            Int(Integer value) { this.value = value; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitInt(this);
            }
        }

        static class Bool extends Expr {
            final Boolean value;

            Bool(Boolean value) { this.value = value; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitBool(this);
            }
        }

        static class Id extends Expr {
            final String id;

            Id(String id) { this.id = id; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitId(this);
            }
        }

        static class Unary extends Expr {
            final Expr atom;
            final UnaryOp op;

            Unary(UnaryOp op, Expr atom) {
                this.atom = atom;
                this.op = op;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitUnary(this);
            }
        }

        static class Binary extends Expr {
            final Expr e1;
            final Expr e2;
            final BinaryOp op;

            Binary(BinaryOp op, Expr e1, Expr e2) {
                this.e1 = e1;
                this.e2 = e2;
                this.op = op;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitBinary(this);
            }
        }

        static class Dot extends Expr {
            Expr atom;
            String member;

            Dot(Expr atom, String member) {
                this.atom = atom;
                this.member = member;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitDot(this);
            }
        }

        static class Call extends Expr {
            Expr callee;
            List<Expr> args;

            Call(Expr callee, List<Expr> args) {
                this.callee = callee;
                this.args = args;
            }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitCall(this);
            }
        }

        static class New extends Expr {
            final String cname;

            New(String cname) { this.cname = cname; }

            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitNew(this);
            }
        }

        static class This extends Expr {
            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitThis(this);
            }
        }

        static class Null extends Expr {
            @Override
            <R> R accept(Visitor<R> visitor) throws StaticCheckException {
                return visitor.visitNull(this);
            }
        }

        static enum UnaryOp { NOT, NEG }

        static enum BinaryOp {
            PLUS,
            MINUS,
            MUL,
            DIV,
            LT,
            GT,
            LEQ,
            GEQ,
            EQ,
            NEQ,
            OR,
            AND
        }
    }

    abstract static class Type {
        final java.lang.String _class = this.getClass().getName();

        public abstract boolean equals(Object o);

        static class Int extends Type {
            @Override
            public boolean equals(Object o) {
                return o instanceof Int;
            }
        }

        static class Bool extends Type {
            @Override
            public boolean equals(Object o) {
                return o instanceof Bool;
            }
        }

        static class String extends Type {
            @Override
            public boolean equals(Object o) {
                return o instanceof String;
            }
        }

        static class Void extends Type {
            @Override
            public boolean equals(Object o) {
                return o instanceof Void;
            }
        }

        static class Class extends Type {
            final java.lang.String name;

            Class(java.lang.String name) { this.name = name; }

            @Override
            public boolean equals(Object o) {
                return o instanceof Class && name.equals(((Class)o).name);
            }
        }

        static class Function extends Type {
            final List<Type> paramTypes;
            final Type returnType;

            Function(Method method) {
                var paramTypes = new ArrayList<Type>();
                for (var var : method.params) {
                    paramTypes.add(var.type);
                }

                this.paramTypes = paramTypes;
                this.returnType = method.returnType;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof Function &&
                    paramTypes.equals(((Function)o).paramTypes) &&
                    returnType.equals(((Function)o).returnType);
            }
        }

        static class Null extends Type {
            @Override
            public boolean equals(Object o) {
                return o instanceof Null;
            }
        }
    }

    static class Var {
        final String _class = this.getClass().getName();
        final Type type;
        final String id;

        Var(Type type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
