package be.solidx.hot.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

public class ClientAuthServlet extends HttpServlet {

	private static final long serialVersionUID = 3247689618377073739L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String provider = req.getParameter("provider");
		if (provider == null || provider.isEmpty()) {
			resp.setStatus(HttpStatus.BAD_REQUEST.value());
			resp.getWriter().write("Missing property 'provider'");
		} else {
			resp.setStatus(HttpStatus.OK.value());
			resp.getWriter().write(provider +" authentication succeed.");
		}
	}
}
