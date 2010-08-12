package org.msh.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Impede que uma página fique no cache do browser
 * @author Ricardo
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

		// indica que a página não deve ser armazenada em cache
		if (path.endsWith(".seam")) 
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
