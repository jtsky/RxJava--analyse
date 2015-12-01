package jin.com.rxjavademo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*Observable.create(new Observable.OnSubscribe<Integer>() {
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
        });*/

        /*Observable.just(0, 1, 2, 3, 4, 5).groupBy(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                //System.out.println("groupBy====>" + (integer % 2 == 0));
                return integer % 2 == 0;
            }
        }).count().subscribe(integer -> Log.v(TAG, "NEXTsssss====>" + integer), e -> Log.v(TAG, "ERROR====>" + e.getMessage())
                , () -> Log.v(TAG, "COMPLETE====>"));*/


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
    }

}
