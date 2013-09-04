

import rx.util.functions.Action1
import rx.Subscription

// it's better not to import rx.Observable, because there might be confusion with object Observable
// being defined in this file

object ScalaAdapter {
  import ImplicitFunctionConversions._

  // zero runtime overhead because it's a value class :)
  implicit class ScalaObservable[T](val wrapped: rx.Observable[T]) extends AnyVal {
    import rx.Observable
	def subscribe(onNext: T => Unit): Subscription = {
	  wrapped.subscribe(scalaFunction1ProducingUnitToAction1(onNext))
	}
	
	// adapted from
	// https://github.com/Netflix/RxJava/blob/master/language-adaptors/rxjava-scala/src/main/scala/rx/lang/scala/RxImplicits.scala
	/**
     * This implicit class implements all of the methods necessary for including Observables in a
     * for-comprehension.  Note that return type is always Observable, so that the ScalaObservable
     * type never escapes the for-comprehension
     */
    def map[B](f: T => B): Observable[B] = wrapped.map(scalaFunction1ToRxFunc1(f))
    def flatMap[B](f: T => Observable[B]): Observable[B] = wrapped.mapMany(f)
    def foreach(f: T => Unit): Unit = wrapped.toBlockingObservable.forEach(f)
    def withFilter(p: T => Boolean): WithFilter[T] = new WithFilter[T](p, wrapped)
  }
  
  // Cannot yet have inner class because of this error message:
  // implementation restriction: nested class is not allowed in value class.
  // This restriction is planned to be removed in subsequent releases.  
  class WithFilter[T](p: T => Boolean, wrapped: rx.Observable[T]) {
    import rx.Observable
    def map[B](f: T => B): Observable[B] = wrapped.filter(p).map(f)
    def flatMap[B](f: T => Observable[B]): Observable[B] = wrapped.filter(p).flatMap(f)
    def foreach(f: T => Unit): Unit = wrapped.filter(p).toBlockingObservable.forEach(f)
    def withFilter(p: T => Boolean): Observable[T] = wrapped.filter(p)
  }
  
  
  
  object Observable {
    // here we do not import rx.Observable!
    
    // these should be implicit, but somehow it does not work and they have to be called explicitly
    import scala.collection.JavaConversions._
    
    def apply[T](args: T*): rx.Observable[T] = {
      rx.Observable.from(asJavaIterable(args.toIterable))
    }
  }
     
}
