
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
import javax.swing.JSlider
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.event.ChangeEvent

// TODO add unit to output
// case class VoltageSetting(value: Int, unit: String)

class Win1 extends JFrame {
  
  val voltageSlider = createTickSlider(0, 1000)
  //val unitCB = new JComboBox[String]
  val periodSlider = createTickSlider(100, 1100)
  
  def changeEventToSliderValue(e: ChangeEvent): Int = {
    e.getSource().asInstanceOf[JSlider].getValue()
  }
  
  def run = {
    initLayout
    
    // Note: fromChangeEvents is not yet in Netflix RxJava
    val voltageSliderEventsJava = SwingObservable.fromChangeEvents(voltageSlider)
    val periodSliderEventsJava = SwingObservable.fromChangeEvents(periodSlider)
    
    // Convert Java observable into Scala Observable:
    val voltageSliderEvents = Observable(voltageSliderEventsJava)
    val periodSliderEvents = Observable(periodSliderEventsJava)
    
    // Turn the Observable[ChangeEvent] into an Observable[Int] emitting the slider values
    val voltageSliderValues = voltageSliderEvents.map(changeEventToSliderValue(_))
    val periodSliderValues = periodSliderEvents.map(changeEventToSliderValue(_))
    
    // When the period slider has kept the same value for 200ms, we believe that the user
    // has made a choice and that it's worth changing the sampling rate
    val changePeriod = periodSliderValues.distinctUntilChanged.throttleWithTimeout(200.millis)
    
    val sampled = changePeriod.map((period: Int) => voltageSliderValues.sample(period.millis)).switch
    
    val t0 = System.currentTimeMillis()
    
    sampled.subscribe(v => println(s"$v at t=${System.currentTimeMillis()-t0}"))
        
    // We set the sliders' start values only now, because we want the start value to appear
    // in the Observables
    setSlidersStartValue(voltageSlider)
    setSlidersStartValue(periodSlider)
  }
  
  def setSlidersStartValue(s: JSlider) { 
    s.setValue((s.getMaximum() - s.getMinimum())/2)
  }
  
  def createTickSlider(min: Int, max: Int): JSlider = {
    val s = new JSlider(min, max)
    s.setMajorTickSpacing((max-min)/10)
    s.setMinorTickSpacing((max-min)/50)
    s.setPaintTicks(true)
	s.setPaintLabels(true)
	s
  }
   
  def initLayout = {  
    rootPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12))
    setTitle("Voltage control")
        
	setLayout(new BorderLayout(12, 12))
		
	//unitCB.addItem("V")
	//unitCB.addItem("mV")
	
	val upperPanel = new JPanel
	upperPanel.setLayout(new BorderLayout(12, 12))
	upperPanel.add(new JLabel("Voltage:"), BorderLayout.WEST)
	upperPanel.add(voltageSlider, BorderLayout.CENTER)
	//upperPanel.add(unitCB, BorderLayout.EAST)
	upperPanel.add(new JLabel("mV"), BorderLayout.EAST)
	add(upperPanel, BorderLayout.NORTH)
	
	val lowerPanel = new JPanel
	lowerPanel.setLayout(new BorderLayout(12, 12))
	lowerPanel.add(new JLabel("Update interval:"), BorderLayout.WEST)
	lowerPanel.add(periodSlider, BorderLayout.CENTER)
	lowerPanel.add(new JLabel("ms"), BorderLayout.EAST)
	add(lowerPanel, BorderLayout.SOUTH)
	
	setSize(800, 150)

	setVisible(true)
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  }
  
}

object VoltageControl extends App {  
  new Win1().run
}

