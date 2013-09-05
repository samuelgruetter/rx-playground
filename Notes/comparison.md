
Comparison Rx C# vs Rx Java
===========================

This table contains all methods declared in C# System.Reactive.Linq.Observable, as well as all methods declared in Java rx.Observable<T>.

Versions considered here:

*    <http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable(v=vs.103).aspx>
*    rxjava-core-0.10.0-javadoc.jar


Note:

*    In C#, the methods are defined in Observable, but can be called on IObservables (and other classes too), thanks to extension methods
*    To edit, first press <Insert> key on your keyboard ;-)
*    Status column: ok means implemented in Java, D means deprecated in C#, Xs mean not implemented, more Xs mean it's considered more important
*    You can run `./comparison-statistics.sh` to get statistics
*    In order to teach RxJava or RxScala, we must add at least the functionalities marked with XXXX in the table below


Method Name            | C# Receiver          | Java Receiver| Status     | Comment    
:----------------------|:---------------------|:-------------|------------|:--------
Aggregate              | IObservable          | Observable   | ok         | synonym of reduce in Java, both Java and C# have a version with seed value (corresponds to Scala's reduce) and one without (corresponds to Scala's foldLeft)
All                    | IObservable          | Observable   | ok         | Scala's forall
Amb                    | IObs./Obs./IEnum.    | (missing)    | XX         | 
And                    | IObservable          | (missing)    | XX         | 
Any()                  | IObservable          | (missing)    | X          | checks for non-emptyness
Any(predicate)         | IObservable          | (missing)    | ?          | same docu as All -> ??
AsObservable           | IObservable          | (missing)    | X          | Observable -> anonymous Observable, not to be confused with ToObservable, which is for IEnumerables
Average                | IObservable          | (missing)    | X          | 
Buffer                 | IObservable          | Observable   | ok         | 
cache                  | (missing)            | Observable   | ok         | similar to replay
Cast                   | IObservable          | (missing)    | X          | calls onError in case of casting exception
Catch                  | IEnumerable          | (missing)    | XX         | more powerful/general than onErrorNext
Catch                  | IObservable          | (missing)    | XX         | 
CombineLatest          | IObservable          | Observable   | ok         | 
Concat                 | IObs./Obs./IEnum.    | Observable   | ok         | 
Contains               | IObservable          | (missing)    | XX         | 
Count                  | IObservable          | (missing)    | XX         | 
Create                 | Observable           | Observable   | ok         | too "low level" in most cases
DefaultIfEmpty         | IObservable          | (missing)    | XX         | 
Defer                  | Observable           | Observable   | ok         | 
Delay                  | IObservable          | (missing)    | XX         | 
Dematerialize          | IObservable          | Observable   | ok         | 
Distinct               | IObservable          | (missing)    | XX         | 
DistinctUntilChanged   | IObservable          | (missing)    | XX         | 
Do                     | IObservable          | (missing)    | XXXX       | useful for printf-like debugging and understanding, important for teaching
ElementAt              | IObservable          | (missing)    | XX         | 
ElementAtOrDefault     | IObservable          | (missing)    | XX         | 
Empty                  | Observable           | Observable   | ok         | 
error                  | (= Throw)            | Observable   | ok         | 
filter                 | (not needed)         | Observable   | ok         | just the same as Where
Finally/finallyDo      | IObservable          | Observable   | ok         | the same, but different names
First                  | IObservable          | (not needed) | D          | 
FirstOrDefault         | IObservable          | (not needed) | D          | 
flatMap                | (not needed)         | Observable   | ok         | the same as mapMany and SelectMany
ForEach                | IObservable          | (not needed) | D          | blocks until the sequence is terminated
FromAsyncPattern       | Observable           | (not needed) | D          | takes a Begin* delegate taking N arguments and a End* delegate taking 1 result. Not needed in Java because this Begin*/End* pattern is not used in Java. And it's deprecated in Rx, nowadays, use TaskFactory.FromAsync to create a Task.
FromEvent              | Observable           | (missing)    | ok         | in Java: use SwingObservable.from*Events
FromEventPattern       | Observable           | (missing)    | ok         | in Java: use SwingObservable.from*Events
from*Events            | (not needed)         | SwingObserv. | ok         | corresponds to C# FromEvent/FromEventPattern
from                   | (not needed)         | Observable   | ok         | from Iterable, Future, varargs
Generate               | Observable           | (missing)    | XXXX       | important for simple toy examples
GetEnumerator          | IObservable          | (not needed) | X          | should be `iterator()` or `getIterator()` in Java. Java's `getIterator()` was moved to `BlockingObservable` because it blocks [https://github.com/Netflix/RxJava/commit/ce3ee1bca053962c8dab8 d8568e9c1bb3728f01b#L8L2170], but what does "block" mean: block until 1 is available, or until completed? anyway this is considered old/bad, just use subscribe instead
GroupBy                | IObservable          | Observable   | ok         | Scala's group by, but overloaded version with integrated map exists. Returns a IObservable<IGroupedObservable<TKey, TElement>> / Observable<GroupedObservable<K,R>>, not the same as ToDictionary!
GroupByUntil           | IObservable          | (missing)    | X          | similar to GroupBy except that there’s a duration selector function to control the lifetime of each generated group
GroupJoin              | IObservable          | (not needed) | ok         | used for LINQ statement join … in … on … equals … into …
IgnoreElements         | IObservable          | (missing)    | XX         | Ignores all values in an observable sequence leaving only the termination messages.
Interval               | IObservable          | (missing)    | XXXX       | Simplification of Generate, important for simple toy examples
Join                   | IObservable          | (not needed) | ok         | used for LINQ statement join … in … on … equals …
just                   | (called Return)      | Observable   | ok         | called Return in C#
Last                   | IObservable          | (not needed) | D          | retunrs TSource (i.e. blocks). In Java, we have BlockingObservable.Last which blocks and returns TSource directly
LastOrDefault          | IObservable          | (not needed) | D          | ditto
Latest                 | IObservable          | (missing)    | XX         | returns an IEnumerable that returns the last sampled element upon each iteration and subsequently blocks until the next element in the observable source sequence becomes available.
LongCount              | IObservable          | (missing)    | X          | returns 64bit Int
mapMany                | (not needed)         | Observable   | ok         | called SelectMany in C# and typically used with LINQ syntax
map                    | (nod needed)         | Observable   | ok         | called Select in C# and typically used with LINQ syntax
Materialize            | IObservable          | Observable   | ok         | turns all onNext/onError/onComplete calls into onNext calls with a Notification object
MaxBy                  | IObservable          | (missing)    | XX         | 
Max                    | IObservable          | (missing)    | XX         | 
Merge                  | IObs./Obs./IEnum.    | Observable   | ok         | flatten
mergeDelayError        | (missing)            | Observable   | ok         | 
MinBy                  | IObservable          | (missing)    | XX         | 
Min                    | IObservable          | (missing)    | XX         | 
MostRecent             | IObservable          | (missing)    | XX         | returns an IEnumerable that returns the last sampled element upon each iteration.
Multicast              | IObservable          | Observable   | ok         | 
Never                  | Observable           | Observable   | ok         | 
Next                   | IObservable          | (missing)    | ?          | what's the difference to ToEnumerable?
ObserveOn              | IObservable          | Observable   | ok         | 
OfType                 | IObservable          | (missing)    | X          | instanceof filter
OnErrorResumeNext      | IObs./IEnum.         | Observable   | ok         | 
onErrorReturn          | (just sugar)         | Observable   | ok         | the same as catch with a singleton observable as argument
onExceptionResumeNext  | (not needed)         | Observable   | ok         | like onErrorResumeNext, but only respects Exceptions, and not general Throwables
Publish                | IObservable          | Observable   | ok         | 
PublishLast            | IObservable          | (missing)    | X          | 
Range                  | Observable           | Observable   | ok         | the entire range will be immediately emitted each time an Observer subscribes => "boring"
reduce                 | (not needed)         | Observable   | ok         | the same as aggregate
RefCount               | IConnectableObservabl| (missing)    | ok         | should be a method of Java's ConnectableObservable
Repeat                 | IObservable          | (missing)    | X          | 
Replay                 | IObservable          | Observable   | ok         | 
Retry                  | IObservable          | (missing)    | XXX        | repeats underlying observable until it succeeds (or failed a given number of times)
Return                 | Observable           | (called just)| ok         | called just in Java
Sample                 | IObservable          | Observable   | ok         | 
Scan                   | IObservable          | Observable   | ok         | 
Select                 | IObservable          | (= map)      | ok         | 
SelectMany             | IObservable          | (= mapMany)  | ok         | 
SequenceEqual          | IObservable          | Observable   | ok         | 
Single                 | IObservable          | (not needed) | D          | asserts that there is one single element in observable (optionally satisfying a predicate)
SingleOrDefault        | IObservable          | (not needed) | D          | 
Skip                   | IObservable          | Observable   | ok         | Scala's drop
SkipLast               | IObservable          | (missing)    | XX         | Scala's dropRight
SkipUntil              | IObservable          | (missing)    | XX         | opposite of TakeUntil
SkipWhile              | IObservable          | (missing)    | XX         | Scala's dropWhile, opposite of TakeWhile
Start                  | Observable           | (missing)    | XXXX       | could use Observable.from(Future<T>), but requires constructing a Future
StartWith              | IObservable          | Observable   | ok         | 
Subscribe              | IEnumerable          | Observable   | ok         | 
SubscribeOn            | IObservable          | Observable   | ok         | 
Sum                    | IObservable          | (missing)    | XX         | 
Switch/switchDo        | IObservable          | Observable   | ok         | 
Synchronize            | IObservable          | Observable   | ok         | TODO: what's the difference to merge?
Take                   | IObservable          | Observable   | ok         | Scala's take
TakeLast               | IObservable          | Observable   | ok         | Scala's takeRight
TakeUntil              | IObservable          | Observable   | ok         | has NOTHING to do with Scala's and Rx's takeWhile!
TakeWhile              | IObservable          | Observable   | ok         | Scala's takeWhile
takeWhileWithIndex     | (missing)            | Observable   | ok         | 
Then                   | IObservable          | (not needed) | ok         | used for LINQ statement orderby …, … / returns a System.Reactive.Joins.Plan<TResult>, we do not need this in Java
Throttle               | IObservable          | (missing)    | XXXX       | important for Dictionary autocompletion example
Throw                  | Observable           | (error)      | ok         | lowercase throw is a reserved keyword ;-)
TimeInterval           | IObservable          | (missing)    | XXX        | 
Timeout                | IObservable          | (missing)    | XXX        | 
Timer                  | Observable           | (missing)    | XXX        | 
Timestamp              | IObservable          | Observable   | ok         | 
ToArray                | IObservable          | (missing)    | X          | toList is better
ToAsync                | Action/Func          | (not needed) | ok         | these calls return a System.Func<ArgT1, ..., ArgTN, IObservable<Unit>> or a System.Func<ArgT1, ..., ArgTN, IObservable<ResT>>, which are popular in the C# world, but not needed in Java
toBlockingObservable   | (not needed)         | Observable   | ok         | in Java, before you can call a blocking method on an observable, you have to call toBlockingObservable
ToDictionary           | IObservable          | (missing)    | XX         | returns a IObservable<IDictionary<TKey, TElement>>, not the same as GroupBy!
ToEnumerable           | IObservable          | (not needed) | X          | returns a System.Collections.Generic.IEnumerable<TSource>, each value is available as soon as produced. This is considered old/bad, just use subscribe instead.
ToEvent                | IObservable          | (missing)    | ok         | Marked ok, but events compatibility is required in some way! Returns a System.Reactive.IEventSource<TSource>.
ToEventPattern         | IObservable          | (missing)    | ok         | ditto
ToList                 | IObservable          | Observable   | ok         | returns IObservable<IList<TSource>>/ Observable<java.util.List<T>>
ToLookup               | IObservable          | (not needed) | ok         | C# specific
toSortedList           | (sugar)              | Observable   | ok         | 
ToObservable           | IEnumerable          | Observable   | ok         | 
Using                  | Observable           | (not needed) | ok         | using + IDisposable is C#-specific
When                   | IEnum.<Plan>/Obs.    | (not needed) | ok         | LINQ stuff (takes a System.Reactive.Joins.Plan<TResult>), not needed in Java
Where                  | IObservable          | Observable   | ok         | in Java: implemented using filter
Window                 | IObservable          | (missing)    | XX         | 
Zip                    | IObservable          | Observable   | ok         | 


