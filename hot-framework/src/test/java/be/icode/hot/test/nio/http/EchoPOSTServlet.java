package be.icode.hot.test.nio.http;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.IOUtils;

public class EchoPOSTServlet extends HttpServlet {

	protected void doPost(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws ServletException ,IOException {
		resp.setContentType(req.getContentType());
		resp.setContentLength(req.getContentLength());
		String body = IOUtils.toString(req.getInputStream());
		System.out.println(body);
		resp.getWriter().write(body);
	};
}
