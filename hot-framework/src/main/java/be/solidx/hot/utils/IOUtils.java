package be.solidx.hot.utils;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyOutputStream;

import be.solidx.hot.utils.FileLoader.Buffer;

public class IOUtils {
	public static final int BUFFER_SIZE = 2048;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);
	
	public static void toOutputStreamBuffered (InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte [BUFFER_SIZE];
		
		int read;
		while ((read = inputStream.read(buffer))!= -1) {
			outputStream.write(buffer,0,read);
		}
		inputStream.close();
	}
	
	public static Promise<byte[], Exception, Void> asyncRead (final HttpServletRequest req, final ExecutorService executorService, final ExecutorService promiseResolver) {
		
		final DeferredObject<byte[], Exception, Void> deferredObject = new DeferredObject<>();
		
		try {
			final ServletInputStream servletInputStream = req.getInputStream();
			
			servletInputStream.setReadListener(new ReadListener() {
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				@Override
				public void onError(final Throwable t) {
					promiseResolver.execute(new Runnable() {
						@Override
						public void run() {
							deferredObject.reject(new Exception(t));
						}
					});
				}
				
				@Override
				public void onDataAvailable() throws IOException {
					executorService.execute(new Runnable() {
						@Override
						public void run() {
							byte b[] = new byte[2048];
							int len = 0;
							
							try {
								while (servletInputStream.isReady() && (len = servletInputStream.read(b)) != -1) {
								    baos.write(b, 0, len);
								}
							} catch (IOException e) {
								LOGGER.error("",e);
							}
						}
					});
				}
				
				@Override
				public void onAllDataRead() throws IOException {
					promiseResolver.execute(new Runnable() {
						@Override
						public void run() {
							deferredObject.resolve(baos.toByteArray());
						}
					});
				}
			});
		} catch (final IOException e2) {
			promiseResolver.execute(new Runnable() {
				@Override
				public void run() {
					deferredObject.reject(new Exception(e2));
				}
			});
			
		} catch (final IllegalStateException exception) {
			promiseResolver.execute(new Runnable() {
				@Override
				public void run() {
					deferredObject.resolve("".getBytes());
				}
			});
		}
		
//		executorService.execute(new Runnable() {
//			@Override
//			public void run() {
//				final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//				try {
//					IOUtils.toOutputStreamBuffered(req.getInputStream(), outputStream);
//					promiseResolver.execute(new Runnable() {
//						@Override
//						public void run() {
//							deferredObject.resolve(outputStream.toByteArray());
//						}
//					});
//				} catch (final IOException e) {
//					promiseResolver.execute(new Runnable() {
//						@Override
//						public void run() {
//							deferredObject.reject(e);
//						}
//					});
//				}
//			}
//		});
		
		return deferredObject.promise();
	}
	
	public static void toGZippedOutputStreamBuffered (InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte [BUFFER_SIZE];
		
		int read;
		SnappyOutputStream snappyOutputStream = new SnappyOutputStream(outputStream);
		while ((read = inputStream.read(buffer))!= -1) {
			snappyOutputStream.write(buffer,0,read);
		}
		inputStream.close();
		snappyOutputStream.close();
		outputStream.close();
	}
	
	public static InputStream loadResourceNoCache (String path) throws IOException {
		URL res = IOUtils.class.getClassLoader().getResource(path);
		
		if (res == null) {
			throw new IOException("Resource "+path+" does not exists");
		}
		URLConnection resConn = res.openConnection();
		// !!! needed to avoid jvm resource catching
		resConn.setUseCaches(false);
		return resConn.getInputStream();
	}
	
	public static byte[] loadBytesNoCache (String path) throws IOException {
		return org.apache.commons.io.IOUtils.toByteArray(loadResourceNoCache(path));
	}
	
	public static InputStream loadResourceNoCache (URL path) throws IOException {
		URLConnection resConn = path.openConnection();
		// !!! needed to avoid jvm resource catching
		resConn.setUseCaches(false);
		return resConn.getInputStream();
	}
	
	public static byte[] loadBytesNoCache (URL path) throws IOException {
		return org.apache.commons.io.IOUtils.toByteArray(loadResourceNoCache(path));
	}
}
