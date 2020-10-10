package jelek;

import java.util.List;

class Ast {
    static class Program {
        final String _class = "Program";
        Class main;
        List<Class> classes;

        Program(Class main, List<Class> classes) {
            this.main = main;
            this.classes = classes;
        }
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

    static class Body {
        final String _class = "Body";
        final List<Var> vars;
        final List<Stmt> stmts;

        Body(List<Var> vars, List<Stmt> stmts) {
            this.vars = vars;
            this.stmts = stmts;
        }
    }

    static class Method {
        final String _class = "Method";
        final String id;
        final Type returnType;
        final List<Var> params;
        final Body body;

        Method(String id, Type returnType, List<Var> params, Body body) {
            this.id = id;
            this.returnType = returnType;
            this.params = params;
            this.body = body;
        }
    }

    abstract static class Stmt {
        static class If extends Stmt {
            final String _class = "If";
            final Expr cond;
            final List<Stmt> thenStmts;
            final List<Stmt> elseStmts;

            If(Expr cond, List<Stmt> thenStmts, List<Stmt> elseStmts) {
                this.cond = cond;
                this.thenStmts = thenStmts;
                this.elseStmts = elseStmts;
            }
        }

        static class While extends Stmt {
            final String _class = "While";
            final Expr cond;
            final List<Stmt> stmts;

            While(Expr cond, List<Stmt> stmts) {
                this.cond = cond;
                this.stmts = stmts;
            }
        }

        static class Readln extends Stmt {
            final String _class = "Readln";
            final String id;

            Readln(String id) { this.id = id; }
        }

        static class Println extends Stmt {
            final String _class = "Println";
            final Expr expr;

            Println(Expr expr) { this.expr = expr; }
        }

        static class Assign extends Stmt {
            final String _class = "Assign";
            final String assignee;
            final Expr expr;

            Assign(String assignee, Expr expr) {
                this.assignee = assignee;
                this.expr = expr;
            }
        }

        static class FieldAssign extends Stmt {
            final String _class = "FieldAssign";
            final Expr atom;
            final String field;
            final Expr expr;

            FieldAssign(Expr atom, String field, Expr expr) {
                this.atom = atom;
                this.field = field;
                this.expr = expr;
            }
        }

        static class Call extends Stmt {
            final String _class = "Call";
            final Expr caller;
            final List<Expr> args;

            Call(Expr caller, List<Expr> args) {
                this.caller = caller;
                this.args = args;
            }
        }

        static class Return extends Stmt {
            final String _class = "Return";
            final Expr expr;

            Return(Expr expr) { this.expr = expr; }
        }
    }

    abstract static class Expr {
        static class Str extends Expr {
            final String _class = "Str";
            final String value;

            Str(String value) { this.value = value; }
        }
        static class Int extends Expr {
            final String _class = "Int";
            Integer value;

            Int(Integer value) { this.value = value; }
        }
        static class Bool extends Expr {
            final String _class = "Bool";
            final Boolean value;

            Bool(Boolean value) { this.value = value; }
        }
        static class Id extends Expr {
            final String _class = "Id";
            final String id;

            Id(String id) { this.id = id; }
        }
        static class Unary extends Expr {
            final String _class = "Unary";
            final Expr e;
            final UnaryOp op;

            Unary(UnaryOp op, Expr e) {
                this.e = e;
                this.op = op;
            }
        }
        static class Binary extends Expr {
            final String _class = "Binary";
            final Expr e1;
            final Expr e2;
            final BinaryOp op;

            Binary(BinaryOp op, Expr e1, Expr e2) {
                this.e1 = e1;
                this.e2 = e2;
                this.op = op;
            }
        }
        static class Dot extends Expr {
            final String _class = "Dot";
            Expr atom;
            String id;

            Dot(Expr atom, String id) {
                this.atom = atom;
                this.id = id;
            }
        }
        static class Call extends Expr {
            final String _class = "Call";
            Expr atom;
            List<Expr> args;

            Call(Expr atom, List<Expr> args) {
                this.atom = atom;
                this.args = args;
            }
        }
        static class New extends Expr {
            final String _class = "New";
            final String cname;

            New(String cname) { this.cname = cname; }
        }
        static class This extends Expr { final String _class = "This"; }
        static class Null extends Expr { final String _class = "Null"; }
        static enum UnaryOp { NOT, NEG }
        static enum BinaryOp {
            PLUS,
            MINUS,
            STAR,
            SLASH,
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

    static class Type {
        final String _class = "Type";
        final String type;

        Type(String type) { this.type = type; }
    }

    static class Var {
        final String _class = "Var";
        final Type type;
        final String id;

        Var(Type type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
