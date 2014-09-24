package be.icode.hot.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyOutputStream;

import reactor.core.Environment;
import reactor.spring.core.task.RingBufferAsyncTaskExecutor;
import be.icode.hot.utils.FileLoader.Buffer;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

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
