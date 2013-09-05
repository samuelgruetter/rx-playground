
import rx.lang.scala.Adaptor._ // TODO replace with new one
import rx.util.functions.Func1
import rx.util.functions.Action1

object Test1 {

  def printInt(x: Int) = println(x)

  def main(args: Array[String]): Unit = {
    val o1 = Observable(1, 2, 3, 4)
    o1.subscribe((x: Int) => println(x))

    // no need to apply implicit conversion on printInt(_)
    // => scala bug in 2.10 does not cause problems
    o1.subscribe(printInt(_)) 
    
    for (n <- o1) {
      println(n)
    }
    
    o1.drop(2).subscribe(printInt(_), (t: Throwable) => {}, () => println("complete"))
    
  }
  
  // Question: Is it a problem that in rx.Observable, some functions take an Action1<Throwable>
  // instead of an Action1<? super Throwable> ?
  // Answer: No problem. Look at this example:
  def foo() = {
    val myFunc: Any => Unit = (o: Any) => println(o)
    
    val f: Action1[Throwable] = new Action1[Throwable]() {
      // this is what the implicit conversion do:
      def call(t: Throwable) {
        myFunc(t) // no one cares that myFunc accepts more than only Throwable
      }
    }
  }

}
