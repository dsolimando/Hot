package be.solidx.hot.rest;

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
