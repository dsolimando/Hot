package be.icode.hot.nio.http;

import java.util.concurrent.ExecutorService;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class HttpClient<CLOSURE, MAP> {

	ExecutorService 				eventLoop;
	NioClientSocketChannelFactory 	channelFactory;
	SSLContextBuilder				sslContextBuilder;
	ObjectMapper					objectMapper;
	HttpDataSerializer				httpDataSerializer;
	
	@Autowired
	public HttpClient(
			ExecutorService eventLoop,
			NioClientSocketChannelFactory channelFactory, 
			SSLContextBuilder sslContextBuilder, 
			ObjectMapper objectMapper, 
			HttpDataSerializer httpDataSerializer) {
		
		this.eventLoop = eventLoop;
		this.channelFactory = channelFactory;
		this.sslContextBuilder = sslContextBuilder;
		this.objectMapper = objectMapper;
		this.httpDataSerializer = httpDataSerializer;
	}

	public abstract Request<CLOSURE, MAP> buildRequest(MAP options);
}
