
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

class Win1 extends JFrame {
  val textArea = new JTextArea()
  val textField = new JTextField()

  def run = {
    initLayout
    
    // wrapping from Java Observable to Scala Observable because there is no RxScalaSwing adapter yet
    val input = for (event <- new Observable(SwingObservable.fromKeyEvents(textField))) yield 
      event.getComponent().asInstanceOf[JTextField].getText()
    
    // TODO: there's no distinctUntilChanged() operation yet in RxJava
    
    // TODO: throttle is not yet in Scala adapter
    val slower = input.sample(1000 millis)
    
    slower.subscribe((s: String) => println(s))
    
//                var input = (from evt in Observable.FromEventPattern<EventArgs>(txt, "TextChanged")
//                         select ((TextBox)evt.Sender).Text)
//                        .Throttle(TimeSpan.FromSeconds(1))
//                        .DistinctUntilChanged()
//                        .Do(x => Console.WriteLine(x));
//

//            //nicer:
//            var res = (from term in input
//                       select matchInWordNetByPrefix(term))
//                      .Switch();
//
//            using (res.ObserveOn(lst).Subscribe(
//                words => {
//                    lst.Items.Clear();
//                    lst.Items.AddRange((from word in words select word.Word).ToArray());
//                },
//                ex => { Console.WriteLine(ex.Message); }
    
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

