/*
package be.solidx.hot.test.async;

import com.ea.async.Async;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

*/
/**
 * Created by dsolimando on 13/04/2018.
 *//*

public class TestAsync {

    static {
        Async.init();
    }

    private static class AsyncTask {

        CompletableFuture<String> run(Function<Void,String> r) {

            CompletableFuture<String> cf = new CompletableFuture<>();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);

                    cf.complete(r.apply(null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            return cf;
        }
    }

    private static class AsyncTest {

        CompletableFuture test(CompletableFuture cf) {
            System.out.println("1");
            String s =  Async.await(cf);
            System.out.println(s);
            System.out.println("2");
            return CompletableFuture.completedFuture(s);
        }
    }

    @Test
    public void testAsync() throws ExecutionException, InterruptedException {

        ExecutorService es = Executors.newFixedThreadPool(1);


        es.execute(() -> {
            CompletableFuture<String> cf = new AsyncTest().test(new AsyncTask().run(a -> "Hello"));
        });
        es.execute(() -> {
            CompletableFuture<String> cf = new AsyncTest().test(new AsyncTask().run(a -> "Hello"));
        });

        Thread.sleep(10000);
        //Assert.assertEquals("Hello",cf.get());
    }
}
*/
