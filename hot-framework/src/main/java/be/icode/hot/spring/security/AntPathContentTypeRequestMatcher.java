package be.icode.hot.spring.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.google.common.net.HttpHeaders;

public class AntPathContentTypeRequestMatcher implements RequestMatcher  {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AntPathContentTypeRequestMatcher.class);

	private final AntPathRequestMatcher antPathRequestMatcher;
	private final String acceptHeader;
	
	public AntPathContentTypeRequestMatcher(String acceptHeader, AntPathRequestMatcher antPathRequestMatcher) {
		this.antPathRequestMatcher = antPathRequestMatcher;
		this.acceptHeader = acceptHeader;
	}

	@Override
	public boolean matches(HttpServletRequest request) {
		String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
		
		if (acceptHeader != null && acceptHeader.contains(this.acceptHeader)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Content-type [%s] match expected %s", acceptHeader,this.acceptHeader));
			}
			return antPathRequestMatcher.matches(request);
		} 
		return false;
	}
}
