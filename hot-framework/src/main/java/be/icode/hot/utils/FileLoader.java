package be.icode.hot.utils;

import java.io.ByteArrayOutputStream;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileLoader {
	
	public static final int BUFFER_SIZE = 2048;

	private static final Logger LOGGER = LoggerFactory.getLogger(FileLoader.class);

	ConcurrentHashMap<Path, byte[]> filecache = new ConcurrentHashMap<>();
	
	ExecutorService eventLoop;
	
	public FileLoader(ExecutorService eventLoop) {
		this.eventLoop = eventLoop;
	}
	
	public Promise<Void, Exception, Buffer> loadResourceAsync(final Path path) throws IOException {
		return loadResourceAsync(path, true);
	}

	public Promise<Void, Exception, Buffer> loadResourceAsync(final Path path, final boolean touchCache) throws IOException {
		final Deferred<Void, Exception, Buffer> deferred = new DeferredObject<>();
		
		if (touchCache && filecache.keySet().contains(path)) {
			final byte[] file = filecache.get(path);
			eventLoop.execute(new Runnable() {
				@Override
				public void run() {
					deferred.notify(new Buffer(file, file.length));
					deferred.resolve(null);
				}
			});
			return deferred.promise();
		}
		
		final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		Set<OpenOption> options = new HashSet<>();
		options.add(StandardOpenOption.READ);
		
		final AsynchronousFileChannel asyncChannel = AsynchronousFileChannel.open(path, options , eventLoop, new FileAttribute[]{});
		
		CompletionHandler<Integer, Void> completionHandler = new CompletionHandler<Integer, Void>() {

			int pos = 0;
			
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			
			@Override
			public void completed(Integer result, Void attachment) {
				if (result == -1) {
					deferred.resolve(null);
					try {
						byteArrayOutputStream.flush();
						filecache.put(path, byteArrayOutputStream.toByteArray());
						asyncChannel.close();
					} catch (IOException e) {
						LOGGER.error("",e);
					}
				} else {
					deferred.notify(new Buffer(byteBuffer.array(), result));
					if (touchCache)
						byteArrayOutputStream.write(byteBuffer.array(),0,result);
					byteBuffer.clear();
					pos += result;
					asyncChannel.read(byteBuffer, pos, null, this);
				}
			}

			@Override
			public void failed(Throwable exc, Void attachment) {
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
			this.content = content;
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
