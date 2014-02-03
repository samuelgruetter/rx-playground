 
// A reactive parallel port scanner written with Rx.Java
// @author: @gousiosg, @headinthebox
// From https://gist.github.com/gousiosg/5264201
// Modified by @samuelgruetter

import java.net.Socket
import rx.lang.scala.Observable
import scala.concurrent.{Future, future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import rx.subscriptions.Subscriptions
import rx.lang.scala.Subscription

// Port scanning results
trait Result
case class Open(s: Socket) extends Result
case class Closed(port: Int) extends Result
 
object PortScanFutures extends App {
 
  import FutureExtensions._
 
  val host = "localhost"
    
  // This line's effect is that the futures are executed on a thread pool whose threads are
  // not deamons, so once the main thread has finished, the threads executing the futures are
  // not killed. 
  // Once the last future has completed, the app waits 60 seconds and then terminates, because
  // threads of the thread pool are terminated after being idle for 60 seconds.
  // Without this line, the app terminates before all open ports are printed!
  implicit val ec = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
 
  Observable.from(1 to 65536).flatMap(port =>
    future {
      try {
        Open(new Socket(host, port))
      } catch {
        case e: Exception => Closed(port)
      }
    }.asObservable
  ).subscribe(
    res => res match {
      case y: Open =>
        println("Port " + y.s.getPort + " is open")
        y.s.close
      case y: Closed =>
    },
    err => err.printStackTrace(),
    () => println("Done.")
  )
}


// Extend Future with the asObservable method
object FutureExtensions {
 
  import rx.lang.scala.Observable
  import rx.lang.scala.Observer
 
  class ObservableFuture[T](f: Future[T]) {
 
    def asObservable: Observable[T] = {
      Observable.create((o: Observer[T]) => {
        f.onComplete {
          t => t match {
            case Success(s) =>
              o.onNext(s)
              o.onCompleted
            case Failure(s) =>
              o.onError(new Exception(s))
          }
        }
        Subscription()
      })
    }
  }
 
  implicit def richFuture[T](f: Future[T]): ObservableFuture[T] = new ObservableFuture[T](f)
}
