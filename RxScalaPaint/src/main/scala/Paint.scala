
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.JFrame
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscription
import rx.observables.SwingObservable
import rx.lang.scala.JavaConversions


case class Line(x1: Int, y1: Int, x2: Int, y2: Int)

class Win1 extends JFrame {

  val drawPad = new DrawPad
  
  def run = {
    initLayout
    
    val mouseMoveEvents = JavaConversions.toScalaObservable(SwingObservable.fromMouseMotionEvents(drawPad))
    
    val lines = for (Seq(e1, e2) <- mouseMoveEvents.buffer(2, 1)) 
          yield Line(e1.getX(), e1.getY(), e2.getX(), e2.getY())

    // emits true when mouse button 1 goes down, false when it goes up
    val mouseDown = MouseEventSource.mouseButtonDown(drawPad, 1)
    
    // Paint if the mouse is down
    val paint = mouseDown.map(if (_) lines else Observable.empty).switch
    
    paint.subscribe(drawPad.drawLine(_))    
  }
   
  def initLayout = {
    add(drawPad)
    setSize(400, 400)
    setResizable(false)
    setTitle("RxScala Paint")
	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
  
}

class DrawPad extends JComponent {

  var image: Image = null
  var graphics2D: Graphics2D = null

  setDoubleBuffered(false)

  override def paintComponent(g: Graphics) {
    if (image == null) {
      image = createImage(getSize().width, getSize().height)
      graphics2D = image.getGraphics().asInstanceOf[Graphics2D]
      graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      clear()
    }
    g.drawImage(image, 0, 0, null)
  }
  
  def drawLine(l: Line) {
    graphics2D.drawLine(l.x1, l.y1, l.x2, l.y2)
    repaint()
  }

  def clear() {
    graphics2D.setPaint(Color.white)
    graphics2D.fillRect(0, 0, getSize().width, getSize().height)
    graphics2D.setPaint(Color.black)
    repaint()
  }
}

object MouseEventSource {
  
  /**
   * @returns an Observable that emits true when mouse button {@code buttonId} goes down and
   *          false when it goes up
   */
  def mouseButtonDown(comp: JComponent, buttonId: Int) : Observable[Boolean] = Observable.create(
    (observer: Observer[Boolean]) => {
      val listener = new MouseListener() {
        def mouseClicked(e: MouseEvent): Unit = {}
        def mouseEntered(e: MouseEvent): Unit = {} 
        def mouseExited(e: MouseEvent): Unit = {} 
        def mousePressed(e: MouseEvent): Unit = {
          if (e.getButton() == buttonId) observer.onNext(true) 
        }
        def mouseReleased(e: MouseEvent): Unit = {
          if (e.getButton() == buttonId) observer.onNext(false) 
        }
      }
      comp.addMouseListener(listener)
      new Subscription {
        override def unsubscribe: Unit = comp.removeMouseListener(listener)
      }
    }
  )
}

object Paint extends App {  
  new Win1().run
}

