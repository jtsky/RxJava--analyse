#Rxjava lift()分析
###要分析的源码
```java
Observable.OnSubscribe<Integer> onSubscribe1 = new Observable.OnSubscribe<Integer>() {

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
        };

        Observable observable1 = Observable.create(onSubscribe1);
        Func1<Integer, String> transform1 = new Func1<Integer, String>() {

            @Override
            public String call(Integer integer) {
                return "==" + integer;
            }
        };

        Func1<String, String> transform2 = new Func1<String, String>() {

            @Override
            public String call(String s) {
                return s + "==";
            }
        };

        Subscriber<String> subscriber1 = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.v("TAG", s);
            }
        };

        observable1.map(transform1).map(transform2).subscribe(subscriber1);
```
<br />
observable1.map(transform1)的时候会返回一个新的Obserable2，同时Obserable2中包含了observable1中的Onsubscribe1和Obserable2
新new的Onsubscribe2.
<br />
而observable2.map(transform2)的时候会返回一个新的Obserable3，同时Obserable3中又包含了observable2中的Onsubscribe2和Obserable3
新new的Onsubscribe3.
<br />
以上两步只是对象的创建，还没开始进行链式调用。事件的发送是从Obserable3.subscribe(subscriber1)开始。
<br />
###subscribe()源码
```java
public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }
<br />
private static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
        //这里可以做一些emit之前的初始化操作 具体可以重写onStart()
        subscriber.onStart();

        // 对subscriber进行封装 具体是一些错误的处理
        if (!(subscriber instanceof SafeSubscriber)) {
            subscriber = new SafeSubscriber<T>(subscriber);
        }
            //重点方法
            hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber);
            return hook.onSubscribeReturn(subscriber);

        }
    }

public <T> OnSubscribe<T> onSubscribeStart(Observable<? extends T> observableInstance, final OnSubscribe<T> onSubscribe) {
        return onSubscribe;
    }

```
<br />
hook.onSubscribeStart(observable, observable.onSubscribe).call(subscriber)其实就是onSubscribe3.call(subscriber1).
###下面看lift源码
```java
public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return lift(new OperatorMap<T, R>(func));
    }

//最主要的方法
public final <R> Observable<R> lift(final Operator<? extends R, ? super T> operator) {
        return new Observable<R>(new OnSubscribe<R>() {
            @Override
            public void call(Subscriber<? super R> o) {
                try {
                    //这段代码下面会仔细分析
                    Subscriber<? super T> st = hook.onLift(operator).call(o);
                    try {
                        st.onStart();
                        onSubscribe.call(st);
                    } catch (Throwable e) {
                        if (e instanceof OnErrorNotImplementedException) {
                            throw (OnErrorNotImplementedException) e;
                        }
                        st.onError(e);
                    }
                } catch (Throwable e) {
                    if (e instanceof OnErrorNotImplementedException) {
                        throw (OnErrorNotImplementedException) e;
                    }
                    o.onError(e);
                }
            }
        });
    }

    public <T, R> Operator<? extends R, ? super T> onLift(final Operator<? extends R, ? super T> lift) {
            return lift;
        }
```
hook.onLift(operator).call(o)就是对subscribe的代理，在旧的Subscriber中加入新的逻辑输出新的Subscriber。
在OperatorMap中的体现就是调用call之前先进行transformer转换，即Func的逻辑
operator是OperatorMap的一个对象
###源码如下
```java
public final class OperatorMap<T, R> implements Operator<R, T> {

    private final Func1<? super T, ? extends R> transformer;

    public OperatorMap(Func1<? super T, ? extends R> transformer) {
        this.transformer = transformer;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super R> o) {
        return new Subscriber<T>(o) {

            @Override
            public void onCompleted() {
                o.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                o.onError(e);
            }

            @Override
            public void onNext(T t) {
                try {
                    o.onNext(transformer.call(t));
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, t));
                }
            }

        };
    }

}

```
onSubscribe.call(st)等于onSubscribe2.call(newSubscribr)。然后进入循环调用，直到onSubscribe1.call()
```java
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
```
随后进入
```java
            @Override
            public void onNext(T t) {
                try {
                    o.onNext(transformer.call(t));
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, t));
                }
            }
```
因为newSubscribr包含oldSubscribr的引用，所以形成循环的内毒链式调用，同时插入transformer的逻辑。
<br />
好了，关于lift()的分析就到这里，写得有点乱，大家凑合着看，有时间的话，会继续分析其他的模块。