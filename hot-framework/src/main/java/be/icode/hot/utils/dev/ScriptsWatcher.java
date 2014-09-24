package be.icode.hot.utils.dev;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import be.icode.hot.spring.config.event.ReloadShowEvent;
import be.icode.hot.spring.config.event.ReloadShowEvent.ReloadReason;

import com.sun.nio.file.SensitivityWatchEventModifier;

public class ScriptsWatcher implements ApplicationEventPublisherAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptsWatcher.class);

	final WatchService watchService;
	
	final Map<WatchKey,Path> dirs = new HashMap<>();
	
	String filepath;
	
	ApplicationEventPublisher applicationEventPublisher;
	
	public ScriptsWatcher(String filepath) throws IOException, URISyntaxException {
		watchService = FileSystems.getDefault().newWatchService();
		registerShowsFiles(Paths.get(new URI(filepath)));
	}
	
	public ScriptsWatcher(URI uri) throws IOException, URISyntaxException {
		watchService = FileSystems.getDefault().newWatchService();
		registerShowsFiles(Paths.get(uri));
	}
	
	private void registerShowsFiles (final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            	LOGGER.info("Watching "+dir);
            	WatchKey key = dir.register(watchService, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
            	dirs.put(key, path);
                return FileVisitResult.CONTINUE;
            }
        });
	}
	
	public void watch () {
		while (true) {
			WatchKey key;
            try {
//            	long t1 = System.currentTimeMillis();
                key = watchService.take();
//                System.out.println(System.currentTimeMillis()-t1);
            } catch (InterruptedException x) {
                return;
            }
            Path dir = dirs.get(key);
            for (WatchEvent<?> event : key.pollEvents()) {
            	Kind<?> kind = event.kind();
            	
            	if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
            	
            	Path file = (Path) event.context();
            	Path child = dir.resolve(file);
            	
//            	System.out.println(file);
            	
            	if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            		if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
            			if (LOGGER.isDebugEnabled()) LOGGER.debug("Directory created :"+child);
            			WatchKey newkey;
						try {
							newkey = child.register(watchService, new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
							dirs.put(newkey, child);
							if (LOGGER.isDebugEnabled()) LOGGER.debug("Start watching :"+child);
						} catch (IOException e) {
							e.printStackTrace();
						}
            		} else {
            			if (LOGGER.isDebugEnabled()) LOGGER.debug("File created:"+child);
            			try {
							applicationEventPublisher.publishEvent(new ReloadShowEvent(this, new URL("file:"+child.toString()), ReloadReason.ADDED));
						} catch (MalformedURLException e) {
							LOGGER.error("Invalid File path",e);
						}
            		}
            	} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            		if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
            			if (LOGGER.isDebugEnabled()) LOGGER.debug("Directory deleted :"+child);
            		} else {
            			if (LOGGER.isDebugEnabled()) LOGGER.debug("File deleted :"+child);
            			try {
							applicationEventPublisher.publishEvent(new ReloadShowEvent(this, new URL("file:"+child.toString()), ReloadReason.DELETED));
						} catch (MalformedURLException e) {
							LOGGER.error("Invalid File path",e);
						}
            		}
            	} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            		if (!Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
            			if (LOGGER.isDebugEnabled()) LOGGER.debug("File modified :"+child);
            			try {
							applicationEventPublisher.publishEvent(new ReloadShowEvent(this, new URL("file:"+child.toString()), ReloadReason.MODIFIED));
						} catch (MalformedURLException e) {
							LOGGER.error("Invalid File path",e);
						}
            		} 
            	}
			}
            boolean valid = key.reset();
            if (!valid) {
            	if (LOGGER.isDebugEnabled()) LOGGER.debug("Stop watching " + dir);
                dirs.remove(key);
            }
		}
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
