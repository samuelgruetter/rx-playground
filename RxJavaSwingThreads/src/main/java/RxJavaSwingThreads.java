import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.concurrency.SwingScheduler;
import rx.observables.SwingObservable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/*
 * we have 3 threads in this app:
 * a) the thread on which RxSwing2 constructor is executed
 * b) the thread created by SecondsImpl
 * c) the event dispatch thread
 */

public class RxJavaSwingThreads extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;
	private JTextArea textArea;
	
	public static void main(String[] args) {
		new RxJavaSwingThreads();
	}

	// approx. count of seconds since app startup
	Observable<Integer> seconds = Observable.create(new SecondsImpl());
		
	class SecondsImpl implements Func1<Observer<Integer>, Subscription> {
		// used by subscription thread as well as by this.thread !
		private Set<Observer<Integer>> observers =
				Collections.newSetFromMap(new ConcurrentHashMap<Observer<Integer>, Boolean>()); 

		Thread thread;
		private int n = 0;

		// called whenever someone subscribes to this observable
		public Subscription call(final Observer<Integer> obs) {
			observers.add(obs);
			return new Subscription() {
				public void unsubscribe() {
					observers.remove(obs);
				}
			};
		}
		
		public SecondsImpl() {
			// using my own thread is bad, should use schedulers
			thread = new Thread() {
				@Override
				public void run() {
					try {
						while (true) {
							Thread.sleep(1000);
							n++;
							for (Observer<Integer> obs : observers) obs.onNext(n);
						}
					} catch (InterruptedException e) {
						for (Observer<Integer> obs : observers) obs.onError(e);
					}
				}
			};
			thread.start();
		}
	}
	
	// would be shorter in Scala ;)
	Iterable<Integer> naturals = new Iterable<Integer>() {
		public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				private int n = 0;
				public boolean hasNext() {
					return true;
				}
				public Integer next() {
					return n++;
				}
				public void remove() {
					throw new UnsupportedOperationException();					
				}
			};
		}
	};
	
	// new try:
	// javadoc of Observable.from: 
	// the entire iterable sequence will be immediately emitted each time an Observer subscribes.
	// => this first creates all naturals eagerly => does not work :(
	// Observable<Integer> secondsEagerly = Observable.from(naturals).sample(1, TimeUnit.SECONDS);
	
	// clumsy try to get callbacks run on event dispatch thread, does not work, not sure if bug
	class MySubscriptions implements Runnable {
		public void run() {
			logThread("subscribe-to-seconds");
			
			Scheduler schedulerOfEventDispatchThread = Schedulers.currentThread();
			
			seconds.observeOn(schedulerOfEventDispatchThread).subscribe(new Action1<Integer>() {
				public void call(Integer n) {
					logThread("title-changing-MySubscriptions-deactivated");
					//RxSwing2.this.setTitle("t = " + n + " seconds");				
				}
			});
		}		
	}
	
	public RxJavaSwingThreads() {

		this.rootPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		// textArea = new JTextArea("some\ninitial text\n");
		textArea = new JTextArea("some\ninitial text\n");
		textField = new JTextField("edit me");
		
		JScrollPane sp = new JScrollPane(textArea);
		
		setLayout(new BorderLayout(12, 12));
		add(textField, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		setSize(400, 400);

		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Observable<KeyEvent> keyEvents = SwingObservable.fromKeyEvents(textField);
		
		Observable<String> textFieldContents = keyEvents.map(new Func1<KeyEvent, String>() {
			public String call(KeyEvent e) {
				return ((JTextField) e.getSource()).getText();
			}
		});
		
		/*Scheduler sched = Schedulers.
		textFieldContents.observeOn(sched);*/
		
		// textFieldContents comes from SwingObservable => callbacks will be on event dispatch thread => OK
		textFieldContents.subscribe(new Action1<String>(){
			public void call(String s) {
				logThread("textFieldContents subscription callback ");
				textArea.setText(textArea.getText() + "\n" + s);
			}
		});
		
		// System.out can be used from any thread => OK
		textFieldContents.subscribe(new Action1<String>() {
			public void call(String s) {
				System.out.println(s);				
			}
		});
		
		// System.out can be used from any thread => OK
		seconds.subscribe(new Action1<Integer>() {
			public void call(Integer n) {
				System.out.println("Infinite subscriber got " + n);				
			}
		});

		// System.out can be used from any thread => OK
		seconds.skip(4).take(6).subscribe(new Action1<Integer>() {
			public void call(Integer n) {
				logThread("skip4take6");
				System.out.println("skip4take6 subscriber got " + n);				
			}
		});
		
		// System.out can be used from any thread => OK
		seconds.where(new Func1<Integer, Boolean>() {
			public Boolean call(Integer t1) {
				return t1 % 2 == 0;
			}
		}).subscribe(new Action1<Integer>() {
			public void call(Integer n) {
				System.out.println("evenonly subscriber got " + n);				
			}
		});
		
		// This is BAD because UI is modified on SecondsImpl's thread instead of event dispatching thread!
		seconds.subscribe(new Action1<Integer>() {
			public void call(Integer n) {
				logThread("title-changing-BAD-deactivated");
				//RxSwing2.this.setTitle("t = " + n);				
			}
		});
		
		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {
				System.out.println("Mouse pressed on JFrame");
				logThread("conventional UI event handler");
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});
		
		// invoke subscriptions on event dispatch thread
		SwingUtilities.invokeLater(new MySubscriptions());
		
		seconds.observeOn(SwingScheduler.getInstance()).subscribe(new Action1<Integer>(){
			public void call(Integer n) {
				logThread("title-changing-SwingScheduler");
				RxJavaSwingThreads.this.setTitle("t = " + n + "s");				
			}
		});
		
		logThread("constructor");
	}
	
	void logThread(String name) {
		System.out.println("[" + Thread.currentThread().getId() + "/" + Schedulers.currentThread().toString() + "] " 
				+ name + " thread ("  
				+ SwingUtilities.isEventDispatchThread() + ")");
	}
}

