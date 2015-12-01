package jin.com.rxjavademo;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void testRx() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
        }).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                return "==========>" + integer;
            }
        }).map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                return s + "aaaa";
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        });

    }

    @Test
    public void testCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}