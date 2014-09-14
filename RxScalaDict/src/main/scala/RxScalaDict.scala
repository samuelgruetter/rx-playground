
import java.awt.BorderLayout
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import rx.lang.scala.Observable
import rx.lang.scala.Scheduler
import rx.lang.scala.schedulers._
import rx.observables.SwingObservable
import rx.lang.scala.JavaConversions

class Win1 extends JFrame {
  val textArea = new JTextArea()
  val textField = new JTextField()

  def run = {
    initLayout
    
    ThreadLogger.log("Win1.run")
    
    // convert Java observable returned by fromKeyEvents into Scala Observable:
    val keyEvents = JavaConversions.toScalaObservable(SwingObservable.fromKeyEvents(textField))
    
    val input = for (event <- keyEvents) yield 
      event.getComponent().asInstanceOf[JTextField].getText()
    
    val throttled = input.distinctUntilChanged.filter(_.length >= 2).throttleWithTimeout(500 millis)
  
    throttled.subscribe(println(_))
    
    throttled
      // Switch from SwingScheduler to IOScheduler:
      .observeOn(IOScheduler())
      // Do IO which might take much time:
      .map(LookupInWordNet.matchPrefixInWordNet(_))
      // Switch back from IOScheduler to SwingScheduler:
      .observeOn(SwingScheduler)
      .subscribe(
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
	setTitle("RxScalaDict")

	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }

  // For SwingScheduler, there is no Scala Wrapper yet, so we have to convert explicitly
  // from Java Scheduler to Scala Scheduler:
  val SwingScheduler: Scheduler = new Scheduler {
    val asJavaScheduler = rx.schedulers.SwingScheduler.getInstance
  }
}

object RxScalaDict {
  def main(args: Array[String]): Unit = {
    // Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        new Win1().run
      }
    });
  }
}

