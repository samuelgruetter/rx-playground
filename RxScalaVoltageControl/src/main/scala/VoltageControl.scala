
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import javax.swing.BorderFactory
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import rx.lang.scala.Observable
import rx.lang.scala.Observer
import rx.lang.scala.Subscription

case class VoltageSetting(value: Int, unit: String) {
  override def toString = s"VoltageSetting($value $unit)"
}

class Win1 extends JFrame {
  
  val voltageSlider = createTickSlider(0, 1000)
  val unitCB = new JComboBox[String]
  val periodSlider = createTickSlider(100, 1100)
  
  def changeEventToSliderValue(e: ChangeEvent): Int = {
    e.getSource().asInstanceOf[JSlider].getValue()
  }
  
  def actionEventToCbValue(e: ActionEvent): String = {
    e.getSource().asInstanceOf[JComboBox[String]].getSelectedItem().asInstanceOf[String]
  }
  
  def run = {
    initLayout
    
    val voltageSliderEvents = SwingSource.fromChangeEventsOf(voltageSlider)
    val periodSliderEvents = SwingSource.fromChangeEventsOf(periodSlider)
    val unitEvents = SwingSource.fromActionEventsOf(unitCB)
    
    // Turn the Observable[ChangeEvent] into an Observable[Int] emitting the slider values
    val voltageSliderValues = voltageSliderEvents.map(changeEventToSliderValue(_))
    val periodSliderValues = periodSliderEvents.map(changeEventToSliderValue(_))
    val unitValues = unitEvents.map(actionEventToCbValue(_))
    
    // When the period slider has kept the same value for 200ms, we believe that the user
    // has made a choice and that it's worth changing the sampling rate
    val changePeriod = periodSliderValues.distinctUntilChanged.throttleWithTimeout(200.millis)
    
    val sampled = changePeriod.map((period: Int) => {
      // Make sure that `sample` has a start value (current value of voltage slider):
      (Observable.items(voltageSlider.getValue) ++ voltageSliderValues).sample(period.millis)
    }).switch
    
    val withUnit = (for ((voltage, unit) <- sampled.combineLatest(unitValues)) 
                        yield VoltageSetting(voltage, unit))
    
    val subscription = withUnit.subscribe(v => println(v))
        
    // We set the start values only now, because we want the start value to appear
    // in the Observables
    setSlidersStartValue(voltageSlider)
    setSlidersStartValue(periodSlider)
    unitCB.setSelectedItem("mV")
    
    addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) {
        subscription.unsubscribe()
        System.exit(0)
      }
    })
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
		
	unitCB.addItem("V")
	unitCB.addItem("mV")
	
	val upperPanel = new JPanel
	upperPanel.setLayout(new BorderLayout(12, 12))
	upperPanel.add(new JLabel("Voltage:"), BorderLayout.WEST)
	upperPanel.add(voltageSlider, BorderLayout.CENTER)
	upperPanel.add(unitCB, BorderLayout.EAST)
	add(upperPanel, BorderLayout.NORTH)
	
	val lowerPanel = new JPanel
	lowerPanel.setLayout(new BorderLayout(12, 12))
	lowerPanel.add(new JLabel("Update interval:"), BorderLayout.WEST)
	lowerPanel.add(periodSlider, BorderLayout.CENTER)
	lowerPanel.add(new JLabel("ms"), BorderLayout.EAST)
	add(lowerPanel, BorderLayout.SOUTH)
	
	setSize(800, 150)

	setVisible(true)
  }
  
}

// {ComboBox ActionEvents => Observable[ActionEvent]} and {JSlider ChangeEvents => Observable[ChangeEvent]}
// is not yet in SwingObservable, so we implement it ourselves:
object SwingSource {
  
  def fromActionEventsOf(cb: JComboBox[String]): Observable[ActionEvent] = Observable.create(
    (observer: Observer[ActionEvent]) => {
      val listener = new ActionListener() {
        def actionPerformed(event: ActionEvent) {
          observer.onNext(event)
        }
      }
      cb.addActionListener(listener)
      new Subscription {
        override def unsubscribe: Unit = cb.removeActionListener(listener)
      }
    }
  )
  
  def fromChangeEventsOf(slider: JSlider): Observable[ChangeEvent] = Observable.create(
    (observer: Observer[ChangeEvent]) => {
      val listener = new ChangeListener() {
        def stateChanged(event: ChangeEvent) {
          observer.onNext(event)
        }
      }
      slider.addChangeListener(listener)
      new Subscription {
       override def unsubscribe: Unit = slider.removeChangeListener(listener)
      }
    }
  )
  
}

object VoltageControl extends App {  
  new Win1().run
}

