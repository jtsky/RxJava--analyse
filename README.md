2015-11-27
本项目的分析参考了 扔物线之前写了一篇文章 [《给 Android 开发者的 RxJava 详解》](http://gank.io/post/560e15be2dca930e00da1083#toc_1)<br />

废话不多说 直接开始

RxJava的基本用法：<br />
Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
        }).map(integer -> {
            return "====>" + integer;
        }).subscribe(s -> Log.i(TAG, s));
<br />
最后将会输出 ====>2