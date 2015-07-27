package be.solidx.hot.test.nio.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XmlServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Content-type", "application/xml");
		String xml = "<data attr=\"value\"/>";
		resp.setHeader("Content-length", xml.length()+"");
		resp.getWriter().print(xml);
	}
}