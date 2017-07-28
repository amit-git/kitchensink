package kitchen.sink.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrialAndError {
    private static Logger log = LoggerFactory.getLogger(TrialAndError.class);

    public static void main(String[] args) throws Exception {

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                log.info("First Thread {}", Thread.currentThread().getName());
                e.onNext("Los Gatos");
                e.onComplete();
            }
        })
        //Observable.just("Los Gatos")
                .map(s -> s.length())
                .map(l -> l * 2)
                //.observeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .map(l -> {
                    log.info("Downstream thread  {}", Thread.currentThread().getName());
                    return l * 2;
                })
                .subscribe(n -> log.info("Result :: {}  ({})", n, Thread.currentThread().getName()));

        Thread.sleep(2000);

    }
}
