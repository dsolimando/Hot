/*
package be.solidx.hot.test.async;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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
