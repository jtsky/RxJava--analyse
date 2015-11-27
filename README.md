2015-11-27
本项目的分析参考了 扔物线之前写了一篇文章 [《给 Android 开发者的 RxJava 详解》](http://gank.io/post/560e15be2dca930e00da1083#toc_1)<br />

废话不多说 直接开始

RxJava的基本用法：<br />
```java
Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
        }).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return "===>" + integer;
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, s);
            }
        });
```
<br />
最后将会输出 ===>2

其实上面这段代码中涉及到2个Observable和2个OnSubscribe（具体Observable OnSubscribe Subscribe之间的关系可以去看扔物线的文章）

具体可以看源码
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
Observable.create()的时候是不发送事件的，只有当Observable.subscribe()以后事件才开始emit。

下面直接看subscribe()源码(这里是精简的源码)
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
onSubscribeStart()方法只是简单的返回了map操作符中创建的OnSubscribe对象，简称为OnSubscribe2，即lift中的
```java
new OnSubscribe<R>() {
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
        }
```
而OnSubscribe2.call(subscriber)中的subscriber为
```java
new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, s);
            }
        }

```
接下来进入到lift中继续分析
Subscriber<? super T> st = hook.onLift(operator).call(o) 中的operator 是OperatorMap
由lift(new OperatorMap<T, R>(func))传入
func很简单就是
```java
new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return "===>" + integer;
            }
        }
```
下面具体看下OperatorMap
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
transformer 即为func
所以
```java
Subscriber<? super T> st = hook.onLift(operator).call(o);
st.onStart();
onSubscribe.call(st);
```
可以简写成
```java
Subscriber newSubscriber = operator.call(subscriber);
newSubscriber.onStart();
onSubscribe.call(newSubscriber);
```
operator.call(subscriber)其实就是对
```java
new Action1<String>() {
            @Override
            public void call(String s) {
                Log.i(TAG, s);
            }
        }
```
进行了包装 返回了一个新的newSubscriber
```java
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
```
所以
onSubscribe.call(newSubscriber)即为subscriber.onNext(2);
而
```java
public void onNext(T t) {
                try {
                    o.onNext(transformer.call(t));
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, t));
                }
            }
```
就可以写成
```java

public void onNext(T t) {
                try {
                    o.onNext(new Func1<Integer, String>() {
                                         @Override
                                         public String call(Integer integer) {
                                             return "===>" + integer;
                                         }
                                     }.call(t));
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    onError(OnErrorThrowable.addValueAsLastCause(e, t));
                }
            }
```
o即为
 ```java
 new Action1<String>() {
             @Override
             public void call(String s) {
                 Log.i(TAG, s);
             }
         }
 ```
所以最终的结果为===>2
