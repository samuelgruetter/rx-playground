
package rx.lang.scala

/*
 * Note: Unit tests below cannot be run with sbt, I run them from Eclipse.
 * 
 * TODO:
 * - once/if RxJava has covariant Observable, make ScalaObservable covariant, too
 * - many TODOs below
 */

// compiler crashes, so everything is commented out...
/*
object Observable {  

  import scala.collection.JavaConverters._
  import rx.{Observable => JObservable}
  import rx.observables.{BlockingObservable => JBlockingObservable}
  import rx.util.functions.Action1
  import rx.Subscription
  import rx.lang.scala.ImplicitFunctionConversions._
  import rx.util.BufferClosing
  import rx.util.BufferOpening
  import rx.observables.GroupedObservable
  import rx.Scheduler
  import rx.Observer
  import rx.util.functions.Func1
  import rx.util.functions.Func2

  def apply[T](args: T*): Observable[T] = {     
    new Observable(JObservable.from(args.toIterable.asJava))
  }
  
  def apply[T](iterable: Iterable[T]): Observable[T] = {
    new Observable(JObservable.from(iterable.asJava))
  }
  
  // apply() is not needed because it's a special case of apply[T](args: T*)
    
  // TODO: apply() to construct from Scala futures, converting them first to Java futures
  
  def apply[T](func: Observer[T] => Subscription): Observable[T] = {
    new Observable(JObservable.create(func))
  }
  
  def apply(exception: Throwable): Observable[Nothing] = {
    new Observable(JObservable.error(exception))
  }
  
  // call by name
  def defer[T](observable: => Observable[T]): Observable[T] = {
    new Observable(JObservable.defer(observable.wrapped))
  }
  
  // These are the static methods of JObservable that we don't need here:
  
  // concat         because it's implemented as instance method ++:
  // zip            because it's an instance method
  
  // These are the static methods of JObservable implemented as overloads of apply():
  
  //   <T> Observable<T> create(Func1<Observer<T>,Subscription> func)
  //   <T> Observable<T> empty()
  //   <T> Observable<T> error(java.lang.Throwable exception)    
  //   <T> Observable<T> from(java.util.concurrent.Future<T> future)
  //   <T> Observable<T> from(java.util.concurrent.Future<T> future, long timeout, java.util.concurrent.TimeUnit unit)
  //   <T> Observable<T> from(java.util.concurrent.Future<T> future, Scheduler scheduler)
  //   <T> Observable<T> from(java.lang.Iterable<T> iterable)
  //   <T> Observable<T> from(T... items)
  //   <T> Observable<T> just(T value)
  
  // TODO decide on these static methods how to bring them to Scala:
  
  //   <R,T0,T1> Observable<R>        combineLatest(Observable<T0> w0, Observable<T1> w1, Func2<T0,T1,R> combineFunction)
  //   <R,T0,T1,T2> Observable<R>    combineLatest(Observable<T0> w0, Observable<T1> w1, Observable<T2> w2, Func3<T0,T1,T2,R> combineFunction)
  //   <R,T0,T1,T2,T3> Observable<R> combineLatest(Observable<T0> w0, Observable<T1> w1, Observable<T2> w2, Observable<T3> w3, Func4<T0,T1,T2,T3,R> combineFunction)
  //   <T> Observable<T>             merge(java.util.List<Observable<T>> source)
  //   <T> Observable<T>             merge(Observable<Observable<T>> source)
  //   <T> Observable<T>             merge(Observable<T>... source)
  //   <T> Observable<T>             mergeDelayError(java.util.List<Observable<T>> source)
  //   <T> Observable<T>             mergeDelayError(Observable<Observable<T>> source)
  //   <T> Observable<T>             mergeDelayError(Observable<T>... source)
  //   <T> Observable<T>             never()
  //  Observable<java.lang.Integer> range(int start, int count)
  //   <T> Observable<Boolean>       sequenceEqual(Observable<T> first, Observable<T> second)
  //   <T> Observable<Boolean>       sequenceEqual(Observable<T> first, Observable<T> second, Func2<T,T,java.lang.Boolean> equality)
  //   <T> Observable<T>             switchDo(Observable<Observable<T>> sequenceOfSequences)
  //   <T> Observable<T>             synchronize(Observable<T> observable)
  
}
  
class Observable[+T](val wrapped: rx.Observable[_ <: T]) extends AnyVal {
  import scala.collection.JavaConverters._
  import rx.{Observable => JObservable}
  import rx.observables.{BlockingObservable => JBlockingObservable}
  import rx.util.functions.Action1
  import rx.Subscription
  import rx.lang.scala.ImplicitFunctionConversions._
  import rx.util.BufferClosing
  import rx.util.BufferOpening
  import rx.observables.GroupedObservable
  import rx.Scheduler
  import rx.Observer
  import rx.util.functions.Func1
  import rx.util.functions.Func2
  
  ////////////// ScalaObservable Section 1 ///////////////////////////////////
  
  /* Section 1 contains methods that have different signatures in Scala than
   * in RxJava */
  
  // TODO aggregate works differently in Scala
  
  // use Scala naming
  def forall(predicate: T => scala.Boolean): Observable[java.lang.Boolean] = {
    new Observable(wrapped.all(predicate))
  }
  
  def drop(n: Int): Observable[T] = {
    new Observable(wrapped.skip(n))
  }
  
  // no mapMany, because that's flatMap   
  
  // no reduce[R](initialValue: R, accumulator: (R, T) => R): Observable[R] 
  // because that's called fold in Scala, and it's curried
  def fold[R](initialValue: R)(accumulator: (R, T) => R): Observable[R] = {
    new Observable(wrapped.reduce(initialValue, accumulator))
  }
  
  // no scan(accumulator: (T, T) => T ): Observable[T] 
  // because Scala does not have scan without initial value
  
  // scan with initial value is curried in Scala
  def scan[R](initialValue: R)(accumulator: (R, T) => R): Observable[R] = {
    new Observable(wrapped.scan(initialValue, accumulator))
  }
  
  // TODO is this what we want?
  def foreach(f: T => Unit): Unit = wrapped.toBlockingObservable.forEach(f)
  
  def withFilter(p: T => Boolean): WithFilter[T] = new WithFilter[T](p, wrapped)
    
  // TODO: make zipWithIndex method
  
  // TODO: if we have zipWithIndex, takeWhileWithIndex is not needed any more
  def takeWhileWithIndex(predicate: (T, Integer) => Boolean): Observable[T] = {
    new Observable(wrapped.takeWhileWithIndex(predicate))
  }
  
  // where is not needed because it's called filter in Scala
  
  // we want zip as an instance method
  def zip[U](that: Observable[U]): Observable[(T, U)] = {
    // new Observable(JObservable.zip(wrapped, that.wrapped, (t: T, u: U) => (t, u))) TODO
    ???    
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
  
  def buffer(bufferClosingSelector: () => JObservable[BufferClosing]): Observable[java.util.List[T]] = {
    //new Observable(wrapped.buffer(bufferClosingSelector)) TODO
    ???    
  }
  
  def buffer(bufferOpenings: Observable[BufferOpening], bufferClosingSelector: BufferOpening => JObservable[BufferClosing]): Observable[java.util.List[T]] = {
    // new Observable(wrapped.buffer(bufferOpenings.wrapped, bufferClosingSelector)) TODO
    ???
  }
  
  def filter(predicate: T => Boolean): Observable[T] = {
    new Observable(wrapped.filter(predicate))
  }
  
  def finallyDo(action: () => Unit): Observable[T] = {
    new Observable(wrapped.finallyDo(action))
  }
  
  def flatMap[R](func: T => JObservable[R]): Observable[R] = {
    new Observable(wrapped.flatMap(func))
  }
  
  def groupBy[K](keySelector: T => K ): Observable[GroupedObservable[K,T]] = {
    // new Observable(wrapped.groupBy(keySelector)) TODO
    ???
  }
  
  def groupBy[K,R](keySelector: T => K, elementSelector: T => R ): Observable[GroupedObservable[K,R]] = {
    new Observable(wrapped.groupBy(keySelector, elementSelector))
  }
  
  def map[R](func: T => R): Observable[R] = {
    new Observable(wrapped.map(func))
  }
  
  def map2[R](func: T => R): Observable[R] = {
    new Observable(wrapped.map(func))
  }
  
  def onErrorResumeNext(resumeFunction: Throwable => Observable[T]): Observable[T] = {
    // new Observable(wrapped.onErrorResumeNext((t: Throwable) => resumeFunction(t).wrapped))
    ???
  }
  
  def onErrorResumeNext(resumeObservable: Observable[T]): Observable[T] = {
    // new Observable(wrapped.onErrorResumeNext(resumeObservable.wrapped)) TODO
    ???
  }
  
  def onErrorReturn(resumeFunction: Throwable => T): Observable[T] = {
    // new Observable(wrapped.onErrorReturn(resumeFunction)) TODO
    ???
  }
  
  def reduce(accumulator: (T, T) => T): Observable[T] = {
    // new Observable(wrapped.reduce(accumulator)) TODO
    ???
  }
  
  def subscribe(obs: Observer[_ >: T]): Subscription = {
    // wrapped.subscribe(obs) TODO
    ???
  }
  
  def subscribe(obs: Observer[_ >: T], scheduler: Scheduler): Subscription = {
    // wrapped.subscribe(obs, scheduler)
    ???
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
  
  def takeWhile(predicate: T => Boolean): Observable[T] = {
    new Observable(wrapped.takeWhile(predicate))
  }
  
  // TODO do we need this? Scala has its own sort functions
  // TODO do we return java.util.List or scala list?
  /*def toSortedList(sortFunction: (T, T) => Integer): Observable[java.util.List[T]] = {
    new Observable(wrapped.toSortedList(sortFunction))
  }*/
  
  ////////////// ScalaObservable Section 3 ///////////////////////////////////
  
  /* Section 3 contains all instance methods of JObservable which do not take
   * Func or Action as arguments.  */
  
  // TODO
  
  // Observable<java.util.List<T>>  buffer(int count)
  // Observable<java.util.List<T>>  buffer(int count, int skip)
  // Observable<java.util.List<T>>  buffer(long timespan, long timeshift, java.util.concurrent.TimeUnit unit)
  // Observable<java.util.List<T>>  buffer(long timespan, long timeshift, java.util.concurrent.TimeUnit unit, Scheduler scheduler)
  // Observable<java.util.List<T>>  buffer(long timespan, java.util.concurrent.TimeUnit unit)
  // Observable<java.util.List<T>>  buffer(long timespan, java.util.concurrent.TimeUnit unit, int count)
  // Observable<java.util.List<T>>  buffer(long timespan, java.util.concurrent.TimeUnit unit, int count, Scheduler scheduler)
  // Observable<java.util.List<T>>  buffer(long timespan, java.util.concurrent.TimeUnit unit, Scheduler scheduler)
  // Observable<T>  cache()
  // <T2> Observable<T2>  dematerialize()
  // Observable<Notification<T>>  materialize()
  // <R> ConnectableObservable<R>  multicast(Subject<T,R> subject)
  // Observable<T>  observeOn(Scheduler scheduler)
  // Observable<T>  onErrorResumeNext(Observable<T> resumeSequence)
  // Observable<T>  onExceptionResumeNext(Observable<T> resumeSequence)
  // ConnectableObservable<T>  publish()
  // ConnectableObservable<T>  replay()
  // Observable<T>  sample(long period, java.util.concurrent.TimeUnit unit)
  // Observable<T>  sample(long period, java.util.concurrent.TimeUnit unit, Scheduler scheduler)
  // Observable<T>  skip(int num)
  // Observable<T>  startWith(T... values)
  // Subscription  subscribe(Observer<T> observer)
  // Subscription  subscribe(Observer<T> observer, Scheduler scheduler)
  // Observable<T>  subscribeOn(Scheduler scheduler)
  // Observable<T>  switchDo()
  // Observable<T>  take(int num)
  // Observable<T>  takeLast(int count)
  // <E> Observable<T>  takeUntil(Observable<E> other)
  // Observable<Timestamped<T>>  timestamp()
  // BlockingObservable<T>  toBlockingObservable()
  
  def toBlockingObservable: BlockingObservable[T] = {
    // new BlockingObservable(wrapped.toBlockingObservable()) TODO
    ???
  }
  
  // Observable<java.util.List<T>>  toList()
  // Observable<java.util.List<T>>  toSortedList()
  
      
}
    
// Cannot yet have inner class because of this error message:
// implementation restriction: nested class is not allowed in value class.
// This restriction is planned to be removed in subsequent releases.  
class WithFilter[+T] private[scala] (p: T => Boolean, wrapped: rx.Observable[_ <: T]) {
  import ImplicitFunctionConversions._
  
  def map[B](f: T => B): Observable[B] = new Observable(wrapped.filter(p).map(f))
  def flatMap[B](f: T => Observable[B]): Observable[B] = {
    val filtered: rx.Observable[_ <: T] = wrapped.filter(p)
    val func: rx.util.functions.Func1[_ >: T, _ <: rx.Observable[_ <: B]] = (x: T) => f(x).wrapped
    // TODO only works if Java's flatMap has correct signature
    //filtered.flatMap(func)
    ???
  }
  def foreach(f: T => Unit): Unit = wrapped.filter(p).toBlockingObservable.forEach(f)
  def withFilter(p: T => Boolean): Observable[T] = new Observable(wrapped.filter(p))
}
  
class BlockingObservable[T](val wrapped: rx.observables.BlockingObservable[T]) extends AnyVal {
  import ImplicitFunctionConversions._
  
  def single: T = {
    wrapped.single()
  }
  
  def single(predicate: T => Boolean): T = { 
    wrapped.single(predicate)
  }
  
  def last: T = {
    wrapped.last()
  }
  
  // TODO all other members of this class

}

import org.scalatest.junit.JUnitSuite

class UnitTestSuite extends JUnitSuite {

  import org.junit.{ Before, Test }
  import org.junit.Assert._
  import org.mockito.Matchers.any
  import org.mockito.Mockito._
  import org.mockito.{ MockitoAnnotations, Mock }
  import rx.{ Notification, Observer, Subscription }
  import rx.observables.GroupedObservable
  import collection.mutable.ArrayBuffer
  import collection.JavaConverters._

  // Tests which needn't be run:
  
  def testSubtyping = {
    val o1: Observable[Nothing] = Observable()
    val o2: Observable[Int] = o1
    val o3: Observable[App] = o1
    val o4: Observable[Any] = o2
    val o5: Observable[Any] = o3
  }
  
  // Tests which have to be run:
  
  @Mock private[this] val observer: Observer[Any] = null

  @Mock private[this] val subscription: Subscription = null

  val isOdd = (i: Int) => i % 2 == 1
  val isEven = (i: Int) => i % 2 == 0

  def ObservableWithException(s: Subscription, values: String*): Observable[String] =
    Observable((o: Observer[String]) => subscribeFunc1(s, values: _*)(o))

  def subscribeFunc1(s: Subscription, values: String*)(observer: Observer[String]): Subscription = {
    println("ObservableWithException subscribed to ...")
    val t = new Thread(new Runnable() {
      override def run() {
        try {
          println("running ObservableWithException thread")
          values.toList.foreach(v => {
            println("ObservableWithException onNext: " + v)
            observer.onNext(v)
          })
          throw new RuntimeException("Forced Failure")
        } catch {
          case ex: Exception => observer.onError(ex)
        }
      }
    })
    println("starting ObservableWithException thread")
    t.start()
    println("done starting ObservableWithException thread")
    s
  }

  @Before def before {
    MockitoAnnotations.initMocks(this)
  }

  // tests of static methods

  @Test def testSingle {
    assertEquals(1, Observable(1).toBlockingObservable.single)
  }

  @Test def testSinglePredicate {
    val found = Observable(1, 2, 3).toBlockingObservable.single(isEven)
    assertEquals(2, found)
  }

  @Test def testSingleOption {
    //TODO
    /*
    assertEquals(0, Observable().toBlockingObservable.singleOrDefault(0))
    assertEquals(1, Observable(1).toBlockingObservable.singleOrDefault(0))
    try {
      Observable(1, 2, 3).toBlockingObservable.singleOrDefault(0)
      fail("Did not catch any exception, expected IllegalStateException")
    } catch {
      case ex: IllegalStateException => println("Caught expected IllegalStateException")
      case ex: Throwable => fail("Caught unexpected exception " + ex.getCause + ", expected IllegalStateException")
    }
    */
  }

  @Test def testSingleOptionPredicate {
    // TODO
    /*
    assertEquals(2, Observable(1, 2, 3).toBlockingObservable.singleOrDefault(0, isEven))
    assertEquals(0, Observable(1, 3).toBlockingObservable.singleOrDefault(0, isEven))
    try {
      Observable(1, 2, 3).toBlockingObservable.singleOrDefault(0, isOdd)
      fail("Did not catch any exception, expected IllegalStateException")
    } catch {
      case ex: IllegalStateException => println("Caught expected IllegalStateException")
      case ex: Throwable => fail("Caught unexpected exception " + ex.getCause + ", expected IllegalStateException")
    }
    */
  }

  // TODO this does not compile. Should it?
  /*
  @Test def testFromJavaInterop {
    val observable: Observable[Int] = Observable(List(1, 2, 3).asJava)
    assertSubscribeReceives(observable)(1, 2, 3)
  }
  */

  @Test def testSubscribe {
    val observable = Observable("1", "2", "3")
    assertSubscribeReceives(observable)("1", "2", "3")
  }

  @Test def testDefer {
    val observable = Observable.defer(Observable(1, 2)) // call by name
    assertSubscribeReceives(observable)(1, 2)
  }

  @Test def testJust {
    val observable = Observable("foo")
    assertSubscribeReceives(observable)("foo")
  }

  // TODO implement merge
  /*
  @Test def testMerge {
    val observable1 = Observable(1, 2, 3)
    val observable2 = Observable(4, 5, 6)
    val observableList = List(observable1, observable2).asJava
    val merged = Observable.merge(observableList)
    assertSubscribeReceives(merged)(1, 2, 3, 4, 5, 6)
  }

  @Test def testFlattenMerge {
    val observable = Observable(Observable(1, 2, 3))
    val merged = Observable.merge(observable)
    assertSubscribeReceives(merged)(1, 2, 3)
  }

  @Test def testSequenceMerge {
    val observable1 = Observable(1, 2, 3)
    val observable2 = Observable(4, 5, 6)
    val merged = Observable.merge(observable1, observable2)
    assertSubscribeReceives(merged)(1, 2, 3, 4, 5, 6)
  }
  */

  // TODO implement concat or ++:
  /*
  @Test def testConcat {
    val observable1 = Observable(1, 2, 3)
    val observable2 = Observable(4, 5, 6)
    val concatenated = Observable.concat(observable1, observable2)
    assertSubscribeReceives(concatenated)(1, 2, 3, 4, 5, 6)
  }
  */

  // TODO implement synchronize
  /*
  @Test def testSynchronize {
    val observable = Observable(1, 2, 3)
    val synchronized = Observable.synchronize(observable)
    assertSubscribeReceives(synchronized)(1, 2, 3)
  }
  */

  // TODO test zip

  // there's no such thing as zip3 and zip4 in Scala

  //tests of instance methods

  // missing tests for : takeUntil, groupBy, next, mostRecent

  @Test def testFilter {
    val numbers = Observable(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val observable = numbers.filter(isEven)
    assertSubscribeReceives(observable)(2, 4, 6, 8)
  }

  @Test def testLast {
    val observable = Observable(1, 2, 3, 4)
    assertEquals(4, observable.toBlockingObservable.last)
  }

  @Test def testLastPredicate {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    assertEquals(3, observable.toBlockingObservable.last(isOdd))
    */
  }

  @Test def testLastOption {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    assertEquals(4, observable.toBlockingObservable.lastOrDefault(5))
    assertEquals(5, Observable.from[Int]().toBlockingObservable.lastOrDefault(5))
    */
  }

  @Test def testLastOptionPredicate {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    assertEquals(3, observable.toBlockingObservable.lastOrDefault(5, isOdd))
    assertEquals(5, Observable.from[Int]().toBlockingObservable.lastOrDefault(5, isOdd))
    */
  }

  @Test def testMap {
    val numbers = Observable(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val mappedNumbers = ArrayBuffer.empty[Int]
    numbers.map((x: Int) => x * x).subscribe((squareVal: Int) => {
      mappedNumbers.append(squareVal)
    })
    assertEquals(List(1, 4, 9, 16, 25, 36, 49, 64, 81), mappedNumbers.toList)
  }

  @Test def testFlatMap {
    // TODO
    /*
    val numbers = Observable(1, 2, 3, 4)
    val f = (i: Int) => Observable(List(i, -i).asJava)
    val mappedNumbers = ArrayBuffer.empty[Int]
    numbers.mapMany(f).subscribe((i: Int) => {
      mappedNumbers.append(i)
    })
    assertEquals(List(1, -1, 2, -2, 3, -3, 4, -4), mappedNumbers.toList)
    */
  }

  @Test def testMaterialize {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    val expectedNotifications: List[Notification[Int]] =
      ((1.to(4).map(i => new Notification(i))) :+ new Notification()).toList
    val actualNotifications: ArrayBuffer[Notification[Int]] = ArrayBuffer.empty
    observable.materialize.subscribe((n: Notification[Int]) => {
      actualNotifications.append(n)
    })
    assertEquals(expectedNotifications, actualNotifications.toList)
    */
  }

  @Test def testDematerialize {
    // TODO
    /*
    val notifications: List[Notification[Int]] =
      ((1.to(4).map(i => new Notification(i))) :+ new Notification()).toList
    val observableNotifications: Observable[Notification[Int]] =
      Observable(notifications)
    val observable: Observable[Int] =
      observableNotifications.dematerialize()
    assertSubscribeReceives(observable)(1, 2, 3, 4)
    */
  }

  @Test def testOnErrorResumeNextObservableNoError {
    val observable = Observable(1, 2, 3, 4)
    val resumeObservable = Observable(5, 6, 7, 8)
    val observableWithErrorHandler = observable.onErrorResumeNext(resumeObservable)
    assertSubscribeReceives(observableWithErrorHandler)(1, 2, 3, 4)
  }

  @Test def testOnErrorResumeNextObservableErrorOccurs {
    /*
    val observable = ObservableWithException(subscription, "foo", "bar")
    val resumeObservable = Observable("a", "b", "c", "d")
    val observableWithErrorHandler = observable.onErrorResumeNext(resumeObservable)
    observableWithErrorHandler.subscribe(observer.asInstanceOf[Observer[String]])

    try {
      // TODO expose the thread somehow
      observable.t.join()
    } catch {
      case ex: InterruptedException => fail(ex.getMessage)
    }

    List("foo", "bar", "a", "b", "c", "d").foreach(t => verify(observer, times(1)).onNext(t))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
    */
  }

  @Test def testOnErrorResumeNextFuncNoError {
    val observable = Observable(1, 2, 3, 4)
    val resumeFunc = (ex: Throwable) => Observable(5, 6, 7, 8)
    val observableWithErrorHandler = observable.onErrorResumeNext(resumeFunc)
    assertSubscribeReceives(observableWithErrorHandler)(1, 2, 3, 4)
  }

  @Test def testOnErrorResumeNextFuncErrorOccurs {
    /*
    val observable = ObservableWithException(subscription, "foo", "bar")
    val resumeFunc = (ex: Throwable) => Observable("a", "b", "c", "d")
    val observableWithErrorHandler = observable.onErrorResumeNext(resumeFunc)
    observableWithErrorHandler.subscribe(observer.asInstanceOf[Observer[String]])

    try {
      // TODO same as above
      observable.t.join()
    } catch {
      case ex: InterruptedException => fail(ex.getMessage)
    }

    List("foo", "bar", "a", "b", "c", "d").foreach(t => verify(observer, times(1)).onNext(t))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
    */
  }

  @Test def testOnErrorReturnFuncNoError {
    val observable = Observable(1, 2, 3, 4)
    val returnFunc = (ex: Throwable) => 87
    val observableWithErrorHandler = observable.onErrorReturn(returnFunc)
    assertSubscribeReceives(observableWithErrorHandler)(1, 2, 3, 4)
  }

  @Test def testOnErrorReturnFuncErrorOccurs {
    /*
    val observable = ObservableWithException(subscription, "foo", "bar")
    val returnFunc = (ex: Throwable) => "baz"
    val observableWithErrorHandler = observable.onErrorReturn(returnFunc)
    observableWithErrorHandler.subscribe(observer.asInstanceOf[Observer[String]])

    try {
      observable.t.join()
    } catch {
      case ex: InterruptedException => fail(ex.getMessage)
    }

    List("foo", "bar", "baz").foreach(t => verify(observer, times(1)).onNext(t))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
    */
  }

  @Test def testReduce {
    val observable = Observable(1, 2, 3, 4)
    assertEquals(10, observable.reduce((a: Int, b: Int) => a + b).toBlockingObservable.single)
  }

  @Test def testDrop {
    val observable = Observable(1, 2, 3, 4)
    val skipped = observable.drop(2)
    assertSubscribeReceives(skipped)(3, 4)
  }

  /**
   * Both testTake and testTakeWhileWithIndex exposed a bug with unsubscribes not properly propagating.
   * observable.take(2) produces onNext(first), onNext(second), and 4 onCompleteds
   * it should produce onNext(first), onNext(second), and 1 onCompleted
   *
   * Switching to Observable.create(OperationTake.take(observable, 2)) works as expected
   */
  @Test def testTake {
    // TODO
    /*
    import rx.operators._

    val observable = Observable(1, 2, 3, 4, 5)
    val took = Observable.create(OperationTake.take(observable, 2))
    assertSubscribeReceives(took)(1, 2)
    */
  }

  @Test def testTakeWhile {
    val observable = Observable(1, 3, 5, 6, 7, 9, 11)
    val took = observable.takeWhile(isOdd)
    assertSubscribeReceives(took)(1, 3, 5)
  }

  /*
  @Test def testTakeWhileWithIndex {
    val observable = Observable(1, 3, 5, 6, 7, 9, 11, 12, 13, 15, 17)
    val took = observable.takeWhileWithIndex((i: Int, idx: Int) => isOdd(i) && idx > 4)
    assertSubscribeReceives(took)(9, 11)
  }
  */

  @Test def testTakeRight {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val tookLast = observable.takeLast(3);
    assertSubscribeReceives(tookLast)(7, 8, 9)
    */
  }

  @Test def testToList {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    val toList = observable.toList
    assertSubscribeReceives(toList)(List(1, 2, 3, 4).asJava)
    */
  }

  @Test def testToSortedList {
    // TODO
    /*
    val observable = Observable(1, 3, 4, 2)
    val toSortedList = observable.toSortedList
    assertSubscribeReceives(toSortedList)(List(1, 2, 3, 4).asJava)
    */
  }

  @Test def testToArbitrarySortedList {
    // TODO
    /*
    val observable = Observable("a", "aaa", "aaaa", "aa")
    val sortByLength = (s1: String, s2: String) => s1.length.compareTo(s2.length)
    val toSortedList = observable.toSortedList(sortByLength)
    assertSubscribeReceives(toSortedList)(List("a", "aa", "aaa", "aaaa").asJava)
    */
  }

  @Test def testToIterable {
    // TODO
    /*
    val observable = Observable(1, 2)
    val it = observable.toBlockingObservable.toIterable.iterator
    assertTrue(it.hasNext)
    assertEquals(1, it.next)
    assertTrue(it.hasNext)
    assertEquals(2, it.next)
    assertFalse(it.hasNext)
    */
  }

  @Test def testStartWith {
    // TODO
    /*
    val observable = Observable(1, 2, 3, 4)
    val newStart = observable.startWith(-1, 0)
    assertSubscribeReceives(newStart)(-1, 0, 1, 2, 3, 4)
    */
  }

  @Test def testOneLineForComprehension {
    val mappedObservable = for {
      i: Int <- Observable(1, 2, 3, 4)
    } yield i + 1
    assertSubscribeReceives(mappedObservable)(2, 3, 4, 5)
  }

  @Test def testSimpleMultiLineForComprehension {
    val flatMappedObservable = for {
      i: Int <- Observable(1, 2, 3, 4)
      j: Int <- Observable(1, 10, 100, 1000)
    } yield i + j
    assertSubscribeReceives(flatMappedObservable)(2, 12, 103, 1004)
  }

  @Test def testMultiLineForComprehension {
    val doubler = (i: Int) => Observable(i, i)
    val flatMappedObservable = for {
      i: Int <- Observable(1, 2, 3, 4)
      j: Int <- doubler(i)
    } yield j
    //can't use assertSubscribeReceives since each number comes in 2x
    flatMappedObservable.subscribe(observer.asInstanceOf[Observer[Int]])
    List(1, 2, 3, 4).foreach(i => verify(observer, times(2)).onNext(i))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
  }

  @Test def testFilterInForComprehension {
    val doubler = (i: Int) => Observable(i, i)
    val filteredObservable = for {
      i: Int <- Observable(1, 2, 3, 4)
      j: Int <- doubler(i) if isOdd(i)
    } yield j
    //can't use assertSubscribeReceives since each number comes in 2x
    filteredObservable.subscribe(observer.asInstanceOf[Observer[Int]])
    List(1, 3).foreach(i => verify(observer, times(2)).onNext(i))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
  }

  @Test def testForEachForComprehension {
    val doubler = (i: Int) => Observable(i, i)
    val intBuffer = ArrayBuffer.empty[Int]
    val forEachComprehension = for {
      i: Int <- Observable(1, 2, 3, 4)
      j: Int <- doubler(i) if isEven(i)
    } {
      intBuffer.append(j)
    }
    assertEquals(List(2, 2, 4, 4), intBuffer.toList)
  }

  private def assertSubscribeReceives[T](o: Observable[T])(values: T*) = {
    o.subscribe(observer.asInstanceOf[Observer[T]])
    values.toList.foreach(t => verify(observer, times(1)).onNext(t))
    verify(observer, never()).onError(any(classOf[Exception]))
    verify(observer, times(1)).onCompleted()
  }

}

*/
