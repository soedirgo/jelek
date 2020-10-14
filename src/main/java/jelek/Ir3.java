package jelek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jelek.Ast.Expr;
import jelek.Ast.Type;
import jelek.Ast.Var;

class Ir3 {
    static class Program {
        List<Data> datas;
        List<Method> methods;

        Program(List<Data> datas, List<Method> methods) {
            this.datas = datas;
            this.methods = methods;
        }
    }

    static class Data {
        String cname;
        List<Var> vars;

        Data(String cname, List<Var> vars) {
            this.cname = cname;
            this.vars = vars;
        }
    }

    static class Method {
        Type returnType;
        String name;
        List<Var> params = new ArrayList<>();
        List<Var> vars = new ArrayList<>();
        HashMap<String, String> varMap = new HashMap<>();
        List<Stmt> stmts = new ArrayList<>();

        Method(String name, Type returnType) {
            this.name = name;
            this.returnType = returnType;
        }
    }

    abstract static class Stmt {
        final String _class = this.getClass().getName();

        abstract <R> R accept(Visitor<R> visitor);

        interface Visitor<R> {
            R visitLabel(Label stmt);
            R visitIf(If stmt);
            R visitGoto(Goto stmt);
            R visitReadln(Readln stmt);
            R visitPrintln(Println stmt);
            R visitAssign(Assign stmt);
            R visitFieldAssign(FieldAssign stmt);
            R visitCall(Call stmt);
            R visitReturn(Return stmt);
        }

        static class Label extends Stmt {
            int label;

            Label(int label) { this.label = label; }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitLabel(this);
            }
        }

        static class If extends Stmt {
            Expr cond;
            int label;

            If(Expr cond, int label) {
                this.cond = cond;
                this.label = label;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitIf(this);
            }
        }

        static class Goto extends Stmt {
            int label;

            Goto(int label) { this.label = label; }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitGoto(this);
            }
        }

        static class Readln extends Stmt {
            String id;

            Readln(String id) { this.id = id; }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitReadln(this);
            }
        }

        static class Println extends Stmt {
            Expr expr;

            Println(Expr expr) { this.expr = expr; }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitPrintln(this);
            }
        }

        static class Assign extends Stmt {
            String lhs;
            Expr rhs;

            Assign(String lhs, Expr rhs) {
                this.lhs = lhs;
                this.rhs = rhs;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitAssign(this);
            }
        }

        static class FieldAssign extends Stmt {
            Expr lhsExpr;
            String lhsField;
            Expr rhs;

            FieldAssign(Expr lhsExpr, String lhsField, Expr rhs) {
                this.lhsExpr = lhsExpr;
                this.lhsField = lhsField;
                this.rhs = rhs;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitFieldAssign(this);
            }
        }

        static class Call extends Stmt {
            String id;
            List<Expr> args;

            Call(String id, List<Expr> args) {
                this.id = id;
                this.args = args;
            }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitCall(this);
            }
        }

        static class Return extends Stmt {
            Expr expr;

            Return(Expr expr) { this.expr = expr; }

            @Override
            <R> R accept(Visitor<R> visitor) {
                return visitor.visitReturn(this);
            }
        }
    }
}
