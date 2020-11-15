package be.solidx.hot.rest;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import be.solidx.hot.spring.security.OAuth2ClientAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ClientAuthServlet extends HttpServlet {

	private static final long serialVersionUID = 3247689618377073739L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req,resp);
    }

    @Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String provider = req.getParameter("provider");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            Boolean isAccessTokenValid = (Boolean) req.getSession(true).getAttribute(OAuth2ClientAuthenticationFilter.IS_ACCESS_TOKEN_VALID);
            if (isAccessTokenValid == null && (provider == null || provider.isEmpty())) {
                resp.setStatus(HttpStatus.BAD_REQUEST.value());
                resp.getWriter().write("Missing property 'provider'");
            } else if (!isAccessTokenValid) {
                resp.setStatus(HttpStatus.UNAUTHORIZED.value());
                resp.getWriter().write("Client access token is not valid");
            } else {
                resp.setStatus(HttpStatus.BAD_REQUEST.value());
            }
        }  else {
			resp.setStatus(HttpStatus.OK.value());
			resp.getWriter().write(provider +" authentication succeed.");
		}
	}
}
