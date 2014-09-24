package be.icode.hot.test.nio.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Content-type", "application/json");
		String json = "[{\"name\":\"damien\",\"age\":\"3\"}]";
		resp.setHeader("Content-length", json.length()+"");
		resp.getWriter().print(json);
	}
}
