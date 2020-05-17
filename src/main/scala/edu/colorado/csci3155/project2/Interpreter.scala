package edu.colorado.csci3155.project2

/*
   A Lettuce interpreter with evalExpr function that has missing cases to be handled. See TODOs below.
 */

object Interpreter {

    def binaryExprEval(expr: Expr, expr1: Expr, env: Environment)(fun: (Value, Value) => Value): Value = {
        val v1 = evalExpr(expr, env)
        val v2 = evalExpr(expr1, env)
        fun(v1, v2)
    }

    def evalExpr(e: Expr, env: Environment): Value = e match {
        case Const(d) => NumValue(d)
        case ConstBool(b) => BoolValue(b)
        case Ident(s) => env.lookup(s)
        case Line(l) => {                        // Handle a Line object
            val v = evalExpr(l, env)
            v match {
                case NumValue(x) => FigValue(new MyCanvas(List(Polygon(List((0,0),(x,0))))))
                case _ => throw new IllegalArgumentException("BasicShapeError: Line argument is not a number")
            }
        }
        case EquiTriangle(sideLength) => {       // Handle Equilateral Triangle
            val v = evalExpr(sideLength, env)
            def tip(l:Double): (Double,Double) = (l/2, Math.sqrt(3)*l/2)
            v match {
                case NumValue(x) => FigValue(new MyCanvas(List(Polygon(List((0,0),(x,0),tip(x))))))
                case _ => throw new IllegalArgumentException("BasicShapeError: Side Length argument is not a number")
            }
        }
        case Rectangle(sideLength) => {          // Handle Square
            val v = evalExpr(sideLength, env)
            v match {
                case NumValue(x) => FigValue(new MyCanvas(List(Polygon(List((0,0),(0,x),(x,x),(x,0))))))
                case _ => throw new IllegalArgumentException("BasicShapeError: Side Length argument is not a number")
            }
        }
        case Circle(rad) => {                    // Handle Circle
            val v = evalExpr(rad, env)
            v match {
                case NumValue(r) => FigValue(new MyCanvas(List(MyCircle((r,r),r))))
                case _ => throw new IllegalArgumentException("BasicShapeError: Radius argument is not a number")
            }
        }
        case Plus (e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.plus) // Handle addition of numbers or figures
        case Minus (e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.minus) // Handle subtraction
        case Mult(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.mult) // Handle multiplication of numbers or figures
        case Div(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.divide) // Handle division
        case Geq(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.geq)
        case Gt(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.gt)
        case Eq(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.equal)
        case Neq(e1, e2) => binaryExprEval(e1, e2, env) (ValueOps.notEqual)
        case And(e1, e2) => {
            val v1 = evalExpr(e1, env)
            v1 match {
                case BoolValue(true) => {
                    val v2 = evalExpr(e2, env)
                    v2 match {
                        case BoolValue(_) => v2
                        case _ => throw new IllegalArgumentException("And applied to a non-Boolean value")
                    }
                }
                case BoolValue(false) => BoolValue(false)
                case _ => throw new IllegalArgumentException("And applied to a non-boolean value")
            }
        }

        case Or(e1, e2) => {
            val v1 = evalExpr(e1, env)
            v1 match {
                case BoolValue(true) => BoolValue(true)
                case BoolValue(false) => {
                    val v2 = evalExpr(e2, env)
                    v2 match {
                        case BoolValue(_) => v2
                        case _ => throw new IllegalArgumentException("Or Applied to a non-Boolean value")
                    }
                }
                case _ => throw new IllegalArgumentException("Or Applied to a non-Boolean Value")
            }
        }

        case Not(e) => {
            val v = evalExpr(e, env)
            v match {
                case BoolValue(b) => BoolValue(!b)
                case _ => throw new IllegalArgumentException("Not applied to a non-Boolean Value")
            }
        }

        case IfThenElse(e, e1, e2) => {
            val v = evalExpr(e, env)
            v match {
                case BoolValue(true) => evalExpr(e1, env)
                case BoolValue(false) => evalExpr(e2,env)
                case _ => throw new IllegalArgumentException("If then else condition is not a Boolean value")
            }
        }

        case Let(x, e1, e2) => {
            val v1 = evalExpr(e1, env)
            val env2 = Extend(x, v1, env)
            evalExpr(e2, env2)
        }

        // Handle function definitions: return a closure with the current env
        case FunDef(x, e) => Closure(x, e, env)
        // Handle recursive functions -- look at Environment.scala
        case LetRec(f, x, e1, e2) => evalExpr(e2, {ExtendREC(f, x, e1, env)})
        // Handle function calls
        case FunCall(fCallExpr, arg) =>
            val v1 = evalExpr(fCallExpr, env)
            val v2 = evalExpr(arg, env)
            v1 match {
                case Closure(x, closureExpr, closedEnv) => {
                    // Extend the closed environment by binding x to v2
                    val newEnv = Extend(x, v2, closedEnv)
                    // then evaluate the body of the closure under the extended environment.
                    evalExpr(closureExpr, newEnv)
                }
                case _ => throw new IllegalArgumentException(
                    s"Function call error: expression $fCallExpr does not evaluate to a closure")
            }
    }

    def evalProgram(p: Program): Value = p match {
        case TopLevel(e) => evalExpr(e, EmptyEnvironment)
    }

}
