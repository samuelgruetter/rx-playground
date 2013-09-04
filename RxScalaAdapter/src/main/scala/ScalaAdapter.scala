
import rx.{Observable => JObservable}
import rx.util.functions.Action1
import rx.Subscription
import ImplicitFunctionConversions._
import rx.util.BufferClosing
import rx.util.BufferOpening
import rx.observables.GroupedObservable
import rx.Scheduler
import rx.Observer
import rx.util.functions.Func2

/*
 * TODO:
 * - once/if RxJava has covariant Observable, make ScalaObservable covariant, too
 * - many TODOs below
 */
object ScalaAdapter {

  object Observable {    
    // it should be possible to use these conversions implicitly, 
    // but somehow it does not work and they have to be called explicitly
    import scala.collection.JavaConversions._
    
    def apply[T](args: T*): JObservable[T] = {     
      JObservable.from(asJavaIterable(args.toIterable))
    }
    
    def apply[T](iterable: Iterable[T]): JObservable[T] = {
      JObservable.from(asJavaIterable(iterable))
    }
    
    def apply() = {
      JObservable.empty()
    }
    
    // TODO: apply() to construct from Scala futures, converting them first to Java futures
    
    def apply[T](func: Observer[T] => Subscription): JObservable[T] = {
      JObservable.create(func);
    }
    
    def apply(exception: Throwable): JObservable[_] = {
      JObservable.error(exception)
    }
    
    // call by name
    def defer[T](observable: => JObservable[T]): JObservable[T] = {
      JObservable.defer(observable)
    }
    
    // These are the static methods of JObservable that we don't need here:
    
    // concat         because it's implemented as instance method ++:
    // zip            because it's an instance method
    
    // These are the static methods of JObservable implemented as overloads of apply():
    
    // 	<T> Observable<T> create(Func1<Observer<T>,Subscription> func)
    // 	<T> Observable<T> empty()
    // 	<T> Observable<T> error(java.lang.Throwable exception)    
    // 	<T> Observable<T> from(java.util.concurrent.Future<T> future)
    // 	<T> Observable<T> from(java.util.concurrent.Future<T> future, long timeout, java.util.concurrent.TimeUnit unit)
    // 	<T> Observable<T> from(java.util.concurrent.Future<T> future, Scheduler scheduler)
    // 	<T> Observable<T> from(java.lang.Iterable<T> iterable)
    // 	<T> Observable<T> from(T... items)
    // 	<T> Observable<T> just(T value)
    
    // TODO decide on these static methods how to bring them to Scala:
    
    // 	<R,T0,T1> Observable<R>	      combineLatest(Observable<T0> w0, Observable<T1> w1, Func2<T0,T1,R> combineFunction)
    // 	<R,T0,T1,T2> Observable<R>	  combineLatest(Observable<T0> w0, Observable<T1> w1, Observable<T2> w2, Func3<T0,T1,T2,R> combineFunction)
    // 	<R,T0,T1,T2,T3> Observable<R> combineLatest(Observable<T0> w0, Observable<T1> w1, Observable<T2> w2, Observable<T3> w3, Func4<T0,T1,T2,T3,R> combineFunction)
    // 	<T> Observable<T>             merge(java.util.List<Observable<T>> source)
    // 	<T> Observable<T>             merge(Observable<Observable<T>> source)
    // 	<T> Observable<T>             merge(Observable<T>... source)
    // 	<T> Observable<T>             mergeDelayError(java.util.List<Observable<T>> source)
    // 	<T> Observable<T>             mergeDelayError(Observable<Observable<T>> source)
    // 	<T> Observable<T>             mergeDelayError(Observable<T>... source)
    // 	<T> Observable<T>             never()
    //  Observable<java.lang.Integer> range(int start, int count)
    // 	<T> Observable<Boolean>       sequenceEqual(Observable<T> first, Observable<T> second)
    // 	<T> Observable<Boolean>       sequenceEqual(Observable<T> first, Observable<T> second, Func2<T,T,java.lang.Boolean> equality)
    // 	<T> Observable<T>             switchDo(Observable<Observable<T>> sequenceOfSequences)
    // 	<T> Observable<T>             synchronize(Observable<T> observable)

  }
  
  // zero runtime overhead because it's a value class :)
  // return types are always types from the Java world, i.e. types such as
  // JObservable[T], JObservable[java.util.List[T]]
  implicit class ScalaObservable[T](val wrapped: JObservable[T]) extends AnyVal {

    ////////////// ScalaObservable Section 1 ///////////////////////////////////
    
    /* Section 1 contains methods that have different signatures in Scala than
     * in RxJava */
    
    // TODO aggregate works differently in Scala
    
    // use Scala naming
    def forall(predicate: T => scala.Boolean): JObservable[java.lang.Boolean] = {
      wrapped.all(predicate)
    }
    
    def drop(n: Int): JObservable[T] = {
      wrapped.skip(n)
    }
    
    // no mapMany, because that's flatMap   
    
    // no reduce[R](initialValue: R, accumulator: (R, T) => R): JObservable[R] 
    // because that's called fold in Scala, and it's curried
    def fold[R](initialValue: R)(accumulator: (R, T) => R): JObservable[R] = {
      wrapped.fold(initialValue)(accumulator)
    }
    
    // no scan(accumulator: (T, T) => T ): JObservable[T] 
    // because Scala does not have scan without initial value

    // scan with initial value is curried in Scala
    def scan[R](initialValue: R)(accumulator: (R, T) => R): JObservable[R] = {
      wrapped.scan(initialValue, accumulator)
    }
    
    // TODO is this what we want?
    def foreach(f: T => Unit): Unit = wrapped.toBlockingObservable.forEach(f)
    
    def withFilter(p: T => Boolean): WithFilter[T] = new WithFilter[T](p, wrapped)
      
    // TODO: make zipWithIndex method
    
    // TODO: if we have zipWithIndex, takeWhileWithIndex is not needed any more
    def takeWhileWithIndex(predicate: (T, Integer) => Boolean): JObservable[T] = {
      wrapped.takeWhileWithIndex(predicate)
    }
    
    // where is not needed because it's called filter in Scala
    
    // we want zip as an instance method
    def zip[U](that: JObservable[U]): JObservable[(T, U)] = {
      JObservable.zip(wrapped, that, (t: T, u: U) => (t, u))
    }
    
    
    ////////////// ScalaObservable Section 2 ///////////////////////////////////
    
    /* Section 2 is just boilerplate code: it contains all instance methods of 
     * JObservable which take Func or Action as argument and don't need any
     * other change in their signature */  
    
    // It would be nice to have Section 2 in a trait ScalaObservableBoilerplate[T],
    // but traits cannot (yet) be mixed into value classes (unless someone shows me how ;-) )
    
    // Once the bug that parameter type inference of implicitly converted functions does not work 
    // in Scala 2.10 is fixed or Scala 2.10 is obsolete, we can see whether we can throw away Section 2
    // by requiring that RxScala users import ImplicitFunctionConversions._
    // Bug link: https://issues.scala-lang.org/browse/SI-6221

    def buffer(bufferClosingSelector: () => JObservable[BufferClosing]): JObservable[java.util.List[T]] = {
      wrapped.buffer(bufferClosingSelector)
    }

    def buffer(bufferOpenings: JObservable[BufferOpening], bufferClosingSelector: BufferOpening => JObservable[BufferClosing]): JObservable[java.util.List[T]] = {
      wrapped.buffer(bufferOpenings, bufferClosingSelector)
    }

    def filter(predicate: T => Boolean): JObservable[T] = {
      wrapped.filter(predicate)
    }

    def finallyDo(action: () => Unit): JObservable[T] = {
      wrapped.finallyDo(action)
    }

    def flatMap[R](func: T => JObservable[R]): JObservable[R] = {
      wrapped.flatMap(func)
    }

    def groupBy[K](keySelector: T => K ): JObservable[GroupedObservable[K,T]] = {
      wrapped.groupBy(keySelector)
    }

    def groupBy[K,R](keySelector: T => K, elementSelector: T => R ): JObservable[GroupedObservable[K,R]] = {
      wrapped.groupBy(keySelector, elementSelector)
    }

    def map[R](func: T => R): JObservable[R] = {
      wrapped.map(func)
    }

    def onErrorResumeNext(resumeFunction: Throwable => JObservable[T]): JObservable[T] = {
      wrapped.onErrorResumeNext(resumeFunction)
    }

    def onErrorReturn(resumeFunction: Throwable => T): JObservable[T] = {
      wrapped.onErrorReturn(resumeFunction)
    }

    def reduce(accumulator: (T, T) => T): JObservable[T] = {
      wrapped.reduce(accumulator)
    }

    def subscribe(onNext: T => Unit): Subscription = {
      wrapped.subscribe(onNext)
    }

    def subscribe(onNext: T => Unit, onError: Throwable => Unit): Subscription = {
      wrapped.subscribe(onNext, onError)
    }

    def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: () => Unit): Subscription = {
      wrapped.subscribe(onNext, onError, onComplete)
    }

    def subscribe(onNext: T => Unit, onError: Throwable => Unit, onComplete: () => Unit, scheduler: Scheduler): Subscription = {
      wrapped.subscribe(onNext, onError, onComplete, scheduler)
    }

    def subscribe(onNext: T => Unit, onError: Throwable => Unit, scheduler: Scheduler): Subscription = {
      wrapped.subscribe(onNext, onError, scheduler)
    }

    def subscribe(onNext: T => Unit, scheduler: Scheduler): Subscription = {
      wrapped.subscribe(onNext, scheduler)
    }

    def takeWhile(predicate: T => Boolean): JObservable[T] = {
      wrapped.takeWhile(predicate)
    }

    def toSortedList(sortFunction: (T, T) => Integer): JObservable[java.util.List[T]] = {
      wrapped.toSortedList(sortFunction)
    }
    
    ////////////// ScalaObservable Section 3 ///////////////////////////////////
    
    /* Section 3 contains all instance methods of JObservable which do not take
     * Func or Action as arguments. Since they can be used directly in Scala,
     * this section is empty. */
    
  }
  
  // Cannot yet have inner class because of this error message:
  // implementation restriction: nested class is not allowed in value class.
  // This restriction is planned to be removed in subsequent releases.  
  class WithFilter[T](p: T => Boolean, wrapped: rx.Observable[T]) {
    def map[B](f: T => B): JObservable[B] = wrapped.filter(p).map(f)
    def flatMap[B](f: T => JObservable[B]): JObservable[B] = wrapped.filter(p).flatMap(f)
    def foreach(f: T => Unit): Unit = wrapped.filter(p).toBlockingObservable.forEach(f)
    def withFilter(p: T => Boolean): JObservable[T] = wrapped.filter(p)
  }
  
     
}

// TODO: tests:

/*
import org.scalatest.junit.JUnitSuite

class UnitTestSuite extends JUnitSuite {

}
*/



