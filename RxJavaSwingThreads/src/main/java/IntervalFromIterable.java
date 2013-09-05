import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import rx.Observable;

// new try to get interval()
// Problem:
// javadoc of Observable.from: 
// the entire iterable sequence will be immediately emitted each time an Observer subscribes.
// => this first creates all naturals eagerly => does not work :(
public class IntervalFromIterable {
	
	// would be shorter in Scala ;)
	private static Iterable<Long> naturals = new Iterable<Long>() {
		public Iterator<Long> iterator() {
			return new Iterator<Long>() {
				private long n = 0;
				public boolean hasNext() {
					return true;
				}
				public Long next() {
					return n++;
				}
				public void remove() {
					throw new UnsupportedOperationException();					
				}
			};
		}
	};
	
	public static Observable<Long> interval(long period, TimeUnit unit) {
		return Observable.from(naturals).sample(1, TimeUnit.SECONDS);
	}

}
