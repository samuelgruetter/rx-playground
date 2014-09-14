
import scala.language.implicitConversions

object ScalaImplicitsProblem {
  
  // The Java library (here implemented in Scala for simplicity).
  // I cannot change this library
  object JLib {
    
	class JObs[T] {
	  def map[R](f: Func1[T, R]): JObs[R] = ???
	    
	  def reduce(f: Func2[T, T, T]): JObs[T] = ???
	}
	
	trait Func1[T1, R] {
	  def call(arg: T1): R
	}
	trait Func2[T1, T2, R] {
	  def call(arg1: T1, arg2: T2): R
	}
    
  }
   
  // The Scala adaptor for JLib that I'm writing.
  // Here I can do whatever I want.
  object Adaptor {
    import JLib._
    
    object JFuncToSFunc{
      implicit def scalaFunction1ToRxFunc1[A, B](f: (A => B)): Func1[A, B] =
        new Func1[A, B] {
          def call(a: A): B = f(a)
        }

      implicit def scalaFunction2ToRxFunc2[A, B, C](f: (A, B) => C): Func2[A, B, C] =
        new Func2[A, B, C] {
          def call(a: A, b: B) = f(a, b)
        }
    }
    
	implicit class SObs[T](val wrapped: JObs[T]) extends AnyVal {
	  import JFuncToSFunc._
	  
      def map[R](f: T => R): JObs[R] = {
        wrapped.map(f)
      }
	    
	  def reduce(f: (T, T) => T): JObs[T] = {
	    wrapped.reduce(f)
	  } 
    }
  }
  
  // User code:
  def main(args: Array[String]): Unit = {
    import JLib._
    import Adaptor._
    
    val o = new JObs[Int]
    val fSum = (a: Int, b: Int) => a + b
    val fSquare = (a: Int) => a*a
    
    // compiles, eclipse says: "Implicit conversions found: o => SObs(o)"
    o.reduce(fSum)
    
    // compiles
    SObs(o).map(fSquare)
    
    // Expected: Compiles. 
    // Actual: Does not compile
    // Error: "type mismatch; found : Int => Int required: ScalaImplicitsProblem.JLib.Func1[Int,?]" 
    o.map(fSquare)
    
    println("hello")
  }

}


