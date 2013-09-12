
/*
 * Note: Could also use scala.swing wrappers, but they don't seem to be very actively developed...
 */

import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.JTextArea
import javax.swing.BorderFactory
import java.awt.BorderLayout
import javax.swing.JScrollPane
import scala.language.postfixOps
import scala.concurrent.duration._
import rx.lang.scala.Observable
import rx.observables.SwingObservable
import rx.concurrency.SwingScheduler

class Win1 extends JFrame {
  val textArea = new JTextArea()
  val textField = new JTextField()

  def run = {
    initLayout
    
    ThreadLogger.log("Win1.run")
    
    // wrapping from Java Observable to Scala Observable because there is no RxScalaSwing adapter yet
    val input = for (event <- new Observable(SwingObservable.fromKeyEvents(textField))) yield 
      event.getComponent().asInstanceOf[JTextField].getText()
    
    // TODO: there's no distinctUntilChanged() operation yet in RxJava
    
    val throttled = input.throttleWithTimeout(1000 millis)
        
    throttled.subscribe((s: String) => println(s))
    
    throttled.observeOn(rx.concurrency.Schedulers.threadPoolForIO()).map(
        (s: String) => LookupInWordNet.matchPrefixInWordNet(s)
    ).observeOn(SwingScheduler.getInstance()).subscribe(
        (matches: Seq[String]) => {
          ThreadLogger.log("updating text in textArea")
          textArea.setText(matches.mkString("\n"))
        }
    )
    
  }
   
  def initLayout = {  
    rootPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12))
        
    val sp = new JScrollPane(textArea)
	setLayout(new BorderLayout(12, 12))
	add(textField, BorderLayout.NORTH)
	add(sp, BorderLayout.CENTER)
	setSize(400, 400)

	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
  
  def test1 = {
    println("Hello world")
    
    Observable(1 to 10).subscribe((i: Int) => println(i))
    
    for (s <- LookupInWordNet.matchPrefixInWordNet("wall")) {
      println(s)
    }
  }
  
}

object RxScalaDict extends App {  
  new Win1().run
}

