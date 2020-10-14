package jelek;

import java.util.ArrayList;
import java.util.HashMap;
import jelek.StaticCheck.StaticCheckException;

class Ir3Gen {
    static int labelCounter = 0;
    static int tempCounter = 0;
    static HashMap<String, Ir3.Data> datas = new HashMap<>();
    static HashMap<String, Ir3.Method> methods = new HashMap<>();

    static Ast.Expr.Id genTemp(Ast.Expr expr, Ir3.Method method) {
        var temp = new Ast.Var(expr.type, "_t" + tempCounter++);
        method.vars.add(temp);
        method.varMap.put(temp.id, temp.id);
        method.stmts.add(new Ir3.Stmt.Assign(temp.id, expr));
        var tempExpr = new Ast.Expr.Id(temp.id);
        tempExpr.type = temp.type;
        return tempExpr;
    }

    static Ir3.Program gen(Ast.Program program) {
        // Populate datas
        for (var class_ : program.classes) {
            var data = new Ir3.Data(class_.name, class_.vars);
            datas.put(class_.name, data);
        }

        // Populate methods
        for (var class_ : program.classes) {
            for (var method : class_.methods) {
                String name;
                if (method.id == "main") {
                    name = method.id;
                } else {
                    name = "%" + class_.name + "_" + method.id;
                }
                methods.put(name, new Ir3.Method(name, method.returnType));
            }
        }

        for (var class_ : program.classes) {
            genClass(class_);
        }

        return new Ir3.Program(new ArrayList<>(datas.values()),
                               new ArrayList<>(methods.values()));
    }

    static void genClass(Ast.Class class_) {
        for (var method : class_.methods) {
            String methodName;
            if (method.id == "main") {
                methodName = method.id;
            } else {
                methodName = "%" + class_.name + "_" + method.id;
            }
            var ir3Method = methods.get(methodName);
            var nameCounter = new HashMap<String, Integer>();

            ir3Method.params.add(
                new Ast.Var(new Ast.Type.Class(class_.name), "this"));
            ir3Method.params.addAll(method.params);
            for (var param : method.params) {
                nameCounter.put(param.id, 1);
                ir3Method.varMap.put(param.id, param.id);
            }

            for (var var : method.vars) {
                String name;
                Integer count = nameCounter.getOrDefault(var.id, 0);
                if (count == 0) {
                    name = var.id;
                    nameCounter.put(var.id, 1);
                } else {
                    name = var.id + "$" + count;
                    nameCounter.put(var.id, count + 1);
                }

                ir3Method.vars.add(new Ast.Var(var.type, name));
                ir3Method.varMap.put(var.id, name);
            }

            for (var stmt : method.stmts) {
                StmtGen.gen(stmt, ir3Method);
            }

            labelCounter = 0;
            tempCounter = 0;
        }
    }

    static class StmtGen implements Ast.Stmt.Visitor<Void> {
        final Ir3.Method method;

        StmtGen(Ir3.Method method) { this.method = method; }

        static void gen(Ast.Stmt stmt, Ir3.Method method) {
            try {
                stmt.accept(new StmtGen(method));
            } catch (StaticCheckException never) {
                throw new AssertionError();
            }
        }

        @Override
        public Void visitIf(Ast.Stmt.If stmt) {
            var thenLabel = new Ir3.Stmt.Label(labelCounter++);
            var endLabel = new Ir3.Stmt.Label(labelCounter++);
            var cond = ExprGen.gen(stmt.cond, method);
            method.stmts.add(new Ir3.Stmt.If(cond, thenLabel.label));
            for (var _stmt : stmt.elseStmts) {
                StmtGen.gen(_stmt, method);
            }
            method.stmts.add(new Ir3.Stmt.Goto(endLabel.label));
            method.stmts.add(thenLabel);
            for (var _stmt : stmt.thenStmts) {
                StmtGen.gen(_stmt, method);
            }
            method.stmts.add(endLabel);

            return null;
        }

        @Override
        public Void visitWhile(Ast.Stmt.While stmt) {
            var bodyLabel = new Ir3.Stmt.Label(labelCounter++);
            var condLabel = new Ir3.Stmt.Label(labelCounter++);
            method.stmts.add(new Ir3.Stmt.Goto(condLabel.label));
            method.stmts.add(bodyLabel);
            for (var _stmt : stmt.stmts) {
                StmtGen.gen(_stmt, method);
            }
            method.stmts.add(condLabel);
            var cond = ExprGen.gen(stmt.cond, method);
            method.stmts.add(new Ir3.Stmt.If(cond, bodyLabel.label));

            return null;
        }

        @Override
        public Void visitReadln(Ast.Stmt.Readln stmt) {
            method.stmts.add(new Ir3.Stmt.Readln(method.varMap.get(stmt.id)));

            return null;
        }

        @Override
        public Void visitPrintln(Ast.Stmt.Println stmt) {
            var expr = ExprGen.gen(stmt.expr, method);
            method.stmts.add(new Ir3.Stmt.Println(expr));

            return null;
        }

        @Override
        public Void visitAssign(Ast.Stmt.Assign stmt) {
            var rhs = ExprGen.gen(stmt.rhs, method);

            if (method.varMap.containsKey(stmt.lhs)) {
                method.stmts.add(
                    new Ir3.Stmt.Assign(method.varMap.get(stmt.lhs), rhs));
            } else {
                // Class field
                var thisExpr = new Ast.Expr.This();
                thisExpr.type = method.params.get(0).type;
                method.stmts.add(
                    new Ir3.Stmt.FieldAssign(thisExpr, stmt.lhs, rhs));
            }

            return null;
        }

        @Override
        public Void visitFieldAssign(Ast.Stmt.FieldAssign stmt) {
            var rhsExpr = ExprGen.gen(stmt.rhs, method);
            var lhsExpr = ExprGen.gen(stmt.lhsExpr, method);

            method.stmts.add(
                new Ir3.Stmt.FieldAssign(lhsExpr, stmt.lhsField, rhsExpr));

            return null;
        }

        @Override
        public Void visitCall(Ast.Stmt.Call stmt) {
            String methodName;
            var args = new ArrayList<Ast.Expr>();
            if (stmt.callee instanceof Ast.Expr.Id) {
                var className =
                    ((Ast.Type.Class)method.params.get(0).type).name;
                methodName =
                    "%" + className + "_" + ((Ast.Expr.Id)stmt.callee).id;

                var thisExpr = new Ast.Expr.This();
                thisExpr.type = method.params.get(0).type;
                args.add(thisExpr);
            } else if (stmt.callee instanceof Ast.Expr.Dot) {
                var callee = (Ast.Expr.Dot)stmt.callee;
                var atom = ExprGen.gen(callee.atom, method);
                methodName = "%" + ((Ast.Type.Class)atom.type).name + "_" +
                             callee.member;

                args.add(atom);
            } else {
                throw new AssertionError();
            }

            for (var arg : stmt.args) {
                args.add(ExprGen.gen(arg, method));
            }

            method.stmts.add(new Ir3.Stmt.Call(methodName, args));

            return null;
        }

        @Override
        public Void visitReturn(Ast.Stmt.Return stmt) {
            Ast.Expr expr = null;
            if (stmt.expr != null) {
                expr = ExprGen.gen(stmt.expr, method);
                expr = genTemp(expr, method);
            }
            method.stmts.add(new Ir3.Stmt.Return(expr));

            return null;
        }
    }

    static class ExprGen implements Ast.Expr.Visitor<Ast.Expr> {
        final Ir3.Method method;

        ExprGen(Ir3.Method method) { this.method = method; }

        static Ast.Expr gen(Ast.Expr expr, Ir3.Method method) {
            try {
                return expr.accept(new ExprGen(method));
            } catch (StaticCheckException never) {
                throw new AssertionError();
            }
        }

        @Override
        public Ast.Expr visitStr(Ast.Expr.Str expr) {
            return expr;
        }

        @Override
        public Ast.Expr visitInt(Ast.Expr.Int expr) {
            return expr;
        }

        @Override
        public Ast.Expr visitBool(Ast.Expr.Bool expr) {
            return expr;
        }

        @Override
        public Ast.Expr visitId(Ast.Expr.Id expr) {
            Ast.Expr.Id idExpr;
            if (method.varMap.containsKey(expr.id)) {
                idExpr = new Ast.Expr.Id(method.varMap.get(expr.id));
                idExpr.type = expr.type;
            } else {
                // Class field
                var thisExpr = new Ast.Expr.This();
                thisExpr.type = method.params.get(0).type;
                var dotExpr = new Ast.Expr.Dot(thisExpr, expr.id);
                dotExpr.type = expr.type;

                idExpr = genTemp(dotExpr, method);
            }

            return idExpr;
        }

        @Override
        public Ast.Expr visitUnary(Ast.Expr.Unary expr) {
            var atom = ExprGen.gen(expr.atom, method);
            var unaryExpr = new Ast.Expr.Unary(expr.op, atom);
            unaryExpr.type = expr.type;

            return genTemp(unaryExpr, method);
        }

        @Override
        public Ast.Expr visitBinary(Ast.Expr.Binary expr) {
            var e1 = ExprGen.gen(expr.e1, method);
            var e2 = ExprGen.gen(expr.e2, method);
            var binaryExpr = new Ast.Expr.Binary(expr.op, e1, e2);
            binaryExpr.type = expr.type;

            return genTemp(binaryExpr, method);
        }

        @Override
        public Ast.Expr visitDot(Ast.Expr.Dot expr) {
            var atom = ExprGen.gen(expr.atom, method);
            var dotExpr = new Ast.Expr.Dot(atom, expr.member);
            dotExpr.type = expr.type;

            return genTemp(dotExpr, method);
        }

        @Override
        public Ast.Expr visitCall(Ast.Expr.Call expr) {
            var callee = expr.callee;
            var args = new ArrayList<Ast.Expr>();
            if (expr.callee instanceof Ast.Expr.Id) {
                var className =
                    ((Ast.Type.Class)method.params.get(0).type).name;
                var methodName =
                    "%" + className + "_" + ((Ast.Expr.Id)expr.callee).id;
                callee = new Ast.Expr.Id(methodName);
                callee.type = expr.callee.type;

                var thisExpr = new Ast.Expr.This();
                thisExpr.type = method.params.get(0).type;
                args.add(thisExpr);
            } else if (expr.callee instanceof Ast.Expr.Dot) {
                var atom = ExprGen.gen(((Ast.Expr.Dot)callee).atom, method);
                var methodName = "%" + ((Ast.Type.Class)atom.type).name + "_" +
                                 ((Ast.Expr.Dot)callee).member;
                callee = new Ast.Expr.Id(methodName);
                callee.type = expr.callee.type;

                args.add(atom);
            } else {
                throw new AssertionError();
            }
            for (var arg : expr.args) {
                args.add(ExprGen.gen(arg, method));
            }

            var callExpr = new Ast.Expr.Call(callee, args);
            callExpr.type = expr.type;

            return genTemp(callExpr, method);
        }

        @Override
        public Ast.Expr visitNew(Ast.Expr.New expr) {
            return genTemp(expr, method);
        }

        @Override
        public Ast.Expr visitThis(Ast.Expr.This expr) {
            return expr;
        }

        @Override
        public Ast.Expr visitNull(Ast.Expr.Null expr) {
            return expr;
        }
    }
}
