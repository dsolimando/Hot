package be.solidx.hot.cli;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class TestJetty {

	public static void main(String[] args) throws Exception {
		Server server = new Server();
		
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(9900);
//		connector.setAcceptQueueSize(8);
		QueuedThreadPool tp = (QueuedThreadPool) server.getThreadPool();
		tp.setMaxThreads(Runtime.getRuntime().availableProcessors());
		
		System.out.println(server.getThreadPool().getClass());
		server.setConnectors(new Connector[]{connector});
		System.out.println(Runtime.getRuntime().availableProcessors());
		server.start();
		server.join();
//		server.start(null);
	}
}
