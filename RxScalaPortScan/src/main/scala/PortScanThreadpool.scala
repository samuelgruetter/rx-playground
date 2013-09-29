// A reactive parallel port scanner
// Based on https://gist.github.com/gousiosg/5264201 by @gousiosg and @headinthebox
// Modified by @samuelgruetter: Remove use of futures, and use Schedulers.threadPoolForIO

import java.net.Socket

import scala.language.implicitConversions

import rx.lang.scala.Observable
import rx.lang.scala.concurrency.Schedulers

object PortScanThreadPool extends App {

  // Port scanning results
  trait Result
  case class Open(s: Socket) extends Result
  case class Closed(port: Int) extends Result

  val host = "localhost"
    
  def scanPort(port: Int): Result = try {
    Open(new Socket(host, port))
  } catch {
    case e: Exception => Closed(port)
  }

  Observable(1 to 65536).flatMap(
    port => Observable(port).observeOn(Schedulers.threadPoolForIO).map(scanPort(_))
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
