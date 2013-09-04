
import ScalaAdapter._

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

}
