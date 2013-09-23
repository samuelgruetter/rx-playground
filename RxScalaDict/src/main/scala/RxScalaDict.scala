
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
    
    // convert Java observable returned by fromKeyEvents into Scala Observable:
    val keyEvents = Observable(SwingObservable.fromKeyEvents(textField))
    
    val input = for (event <- keyEvents) yield 
      event.getComponent().asInstanceOf[JTextField].getText()
    
    val throttled = input.distinctUntilChanged.filter(_.length >= 2).throttleWithTimeout(500 millis)
        
    throttled.subscribe(println(_))
    
    throttled.observeOn(rx.concurrency.Schedulers.threadPoolForIO()).map(
        LookupInWordNet.matchPrefixInWordNet(_)
    ).observeOn(SwingScheduler.getInstance).subscribe(
        matches => {
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
  
}

object RxScalaDict extends App {  
  new Win1().run
}

