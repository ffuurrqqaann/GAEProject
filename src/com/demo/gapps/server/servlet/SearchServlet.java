package com.demo.gapps.server.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.gapps.shared.Constant;


@SuppressWarnings("serial")
public class SearchServlet extends HttpServlet {

	private final static Logger LOGGER = Logger.getLogger(SearchServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
		LOGGER.setLevel(Constant.LOG_LEVEL);
		LOGGER.info("Initializing Search Servlet");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("fileSearch.jsp");
	}
	
}