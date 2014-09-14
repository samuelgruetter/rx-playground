// A reactive parallel port scanner
// Based on https://gist.github.com/gousiosg/5264201 by @gousiosg and @headinthebox
// Modified by @samuelgruetter: Remove use of futures, and use Schedulers.threadPoolForIO

import java.net.Socket

import scala.language.implicitConversions

import rx.lang.scala.Observable
import rx.lang.scala.schedulers.IOScheduler

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

  Observable.from(1 to 65536)
    .flatMap(port => Observable.just(port).observeOn(IOScheduler()).map(scanPort(_)))
    // convert to blocking because otherwise app may terminate before all threads of 
    // Schedulers.threadPoolForIO have completed their work (these threads are daemons)
    .toBlocking
    .foreach(res => res match {
      case y: Open =>
        println("Port " + y.s.getPort + " is open")
        y.s.close
      case y: Closed =>
    })
  println("Done.")
  
}
