package be.solidx.hot.web.deprecated;

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
//package be.icode.hot.web.deprecated;
//
//import java.io.IOException;
//import java.util.concurrent.ConcurrentHashMap;
//
//import javax.annotation.PostConstruct;
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.jasper.EmbeddedServletOptions;
//import org.apache.jasper.compiler.JspRuntimeContext;
//import org.apache.jasper.servlet.JspServletWrapper;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.ServletConfigAware;
//import org.springframework.web.context.ServletContextAware;
//
//
//@Controller
//public class JspController extends HotControllerImpl implements ServletConfigAware, ServletContextAware {
//	
//	private static final Log LOG = LogFactory.getLog(JspController.class);
//	
//	private ServletConfig servletConfig;
//	
//	private ServletContext servletContext;
//
//	private EmbeddedServletOptions options;
//
//	private JspRuntimeContext context;
//
//	private ConcurrentHashMap<String, JspServletWrapper> jspWrapperMap = new ConcurrentHashMap<String, JspServletWrapper>();
//	
//	@PostConstruct
//	public void init () {
//		try {
//			options = new EmbeddedServletOptions(servletConfig, servletContext);
//			context = new JspRuntimeContext(servletContext, options);
//		} catch (Exception e) {
//			LOG.error("Failed to load JSP Engine. You are probably on deploying on Google app engine",e);
//		}
//	}
//
//	@RequestMapping ("/**/*.jsp")
//	public void compileJsp (HttpServletRequest servletRequest, HttpServletResponse response) throws IOException, ServletException {
//		String pathInfo = servletRequest.getPathInfo();
//		if (jspWrapperMap.get(pathInfo) == null) {
//			synchronized (this) {
//				if (jspWrapperMap.get(pathInfo) == null) {
//					try {
//						jspWrapperMap.put(pathInfo, new JspServletWrapper(servletConfig, options, servletRequest.getPathInfo(), false, context));
//					} catch (Exception e) {
//						LOG.error("",e);
//						jspWrapperMap.remove(pathInfo);
//						printErrorPage(e, response.getWriter());
//						response.getWriter().flush();
//					} 
//				}
//			}
//		}
//		try {
//			jspWrapperMap.get(pathInfo).service(servletRequest, response, !devMode);
//		} catch (Exception e) {
//			LOG.error("",e);
//			jspWrapperMap.remove(pathInfo);
//			printErrorPage(e, response.getWriter());
//		}
//		response.getWriter().flush();
//	}
//
//	@Override
//	public void setServletConfig(ServletConfig servletConfig) {
//		this.servletConfig = servletConfig;
//	}
//
//	@Override
//	public void setServletContext(ServletContext servletContext) {
//		this.servletContext = servletContext;
//	}
//}
