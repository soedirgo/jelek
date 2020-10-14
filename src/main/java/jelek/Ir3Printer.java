package jelek;

import java.util.stream.Collectors;
import jelek.Ast.Expr.UnaryOp;
import jelek.StaticCheck.StaticCheckException;

class Ir3Printer implements Ir3.Stmt.Visitor<Void>, Ast.Expr.Visitor<String> {
    static void print(Ir3.Program ir3) {
        System.out.print("======= CData3 =======\n\n");
        for (var data : ir3.datas) {
            System.out.print("class " + data.cname + " {\n");
            for (var var : data.vars) {
                String varType = getSimpleType(var.type);
                System.out.print("    " + varType + " " + var.id + ";\n");
            }
            System.out.print("}\n\n");
        }
        System.out.print("======= CMtd3 =======\n\n");
        for (var method : ir3.methods) {
            System.out.println(getSimpleType(method.returnType) + " " +
                               method.name + "(" +
                               method.params.stream()
                                   .map(p -> getSimpleType(p.type) + " " + p.id)
                                   .collect(Collectors.joining(", ")) +
                               ") {");
            for (var var : method.vars) {
                System.out.println("    " + getSimpleType(var.type) + " " +
                                   var.id + ";");
            }
            System.out.println();
            for (var stmt : method.stmts) {
                Ir3Printer.print(stmt);
            }
            System.out.print("}\n\n");
        }
        System.out.print("=====fx== End of IR3 Program =======\n\n");
    }

    static String getSimpleType(Ast.Type type) {
        if (type instanceof Ast.Type.Class) {
            return ((Ast.Type.Class)type).name;
        } else {
            return type.getClass().getSimpleName();
        }
    }

    static void print(Ir3.Stmt stmt) { stmt.accept(new Ir3Printer()); }

    static String serialize(Ast.Expr expr) {
        try {
            return expr.accept(new Ir3Printer());
        } catch (StaticCheckException never) {
            throw new AssertionError();
        }
    }

    @Override
    public Void visitLabel(Ir3.Stmt.Label stmt) {
        System.out.println("L" + stmt.label + ":");

        return null;
    }

    @Override
    public Void visitIf(Ir3.Stmt.If stmt) {
        System.out.println("    if (" + serialize(stmt.cond) + ") goto L" +
                           stmt.label + ";");

        return null;
    }

    @Override
    public Void visitGoto(Ir3.Stmt.Goto stmt) {
        System.out.println("    goto L" + stmt.label + ";");

        return null;
    }

    @Override
    public Void visitReadln(Ir3.Stmt.Readln stmt) {
        System.out.println("    readln(" + stmt.id + ");");

        return null;
    }

    @Override
    public Void visitPrintln(Ir3.Stmt.Println stmt) {
        System.out.println("    println(" + serialize(stmt.expr) + ");");

        return null;
    }

    @Override
    public Void visitAssign(Ir3.Stmt.Assign stmt) {
        System.out.println("    " + stmt.lhs + " = " + serialize(stmt.rhs) +
                           ";");

        return null;
    }

    @Override
    public Void visitFieldAssign(Ir3.Stmt.FieldAssign stmt) {
        System.out.println("    " + serialize(stmt.lhsExpr) + "." +
                           stmt.lhsField + " = " + serialize(stmt.rhs) + ";");

        return null;
    }

    @Override
    public Void visitCall(Ir3.Stmt.Call stmt) {
        System.out.println("    " + stmt.id + "(" +
                           stmt.args.stream()
                               .map(Ir3Printer::serialize)
                               .collect(Collectors.joining(", ")) +
                           ");");

        return null;
    }

    @Override
    public Void visitReturn(Ir3.Stmt.Return stmt) {
        var expr = stmt.expr == null ? "" : (" " + serialize(stmt.expr));
        System.out.println("    return" + expr + ";");

        return null;
    }

    @Override
    public String visitStr(Ast.Expr.Str expr) {
        return "\"" + expr.value + "\"";
    }

    @Override
    public String visitInt(Ast.Expr.Int expr) {
        return expr.value.toString();
    }

    @Override
    public String visitBool(Ast.Expr.Bool expr) {
        return expr.value.toString();
    }

    @Override
    public String visitId(Ast.Expr.Id expr) {
        return expr.id;
    }

    @Override
    public String visitUnary(Ast.Expr.Unary expr) {
        if (expr.op == UnaryOp.NEG) {
            return "-(" + serialize(expr.atom) + ")";
        } else {
            return "!(" + serialize(expr.atom) + ")";
        }
    }

    @Override
    public String visitBinary(Ast.Expr.Binary expr) {
        String op;
        switch (expr.op) {
        case PLUS:
            op = "+";
            break;
        case MINUS:
            op = "-";
            break;
        case MUL:
            op = "*";
            break;
        case DIV:
            op = "/";
            break;
        case LT:
            op = "<";
            break;
        case GT:
            op = ">";
            break;
        case LEQ:
            op = "<=";
            break;
        case GEQ:
            op = ">=";
            break;
        case EQ:
            op = "==";
            break;
        case NEQ:
            op = "!=";
            break;
        case OR:
            op = "||";
            break;
        case AND:
            op = "&&";
            break;
        default:
            op = "💩";
        }

        return "(" + serialize(expr.e1) + ") " + op + " (" +
            serialize(expr.e2) + ")";
    }

    @Override
    public String visitDot(Ast.Expr.Dot expr) {
        return serialize(expr.atom) + "." + expr.member;
    }

    @Override
    public String visitCall(Ast.Expr.Call expr) {
        return serialize(expr.callee) + "(" +
            expr.args.stream()
                .map(Ir3Printer::serialize)
                .collect(Collectors.joining(", ")) +
            ")";
    }

    @Override
    public String visitNew(Ast.Expr.New expr) {
        return "new " + expr.cname + "()";
    }

    @Override
    public String visitThis(Ast.Expr.This expr) {
        return "this";
    }

    @Override
    public String visitNull(Ast.Expr.Null expr) {
        return "NULL";
    }
}
