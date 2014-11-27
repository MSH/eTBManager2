package org.msh.utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Prevent that a page keeps stored in the browser cache
 * @author Ricardo Memoria
 *
 */
public class NoCacheFilter implements Filter {

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		
		HttpServletRequest r = (HttpServletRequest)request; 
		String path = r.getServletPath();

		// Inform the browser that page should not be stored in cache
		if ((path.endsWith(".seam")) || (path.contains("nocache")))
		{
			((HttpServletResponse)response).setHeader("Cache-Control","no-store");
			((HttpServletResponse)response).setHeader("Pragma","no-cache");
			((HttpServletResponse)response).setDateHeader("Expires",0);
		}
		
		filterChain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
