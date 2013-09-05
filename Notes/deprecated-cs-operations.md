
Deprecated Operations in C# Rx
------------------------------

Deprecated/Obsolete Operations are not marked in MSDN documentation. That's why it's useful to have a list of all deprecated operations.

There are two groups of deprecated operations:

### USE_ASYNC

Message: "This blocking operation is no longer supported. Instead, use the async version in combination with C# and Visual Basic async/await support. In case you need a blocking operation, use Wait or convert the resulting observable sequence to a Task object and block. See http://go.microsoft.com/fwlink/?LinkID=260866 for more information."

Affected methods:

    public static TSource First<TSource>(this IObservable<TSource> source)
    public static TSource First<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)
    public static TSource FirstOrDefault<TSource>(this IObservable<TSource> source)
    public static TSource FirstOrDefault<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)
    public static void ForEach<TSource>(this IObservable<TSource> source, Action<TSource> onNext)
    public static void ForEach<TSource>(this IObservable<TSource> source, Action<TSource, int> onNext)
    public static TSource Last<TSource>(this IObservable<TSource> source)
    public static TSource Last<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)
    public static TSource LastOrDefault<TSource>(this IObservable<TSource> source)
    public static TSource LastOrDefault<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)
    public static TSource Single<TSource>(this IObservable<TSource> source)
    public static TSource Single<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)
    public static TSource SingleOrDefault<TSource>(this IObservable<TSource> source)
    public static TSource SingleOrDefault<TSource>(this IObservable<TSource> source, Func<TSource, bool> predicate)


### USE_TASK_FROMASYNCPATTERN 

Message: "This conversion is no longer supported. Replace use of the Begin/End asynchronous method pair with a new Task-based async method, and convert the result using ToObservable. If no Task-based async method is available, use Task.Factory.FromAsync to obtain a Task object. See http://go.microsoft.com/fwlink/?LinkID=260866 for more information."

Affected methods: All `FromAsyncPattern` methods

