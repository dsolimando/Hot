package be.solidx.hot.cli;

import java.io.IOException;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.RoleInfo;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.URIUtil;

/**
 * Constraint Security handler that redirect secure traffic with 307 redirect code (allowing POST methods redirections)
 * 
 * @author dsolimando
 *
 */
public class HotConstrainSecurityHandler extends ConstraintSecurityHandler {

	@Override
	protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response,
			RoleInfo roleInfo) throws IOException {
		
		if (request.isSecure())
            return true;

		
        HttpConfiguration httpConfig = HttpChannel.getCurrentHttpChannel().getHttpConfiguration();
        
        String scheme = httpConfig.getSecureScheme();
        int port = httpConfig.getSecurePort();
        
        String url = URIUtil.newURI(scheme, request.getServerName(), port,request.getRequestURI(),request.getQueryString());
        
        response.setContentLength(0);
        response.sendRedirect(307, url);
        request.setHandled(true);
        return false;
	}
}
