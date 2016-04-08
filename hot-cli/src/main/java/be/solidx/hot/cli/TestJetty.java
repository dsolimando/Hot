package be.solidx.hot.cli;

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
