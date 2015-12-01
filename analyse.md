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