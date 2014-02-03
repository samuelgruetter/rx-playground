
import java.awt.BorderLayout
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants
import rx.lang.scala.Observable
import rx.observables.SwingObservable
import rx.lang.scala.JavaConversions


class Win1 extends JFrame {
  val label = new JLabel("")
  
  def run = {
    initLayout
    
    val konami = Observable.items(
        38, // up
        38, // up
        40, // down
        40, // down
        37, // left
        39, // right
        37, // left
        39, // right
        66, // b
        65  // a
    )
    
    val pressedKeys = JavaConversions.toScalaObservable(SwingObservable.fromKeyEvents(this))
                        .filter(_.getID() == KeyEvent.KEY_RELEASED)
                        .map(_.getKeyCode())
    
    val bingo = pressedKeys.window(10, 1)
                  .flatMap(window => (window zip konami).forall(p => p._1 == p._2))
                  .filter(identity)
    
    bingo.subscribe(_ => label.setText(label.getText() + " KONAMI "))
  }
   
  def initLayout = {
    setLayout(new BorderLayout)
    label.setHorizontalAlignment(SwingConstants.CENTER)
    add(label, BorderLayout.CENTER)
    setSize(400, 400)
    setResizable(false)
    setTitle("Enter the Konami Code!")
	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }  
}

object Konami extends App {  
  new Win1().run
}

