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
