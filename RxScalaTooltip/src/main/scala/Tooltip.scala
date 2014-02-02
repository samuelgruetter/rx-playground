
import java.awt.Color
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.io.File
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import rx.lang.scala.Observable
import rx.observables.SwingObservable
import rx.lang.scala.JavaConversions

class Win1 extends JFrame {

  val imgDisplay = new ImgDisplay("logo.png")
  val tooltip = new JLabel("tooltip")

  def run = {
    initLayout

    // Events for: mouse button is pressed/released/clicked, mouse enters/leaves imgDisplay
    val mouseActions = JavaConversions.toScalaObservable(SwingObservable.fromMouseEvents(imgDisplay))

    // Events for: mouse is moved
    val mouseMoves = JavaConversions.toScalaObservable(SwingObservable.fromMouseMotionEvents(imgDisplay))

    val mouseEvents = mouseActions merge mouseMoves

    val showTooltipMoves = mouseEvents
       // if the mouse leaves imgDisplay, last event kept by throttleWithTimeout will be the 
       // MOUSE_EXITED event (filtered out), so no tooltip is shown if mouse is outside
      .throttleWithTimeout(300.millis) 
      .filter(_.getID() == MouseEvent.MOUSE_MOVED)
      .map(Point(_))

    // Show tooltip:
    showTooltipMoves.subscribe(p => {
      val c = imgDisplay.getColorAt(p)
      tooltip.setText(colorToString(c))
      tooltip.setLocation(p + Point(0, 15))
      tooltip.setVisible(true)
    })

    // On any mouse event, hide the tooltip. Does not interfere with showing the tooltip because
    // showing happens 300ms later.
    mouseEvents.subscribe(_ => {
      tooltip.setVisible(false)
    })
  }
  
  def colorToString(c: Color) = s"RGB(${c.getRed}, ${c.getGreen}, ${c.getBlue})"

  def initLayout = {
    setSize(350, 250)
    setLayout(null)
    
    tooltip.setBackground(Color.WHITE)
    tooltip.setOpaque(true)
    tooltip.setHorizontalAlignment(SwingConstants.CENTER)
    tooltip.setBorder(BorderFactory.createLineBorder(Color.BLACK))
    tooltip.setVisible(false)
    tooltip.setBounds(40, 40, 150, 20)
    add(tooltip)

    add(imgDisplay)
    
    setResizable(false)
    setTitle("Color tooltip")
	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }

  class ImgDisplay(imgPath: String) extends JPanel {
    val img = ImageIO.read(new File(imgPath))
    setSize(img.getWidth(), img.getHeight())
    setBorder(BorderFactory.createLineBorder(Color.BLACK))

    override def paint(g: Graphics) {
      super.paint(g);
      g.drawImage(img, 0, 0, this);
    }

    def getColorAt(p: Point): Color = {
      new Color(img.getRGB(p.x, p.y))
    }
  }
  
  case class Point(x: Int, y: Int) {
    def +(that: Point) = Point(x + that.x, y + that.y)
  }
  object Point {
    def apply(e: MouseEvent): Point = Point(e.getX, e.getY)
  }
  implicit def point2javaAwtPoint(p: Point): java.awt.Point = new java.awt.Point(p.x, p.y)
}

object Tooltip extends App {  
  new Win1().run
}

