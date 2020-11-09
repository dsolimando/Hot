package be.solidx.hot.test.nio;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;

import reactor.core.Environment;
import reactor.spring.core.task.RingBufferAsyncTaskExecutor;
import be.solidx.hot.utils.FileLoader;
import be.solidx.hot.utils.FileLoader.Buffer;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

public class FileServerMain {

	public static void main(String[] args) throws Exception {
		Thread.sleep(5000);
		final ExecutorService tp = Executors.newFixedThreadPool(1);
		final RingBufferAsyncTaskExecutor el = new RingBufferAsyncTaskExecutor(new Environment())
			.setName("ringBufferExecutor")
			.setProducerType(ProducerType.SINGLE)
			.setBacklog(2048)
			.setWaitStrategy(new YieldingWaitStrategy());
		el.afterPropertiesSet();
		final AtomicInteger atomicInteger = new AtomicInteger(1000);

		final FileLoader fileServer = new FileLoader(el);

		final long t0 = System.currentTimeMillis();
		
		for (int i = 0; i < 1000; i++) {
			
			final int j = i;
			tp.execute(new Runnable() {

				@Override
				public void run() {
					try {
						final FileOutputStream fout = new FileOutputStream(new File("/tmp/s5" + j + ".tiff"));
						final ByteArrayOutputStream bos = new ByteArrayOutputStream();
						// final FileInputStream fin = new FileInputStream(new
						// File ("/Users/dsolimando/Desktop/s5.tiff"));
						// toOutputStreamBuffered(fin, fout);
						// // fin.close();
						// fout.flush();
						// fout.close();
						// // System.out.println(Thread.currentThread() + " - "+
						// (System.currentTimeMillis()-t0));
						// if (atomicInteger.decrementAndGet() == 0) {
						// System.out.println(System.currentTimeMillis()-t0);
						// }

						Path path = Paths.get(new URI("file:/Users/dsolimando/Desktop/s5.tiff"));
						Promise<Void, Exception, Buffer> p = fileServer.loadResourceAsync(path);
						p.done(new DoneCallback<Void>() {
							@Override
							public void onDone(Void voidd) {
								try {
									// fin.close();
									fout.flush();
									fout.close();
									if (atomicInteger.decrementAndGet() == 0) {
										System.out.println(System.currentTimeMillis() - t0);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).progress(new ProgressCallback<Buffer>() {

							@Override
							public void onProgress(Buffer progress) {
								try {
									fout.write(progress.getContent(), 0, progress.getLength());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}).fail(new FailCallback<Exception>() {

							@Override
							public void onFail(Exception result) {
								result.printStackTrace();
							}
						});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
}
