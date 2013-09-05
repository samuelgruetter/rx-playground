import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


public class MyObservable {
	
	public static Observable<Long> interval(long period, TimeUnit unit) {
		return Observable.create(new IntervalImpl(period, unit));
	}
	
	private static class IntervalImpl implements Func1<Observer<Long>, Subscription> {
		// used by subscription thread as well as by this.thread !
		private Set<Observer<Long>> observers =
				Collections.newSetFromMap(new ConcurrentHashMap<Observer<Long>, Boolean>()); 

		Thread thread;
		private long n = 0;

		// called whenever someone subscribes to this observable
		public Subscription call(final Observer<Long> obs) {
			observers.add(obs);
			return new Subscription() {
				public void unsubscribe() {
					observers.remove(obs);
				}
			};
		}
		
		public IntervalImpl(final long period, final TimeUnit unit) {
			// using my own thread is bad, should use schedulers
			thread = new Thread() {
				@Override
				public void run() {
					try {
						while (true) {
							unit.sleep(period);
							n++;
							for (Observer<Long> obs : observers) obs.onNext(n);
						}
					} catch (InterruptedException e) {
						for (Observer<Long> obs : observers) obs.onError(e);
					}
				}
			};
			thread.start();
		}
	}

}
