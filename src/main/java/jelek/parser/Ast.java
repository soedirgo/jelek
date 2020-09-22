package jelek.parser;
import java.util.List;

public class Ast {
    public static class Program {
        final String _class = "Program";
        Class main;
        List<Class> classes;

        public Program(Class main, List<Class> classes) {
            this.main = main;
            this.classes = classes;
        }
    }

    public static class Class {
        final String _class = "Class";
        final String name;
        final List<Var> vars;
        final List<Method> methods;

        public Class(String name, List<Var> vars, List<Method> methods) {
            this.name = name;
            this.vars = vars;
            this.methods = methods;
        }
    }

    public static class Body {
        final String _class = "Body";
        final List<Var> vars;
        final List<Stmt> stmts;

        public Body(List<Var> vars, List<Stmt> stmts) {
            this.vars = vars;
            this.stmts = stmts;
        }
    }

    public static class Method {
        final String _class = "Method";
        final String id;
        final Type returnType;
        final List<Var> params;
        final Body body;

        public Method(String id, Type returnType, List<Var> params, Body body) {
            this.id = id;
            this.returnType = returnType;
            this.params = params;
            this.body = body;
        }
    }

    public abstract static class Stmt {
        public static class If extends Stmt {
            final String _class = "If";
            final Expr cond;
            final List<Stmt> thenStmts;
            final List<Stmt> elseStmts;

            public If(Expr cond, List<Stmt> thenStmts, List<Stmt> elseStmts) {
                this.cond = cond;
                this.thenStmts = thenStmts;
                this.elseStmts = elseStmts;
            }
        }

        public static class While extends Stmt {
            final String _class = "While";
            final Expr cond;
            final List<Stmt> stmts;

            public While(Expr cond, List<Stmt> stmts) {
                this.cond = cond;
                this.stmts = stmts;
            }
        }

        public static class Readln extends Stmt {
            final String _class = "Readln";
            final String id;

            public Readln(String id) { this.id = id; }
        }

        public static class Println extends Stmt {
            final String _class = "Println";
            final Expr expr;

            public Println(Expr expr) { this.expr = expr; }
        }

        public static class Assign extends Stmt {
            final String _class = "Assign";
            final String assignee;
            final Expr expr;

            public Assign(String assignee, Expr expr) {
                this.assignee = assignee;
                this.expr = expr;
            }
        }

        public static class FieldAssign extends Stmt {
            final String _class = "FieldAssign";
            final Expr atom;
            final String field;
            final Expr expr;

            public FieldAssign(Expr atom, String field, Expr expr) {
                this.atom = atom;
                this.field = field;
                this.expr = expr;
            }
        }

        public static class Call extends Stmt {
            final String _class = "Call";
            final Expr caller;
            final List<Expr> args;

            public Call(Expr caller, List<Expr> args) {
                this.caller = caller;
                this.args = args;
            }
        }

        public static class Return extends Stmt {
            final String _class = "Return";
            final Expr expr;

            public Return(Expr expr) { this.expr = expr; }
        }
    }

    public abstract static class Expr {
        public static class Str extends Expr {
            final String _class = "Str";
            final String value;

            public Str(String value) { this.value = value; }
        }
        public static class Int extends Expr {
            final String _class = "Int";
            Integer value;

            public Int(Integer value) { this.value = value; }
        }
        public static class Bool extends Expr {
            final String _class = "Bool";
            final Boolean value;

            public Bool(Boolean value) { this.value = value; }
        }
        public static class Id extends Expr {
            final String _class = "Id";
            final String id;

            public Id(String id) { this.id = id; }
        }
        public static class Unary extends Expr {
            final String _class = "Unary";
            final Expr e;
            final UnaryOp op;

            public Unary(UnaryOp op, Expr e) {
                this.e = e;
                this.op = op;
            }
        }
        public static class Binary extends Expr {
            final String _class = "Binary";
            final Expr e1;
            final Expr e2;
            final BinaryOp op;

            public Binary(BinaryOp op, Expr e1, Expr e2) {
                this.e1 = e1;
                this.e2 = e2;
                this.op = op;
            }
        }
        public static class Dot extends Expr {
            final String _class = "Dot";
            Expr atom;
            String id;

            public Dot(Expr atom, String id) {
                this.atom = atom;
                this.id = id;
            }
        }
        public static class Call extends Expr {
            final String _class = "Call";
            Expr atom;
            List<Expr> args;

            public Call(Expr atom, List<Expr> args) {
                this.atom = atom;
                this.args = args;
            }
        }
        public static class New extends Expr {
            final String _class = "New";
            final String cname;

            public New(String cname) { this.cname = cname; }
        }
        public static class This extends Expr { final String _class = "This"; }
        public static class Null extends Expr { final String _class = "Null"; }
        public static enum UnaryOp { NOT, NEG }
        public static enum BinaryOp {
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

    public static class Type {
        final String _class = "Type";
        final String type;

        public Type(String type) { this.type = type; }
    }

    public static class Var {
        final String _class = "Var";
        final Type type;
        final String id;

        public Var(Type type, String id) {
            this.type = type;
            this.id = id;
        }
    }
}
