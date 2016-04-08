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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileLoader {
	
	public static final int BUFFER_SIZE = 2048;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileLoader.class);

//	ConcurrentHashMap<Path, byte[]> filecache = new ConcurrentHashMap<>();
	
	ExecutorService eventLoop;
	
	public FileLoader(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
	}
	
//	public Promise<Void, Exception, Buffer> loadResourceAsync(final Path path) throws IOException {
//		return loadResourceAsync(path, true);
//	}

	public Promise<Void, Exception, Buffer> loadResourceAsync(final Path path) throws IOException {
		final Deferred<Void, Exception, Buffer> deferred = new DeferredObject<>();
		
		final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		Set<OpenOption> options = new HashSet<>();
		options.add(StandardOpenOption.READ);
		
		final AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, options , eventLoop, new FileAttribute[]{});
		
		CompletionHandler<Integer, Void> completionHandler = new CompletionHandler<Integer, Void>() {

			int pos = 0;
			
			@Override
			public void completed(Integer result, Void attachment) {
				if (result == -1) {
					deferred.resolve(null);
					try {
						asyncChannel.close();
					} catch (IOException e) {
						LOGGER.error("",e);
					}
				} else {
					deferred.notify(new Buffer(byteBuffer.array(), result));
					byteBuffer.clear();
					pos += result;
					asyncChannel.read(byteBuffer, pos, null, this);
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
				LOGGER.error("",exc);
				deferred.reject(new Exception(exc));
			}
		};
		
		asyncChannel.read(byteBuffer, 0, null, completionHandler);
		return deferred.promise();
	}
	
	public static class Buffer {
		byte[] content;
		int length;
		
		public Buffer(byte[] content, int length) {
			this.content = new byte[length];
			System.arraycopy(content, 0, this.content, 0, length);
			this.length = length;
		}
		
		public byte[] getContent() {
			return content;
		}
		
		public int getLength() {
			return length;
		}
	}
}
