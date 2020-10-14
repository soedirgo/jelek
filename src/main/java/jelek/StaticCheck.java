package jelek;

import java.util.ArrayList;
import java.util.HashMap;

class StaticCheck {
    static HashMap<String, ClassDesc> classDescs = new HashMap<>();

    static void run(Ast.Program program) throws StaticCheckException {
        init(program);

        for (var class_ : program.classes) {
            checkClass(class_);
        }
    }

    // Guarantees after this pass:
    //
    // 1. No duplicate class names
    // 2. No duplicate var names within a class
    // 3. No duplicate method names within a class
    // 4. No duplicate param names in a method signature
    static void init(Ast.Program program) throws StaticCheckException {
        // Populate classes
        for (var class_ : program.classes) {
            if (classDescs.containsKey(class_.name)) {
                throw new StaticCheckException(
                    String.format("Duplicate class name '%s'", class_.name));
            }

            ClassDesc classDesc = new ClassDesc(class_.name);
            classDescs.put(class_.name, classDesc);
        }

        // Populate class members
        for (var class_ : program.classes) {
            ClassDesc classDesc = classDescs.get(class_.name);

            // Populate vars
            for (var var : class_.vars) {
                if (classDesc.vars.containsKey(var.id)) {
                    throw new StaticCheckException(String.format(
                        "Duplicate var declaration '%s' in class '%s'", var.id,
                        class_.name));
                }

                if (var.type instanceof Ast.Type.Class &&
                    !classDescs.containsKey(((Ast.Type.Class)var.type).name)) {
                    throw new StaticCheckException(String.format(
                        "Invalid var type '%s' for var '%s' in class '%s'",
                        ((Ast.Type.Class)var.type).name, var.id, class_.name));
                }

                classDesc.vars.put(var.id, var.type);
            }

            // Populate methods
            for (var method : class_.methods) {
                if (classDesc.methods.containsKey(method.id)) {
                    throw new StaticCheckException(String.format(
                        "Duplicate method declaration '%s' in class '%s'",
                        method.id, class_.name));
                }

                var params = new HashMap<String, Ast.Type>();

                for (var param : method.params) {
                    if (params.containsKey(param.id)) {
                        throw new StaticCheckException(String.format(
                            "Duplicate param name '%s' in method '%s' of class '%s'",
                            param.id, method.id, class_.name));
                    }

                    if (param.type instanceof Ast.Type.Class &&
                        !classDescs.containsKey(
                            ((Ast.Type.Class)param.type).name)) {
                        throw new StaticCheckException(String.format(
                            "Invalid param type '%s' for param '%s' in method '%s' of class '%s'",
                            ((Ast.Type.Class)param.type).name, param.id,
                            method.id, class_.name));
                    }

                    params.put(param.id, param.type);
                }

                classDesc.methods.put(method.id, new Ast.Type.Function(method));
            }
        }
    }

    static void checkClass(Ast.Class class_) throws StaticCheckException {
        var classDesc = classDescs.get(class_.name);
        var env = new Env(null);

        for (var e : classDesc.methods.entrySet()) {
            env.put(e.getKey(), e.getValue());
        }

        // Variables shadow methods when their names collide
        for (var e : classDesc.vars.entrySet()) {
            env.put(e.getKey(), e.getValue());
        }

        env.put("this", new Ast.Type.Class(class_.name));

        for (var method : class_.methods) {
            checkMethod(method, env);
        }
    }

    static void checkMethod(Ast.Method method, Env parentEnv)
        throws StaticCheckException {
        var env = new Env(parentEnv);

        for (var var : method.params) {
            env.put(var.id, var.type);
        }

        env.put("Ret", method.returnType);

        for (var var : method.vars) {
            if (var.type instanceof Ast.Type.Class &&
                !classDescs.containsKey(((Ast.Type.Class)var.type).name)) {
                throw new StaticCheckException(String.format(
                    "Invalid variable type '%s' for variable '%s' in method '%s'",
                    ((Ast.Type.Class)var.type).name, var.id, method.id));
            }

            env.put(var.id, var.type);
        }

        Ast.Type lastReturnStmtType = new Ast.Type.Void();
        for (var stmt : method.stmts) {
            var type = StmtCheck.check(stmt, env);
            if (stmt instanceof Ast.Stmt.Return) {
                lastReturnStmtType = type;
            }
        }

        if (lastReturnStmtType.getClass() != method.returnType.getClass()) {
            throw new StaticCheckException(String.format(
                "Type of method body '%s' does not match return type '%s'",
                lastReturnStmtType.getClass().getSimpleName(),
                method.returnType.getClass().getSimpleName()));
        }
    }

    static class StmtCheck implements Ast.Stmt.Visitor<Ast.Type> {
        final Env env;

        StmtCheck(Env env) { this.env = new Env(env); }

        static Ast.Type check(Ast.Stmt stmt, Env env)
            throws StaticCheckException {
            return stmt.accept(new StmtCheck(env));
        }

        @Override
        public Ast.Type visitIf(Ast.Stmt.If stmt) throws StaticCheckException {
            var condType = ExprCheck.check(stmt.cond, env);
            if (!(condType instanceof Ast.Type.Bool)) {
                throw new StaticCheckException(String.format(
                    "If statement condition type '%s' is not Bool",
                    condType.getClass().getSimpleName()));
            }

            for (var _stmt : stmt.thenStmts) {
                StmtCheck.check(_stmt, env);
            }

            for (var _stmt : stmt.elseStmts) {
                StmtCheck.check(_stmt, env);
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitWhile(Ast.Stmt.While stmt)
            throws StaticCheckException {
            var condType = ExprCheck.check(stmt.cond, env);
            if (!(condType instanceof Ast.Type.Bool)) {
                throw new StaticCheckException(String.format(
                    "While statement condition type '%s' is not Bool",
                    condType.getClass().getSimpleName()));
            }

            for (var _stmt : stmt.stmts) {
                StmtCheck.check(_stmt, env);
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitReadln(Ast.Stmt.Readln stmt)
            throws StaticCheckException {
            var varType = env.get(stmt.id);
            if (!(varType instanceof Ast.Type.Int ||
                  varType instanceof Ast.Type.Bool ||
                  varType instanceof Ast.Type.String)) {
                throw new StaticCheckException(
                    String.format("Cannot read into a variable of type '%s'",
                                  varType.getClass().getSimpleName()));
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitPrintln(Ast.Stmt.Println stmt)
            throws StaticCheckException {
            var exprType = ExprCheck.check(stmt.expr, env);
            if (!(exprType instanceof Ast.Type.Int ||
                  exprType instanceof Ast.Type.Bool ||
                  exprType instanceof Ast.Type.String)) {
                throw new StaticCheckException(
                    String.format("Cannot print a variable of type '%s'",
                                  exprType.getClass().getSimpleName()));
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitAssign(Ast.Stmt.Assign stmt)
            throws StaticCheckException {
            var lhsType = env.get(stmt.lhs);
            var rhsType = ExprCheck.check(stmt.rhs, env);

            if (lhsType.getClass() != rhsType.getClass()) {
                throw new StaticCheckException(String.format(
                    "Cannot assign a value of type '%s' to a variable of type '%s'",
                    rhsType.getClass().getSimpleName(),
                    lhsType.getClass().getSimpleName()));
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitFieldAssign(Ast.Stmt.FieldAssign stmt)
            throws StaticCheckException {
            var lhsType = ExprCheck.check(
                new Ast.Expr.Dot(stmt.lhsExpr, stmt.lhsField), env);
            var rhsType = ExprCheck.check(stmt.rhs, env);

            if (lhsType.getClass() != rhsType.getClass()) {
                throw new StaticCheckException(String.format(
                    "Cannot assign a value of type '%s' to a variable of type '%s'",
                    rhsType.getClass().getSimpleName(),
                    lhsType.getClass().getSimpleName()));
            }

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitCall(Ast.Stmt.Call stmt)
            throws StaticCheckException {
            var callee = stmt.callee;
            var args = stmt.args;

            var callExpr = new Ast.Expr.Call(callee, args);
            ExprCheck.check(callExpr, env);

            return new Ast.Type.Void();
        }

        @Override
        public Ast.Type visitReturn(Ast.Stmt.Return stmt)
            throws StaticCheckException {
            var expr = stmt.expr;
            var returnType = env.get("Ret");

            if (expr == null) {
                if (!(returnType instanceof Ast.Type.Void)) {
                    throw new StaticCheckException(
                        "Must return a value in a method returning non-Void");
                }
            } else {
                var exprType = ExprCheck.check(expr, env);
                if (exprType.getClass() != returnType.getClass()) {
                    throw new StaticCheckException(String.format(
                        "Type of return statement '%s' is not equal to return type '%s'",
                        exprType.getClass().getSimpleName(),
                        returnType.getClass().getSimpleName()));
                }
            }

            return returnType;
        }
    }

    static class ExprCheck implements Ast.Expr.Visitor<Ast.Type> {
        final Env env;

        ExprCheck(Env env) { this.env = new Env(env); }

        static Ast.Type check(Ast.Expr expr, Env env)
            throws StaticCheckException {
            return expr.accept(new ExprCheck(env));
        }

        @Override
        public Ast.Type visitStr(Ast.Expr.Str expr) {
            expr.type = new Ast.Type.String();
            return expr.type;
        }

        @Override
        public Ast.Type visitInt(Ast.Expr.Int expr) {
            expr.type = new Ast.Type.Int();
            return expr.type;
        }

        @Override
        public Ast.Type visitBool(Ast.Expr.Bool expr) {
            expr.type = new Ast.Type.Bool();
            return expr.type;
        }

        @Override
        public Ast.Type visitId(Ast.Expr.Id expr) throws StaticCheckException {
            expr.type = env.get(expr.id);
            return expr.type;
        }

        @Override
        public Ast.Type visitUnary(Ast.Expr.Unary expr)
            throws StaticCheckException {
            var atomType = ExprCheck.check(expr.atom, env);

            switch (expr.op) {
            case NEG:
                if (!(atomType instanceof Ast.Type.Int)) {
                    throw new StaticCheckException(String.format(
                        "Attempt to perform integer negation on a '%s'",
                        atomType.getClass().getSimpleName()));
                }
                break;
            case NOT:
                if (!(atomType instanceof Ast.Type.Bool)) {
                    throw new StaticCheckException(String.format(
                        "Attempt to perform boolean negation on a '%s'",
                        atomType.getClass().getSimpleName()));
                }
                break;
            }

            expr.type = atomType;
            return expr.type;
        }

        @Override
        public Ast.Type visitBinary(Ast.Expr.Binary expr)
            throws StaticCheckException {
            var e1Type = ExprCheck.check(expr.e1, env);
            var e2Type = ExprCheck.check(expr.e2, env);

            switch (expr.op) {
            case PLUS:
            case MINUS:
            case MUL:
            case DIV:
                if (!(e1Type instanceof Ast.Type.Int) ||
                    !(e2Type instanceof Ast.Type.Int)) {
                    throw new StaticCheckException(
                        "Attempt to perform arithmetic operation on a non-Int");
                }

                expr.type = new Ast.Type.Int();
                break;
            case LT:
            case GT:
            case LEQ:
            case GEQ:
                if (!(e1Type instanceof Ast.Type.Int) ||
                    !(e2Type instanceof Ast.Type.Int)) {
                    throw new StaticCheckException(
                        "Attempt to perform comparison operation on a non-Int");
                }

                expr.type = new Ast.Type.Bool();
                break;
            case EQ:
            case NEQ:
                if (e1Type.getClass() != e2Type.getClass()) {
                    throw new StaticCheckException(
                        "Attempt to perform equality operation on incompatible types");
                }

                expr.type = new Ast.Type.Bool();
                break;
            case AND:
            case OR:
                if (!(e1Type instanceof Ast.Type.Bool) ||
                    !(e2Type instanceof Ast.Type.Bool)) {
                    throw new StaticCheckException(
                        "Attempt to perform boolean operation on a non-Bool");
                }

                expr.type = new Ast.Type.Bool();
                break;
            }

            return expr.type;
        }

        @Override
        public Ast.Type visitDot(Ast.Expr.Dot expr)
            throws StaticCheckException {
            var atomType = ExprCheck.check(expr.atom, env);
            if (!(atomType instanceof Ast.Type.Class)) {
                throw new StaticCheckException(
                    String.format("Cannot access field of type '%s'",
                                  atomType.getClass().getSimpleName()));
            }

            var className = ((Ast.Type.Class)atomType).name;
            var classDesc = classDescs.get(className);
            if (!(classDesc.vars.containsKey(expr.member))) {
                throw new StaticCheckException(String.format(
                    "Class '%s' has no field '%s'", className, expr.member));
            }

            expr.type = classDesc.vars.get(expr.member);
            return expr.type;
        }

        @Override
        public Ast.Type visitCall(Ast.Expr.Call expr)
            throws StaticCheckException {
            var calleeType = (Ast.Type.Function)expr.callee.type;
            if (expr.callee instanceof Ast.Expr.Id) {
                // LocalCall
                var callee = (Ast.Expr.Id)expr.callee;
                calleeType = (Ast.Type.Function)env.get(callee.id);
                if (!(calleeType instanceof Ast.Type.Function)) {
                    throw new StaticCheckException(
                        String.format("'%s' is not a method", callee.id));
                }
            } else if (expr.callee instanceof Ast.Expr.Dot) {
                // GlobalCall
                var callee = (Ast.Expr.Dot)expr.callee;
                var atomType = ExprCheck.check(callee.atom, env);
                if (!(atomType instanceof Ast.Type.Class)) {
                    throw new StaticCheckException(
                        String.format("Cannot access field of type '%s'",
                                      atomType.getClass().getSimpleName()));
                }

                var className = ((Ast.Type.Class)atomType).name;
                var classDesc = classDescs.get(className);
                if (!(classDesc.methods.containsKey(callee.member))) {
                    throw new StaticCheckException(
                        String.format("Class '%s' has no method '%s'",
                                      className, callee.member));
                }

                calleeType = classDesc.methods.get(callee.member);
            } else {
                throw new AssertionError();
            }

            var argTypes = new ArrayList<Ast.Type>();
            for (var arg : expr.args) {
                argTypes.add(ExprCheck.check(arg, env));
            }
            if (!argTypes.equals(calleeType.paramTypes)) {
                throw new StaticCheckException(
                    "Attempt to call method with incompatible argument type");
            }

            expr.callee.type = calleeType;
            expr.type = calleeType.returnType;
            return expr.type;
        }

        @Override
        public Ast.Type visitNew(Ast.Expr.New expr)
            throws StaticCheckException {
            if (!classDescs.containsKey(expr.cname)) {
                throw new StaticCheckException(
                    String.format("No such class '%s'", expr.cname));
            }

            expr.type = new Ast.Type.Class(expr.cname);
            return expr.type;
        }

        @Override
        public Ast.Type visitThis(Ast.Expr.This expr)
            throws StaticCheckException {
            expr.type = env.get("this");
            return expr.type;
        }

        @Override
        public Ast.Type visitNull(Ast.Expr.Null expr) {
            expr.type = new Ast.Type.Null();
            return expr.type;
        }
    }

    static class ClassDesc {
        final String name;
        HashMap<String, Ast.Type> vars = new HashMap<>();
        HashMap<String, Ast.Type.Function> methods = new HashMap<>();

        ClassDesc(String name) { this.name = name; }
    }

    static class Env {
        final Env parent;
        final HashMap<String, Ast.Type> scope = new HashMap<>();

        Env(Env parent) { this.parent = parent; }

        void put(String id, Ast.Type type) { this.scope.put(id, type); }

        Ast.Type get(String id) throws StaticCheckException {
            if (scope.containsKey(id)) {
                return scope.get(id);
            } else if (parent != null) {
                return parent.get(id);
            } else {
                throw new StaticCheckException(
                    String.format("Cannot resolve identifier '%s'", id));
            }
        }

        boolean contains(String id) {
            return scope.containsKey(id) ||
                (parent != null && parent.contains(id));
        }
    }

    static class StaticCheckException extends Exception {
        StaticCheckException(String message) { super(message); }
    }
}
