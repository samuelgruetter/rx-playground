import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import rx.Observable;
import rx.Scheduler;
import rx.concurrency.Schedulers;
import rx.concurrency.SwingScheduler;
import rx.observables.SwingObservable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/*
 * we have 3 threads in this app:
 * a) the thread on which RxJavaSwingThreads constructor is executed
 * b) the thread created by interval()
 * c) the event dispatch thread
 */

public class RxJavaSwingThreads extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;
	private JTextArea textArea;
	
	// approx. count of seconds since app startup
	Observable<Long> seconds = MyObservable.interval(1, TimeUnit.SECONDS);
	
	// all key Events of textField
	Observable<KeyEvent> keyEvents;
	
	// all Strings ever present in textField (duplicates because of key down/up)
	Observable<String> textFieldContents;
	
	public static void main(String[] args) {
		new RxJavaSwingThreads();
	}

	public RxJavaSwingThreads() {
		makeLayout();
		
		keyEvents = SwingObservable.fromKeyEvents(textField);
		textFieldContents = keyEvents.map(new Func1<KeyEvent, String>() {
			public String call(KeyEvent e) {
				return ((JTextField) e.getSource()).getText();
			}
		});
		
		checkNotOnUiThread("constructor");
		
		// spams System.out 
		// subscriptionsTouchingOnlySystemOut();
		
		subscriptionsTouchingUi();
	}
	
	void subscriptionsTouchingUi() {
		// Subscription 1 (correct)
		// textFieldContents comes from SwingObservable => callbacks will be on event dispatch thread => OK
		textFieldContents.subscribe(new Action1<String>(){
			public void call(String s) {
				checkOnUiThread("textFieldContents subscription callback ");
				textArea.setText(textArea.getText() + "\n" + s);
			}
		});
		
		// Subscription 2 (bad)
		// This is BAD because UI is modified on SecondsImpl's thread instead of event dispatching thread!
		seconds.subscribe(new Action1<Long>() {
			public void call(Long n) {
				checkOnUiThread("title-changing-BAD-deactivated");
				//RxSwing2.this.setTitle("t = " + n);				
			}
		});
		
		// Subscription 3 (correct)
		// The correct way to achieve what we tried above
		seconds.observeOn(SwingScheduler.getInstance()).subscribe(new Action1<Long>(){
			public void call(Long n) {
				checkOnUiThread("title-changing-SwingScheduler");
				RxJavaSwingThreads.this.setTitle("t = " + n + "s");				
			}
		});

		// Subscription 4 (producing unexpected behavior)
		// clumsy try to get callbacks run on event dispatch thread, does not work, not sure if bug
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				checkOnUiThread("subscribe-to-seconds");
				// we're indeed on the UI thread
				
				// so Schedulers.currentThread() must "represent" the UI thread
				Scheduler schedulerOfEventDispatchThread = Schedulers.currentThread();
				
				seconds.observeOn(schedulerOfEventDispatchThread).subscribe(new Action1<Long>() {
					public void call(Long n) {
						// BUT this test fails! (i.e. it prints "BAD") Why? Where's the problem?
						checkOnUiThread("title-changing-MySubscriptions-deactivated");
						//RxSwing2.this.setTitle("t = " + n + " seconds");				
					}
				});
			}		
		});
	}
	
	// Logging/Checking:
	
	void checkOnUiThread(String whereAreWe) {
		System.out.println(
				(SwingUtilities.isEventDispatchThread() ? "" : "(BAD: not on UI thread)") +
				threadDescription(whereAreWe)
		);
	}
	
	void checkNotOnUiThread(String whereAreWe) {
		System.out.println(
				(SwingUtilities.isEventDispatchThread() ? "(BAD: on UI thread)" : "") +
				threadDescription(whereAreWe)
		);
	}
	
	String threadDescription(String whereAreWe) {
		return "[" + Thread.currentThread().getId() + "] " + whereAreWe;
	}
	
	void logThread(String whereAreWe) {
		System.out.println(threadDescription(whereAreWe));
	}
	
	// Boring subscriptions:
	
	void subscriptionsTouchingOnlySystemOut() {
		// System.out can be used from any thread => OK
		textFieldContents.subscribe(new Action1<String>() {
			public void call(String s) {
				System.out.println(s);				
			}
		});
		
		// System.out can be used from any thread => OK
		seconds.subscribe(new Action1<Long>() {
			public void call(Long n) {
				System.out.println("Infinite subscriber got " + n);				
			}
		});

		// System.out can be used from any thread => OK
		seconds.skip(4).take(6).subscribe(new Action1<Long>() {
			public void call(Long n) {
				logThread("skip4take6");
				System.out.println("skip4take6 subscriber got " + n);				
			}
		});
		
		// System.out can be used from any thread => OK
		seconds.where(new Func1<Long, Boolean>() {
			public Boolean call(Long t1) {
				return t1 % 2 == 0;
			}
		}).subscribe(new Action1<Long>() {
			public void call(Long n) {
				System.out.println("evenonly subscriber got " + n);				
			}
		});
	}
	
	// Window Layout:
	
	void makeLayout() {
		rootPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		textArea = new JTextArea("some\ninitial text\n");
		textField = new JTextField("edit me");
		
		JScrollPane sp = new JScrollPane(textArea);
		
		setLayout(new BorderLayout(12, 12));
		add(textField, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		setSize(400, 400);

		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// traditional event subscription:
	
	void traditionalEventSubscription() {
		this.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {
				System.out.println("Mouse pressed on JFrame");
				checkOnUiThread("conventional UI event handler");
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});
	}
	
}

