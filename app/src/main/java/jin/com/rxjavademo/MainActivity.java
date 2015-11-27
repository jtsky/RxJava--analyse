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

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(2);
            }
        }).map(integer -> {
            int a = 0;
            a = a + 1;
            return "====>" + integer;
        }).subscribe(s -> Log.i(TAG, s));

        /*Observable.just(0, 1, 2, 3, 4, 5).groupBy(new Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer integer) {
                //System.out.println("groupBy====>" + (integer % 2 == 0));
                return integer % 2 == 0;
            }
        }).count().subscribe(integer -> Log.v(TAG, "NEXTsssss====>" + integer), e -> Log.v(TAG, "ERROR====>" + e.getMessage())
                , () -> Log.v(TAG, "COMPLETE====>"));*/


    }

}
