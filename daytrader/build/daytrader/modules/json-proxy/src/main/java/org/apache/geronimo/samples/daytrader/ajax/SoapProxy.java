/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.samples.daytrader.ajax;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.geronimo.samples.daytrader.AccountProfileDataBean;
import org.apache.geronimo.samples.daytrader.TradeWSServicesProxy;

public class SoapProxy extends HttpServlet implements Servlet {
	private TradeWSServicesProxy _soapProxy;

	public SoapProxy() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			_soapProxy = new TradeWSServicesProxy();
			//_soapProxy.setEndpoint("http://localhost:8080/daytrader/services/TradeWSServices");
			_soapProxy.setEndpoint(config.getInitParameter("endpoint"));
		} catch (IllegalArgumentException x) {
			throw new ServletException(x);
		}
	}

	protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		try {
			doProxy(arg0, arg1);
		} catch (Exception x) {
			throw new ServletException(x);
		}
	}

	protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		try {
			doProxy(arg0, arg1);
		} catch (Exception x) {
			throw new ServletException(x);
		}
	}

	private void doProxy(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException,
			TransformerException, TransformerConfigurationException, SecurityException, NoSuchMethodException,
			InstantiationException, IllegalArgumentException, IntrospectionException, IllegalAccessException,
			InvocationTargetException {

		String jsonBody = null;
		String pathInfo = req.getPathInfo();
		String basePath = req.getContextPath() + req.getServletPath();
		if (pathInfo == null) {
			rsp.setContentType("text/html");
			rsp.getWriter().println("<html><body><h3>Available operations:</h3><ul>");
			rsp.getWriter().println("<li>" + basePath + "/buy?p1=<i>java.lang.String</i>&p2=<i>java.lang.String</i>&p3=<i>double</i>&p4=<i>int</i>");
			rsp.getWriter().println("<li>" + basePath + "/getAccountData?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/getAccountProfileData?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/getAllQuotes");
			rsp.getWriter().println("<li>" + basePath + "/getClosedOrders?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/getHolding?p1=<i>java.lang.Integer</i>");
			rsp.getWriter().println("<li>" + basePath + "/getHoldings?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/getMarketSummary");
			rsp.getWriter().println("<li>" + basePath + "/getOrders?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/getQuote?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/login?p1=<i>java.lang.String</i>&p2=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/logout?p1=<i>java.lang.String</i>");
			rsp.getWriter().println("<li>" + basePath + "/register?p1=<i>java.lang.String</i>&p2=<i>java.lang.String</i>&p3=<i>java.lang.String</i>&p4=<i>java.lang.String</i>&p5=<i>java.lang.String</i>&p6=<i>java.lang.String</i>&p7=<i>java.math.BigDecimal</i>");
			rsp.getWriter().println("<li>" + basePath + "/sell?p1=<i>java.lang.String</i>&p2=<i>java.lang.Integer</i>&p3=<i>int</i>");
			rsp.getWriter().println("<li>" + basePath + "/updateAccountProfile?p1=<i>org.apache.geronimo.samples.daytrader.AccountProfileDataBean</i>");
			rsp.getWriter().println("</ul></body></html>");
		} else {
			// Path info includes leading '/'; remove it to get the operation name.
			String op = pathInfo.substring(1);

			if (op.equals("buy")) {
				String p1 = req.getParameter("p1");
				String p2 = req.getParameter("p2");
				double p3 = Double.parseDouble(req.getParameter("p3"));
				int p4 = Integer.parseInt(req.getParameter("p4"));

				Object result = _soapProxy.buy(p1, p2, p3, p4);
				jsonBody = convertResultToJSON(result, op);

			}
			// else if (op.equals("cancelOrder")) {}
			// else if (op.equals("completeOrder")) {}
			// else if (op.equals("createQuote")) {}
			else if (op.equals("getAccountData")) {
				String p1 = req.getParameter("p1");

				Object result = _soapProxy.getAccountData(p1);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("getAccountProfileData")) {
				String p1 = req.getParameter("p1");

				Object result = _soapProxy.getAccountProfileData(p1);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("getAllQuotes")) {
				Object[] results = _soapProxy.getAllQuotes();
				jsonBody = convertResultsToJSON(results, op);

			} else if (op.equals("getClosedOrders")) {
				String p1 = req.getParameter("p1");

				Object[] results = _soapProxy.getClosedOrders(p1);
				jsonBody = convertResultsToJSON(results, op);

			} else if (op.equals("getHolding")) {
				Integer p1 = new Integer(req.getParameter("p1"));

				Object result = _soapProxy.getHolding(p1);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("getHoldings")) {
				String p1 = req.getParameter("p1");

				Object[] results = _soapProxy.getHoldings(p1);
				jsonBody = convertResultsToJSON(results, op);

			} else if (op.equals("getMarketSummary")) {
				Object result = _soapProxy.getMarketSummary();
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("getOrders")) {
				String p1 = req.getParameter("p1");

				Object[] results = _soapProxy.getOrders(p1);
				jsonBody = convertResultsToJSON(results, op);

			} else if (op.equals("getQuote")) {
				String p1 = req.getParameter("p1");

				Object result = _soapProxy.getQuote(p1);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("login")) {
				String p1 = req.getParameter("p1");
				String p2 = req.getParameter("p2");

				Object result = _soapProxy.login(p1, p2);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("logout")) {
				String p1 = req.getParameter("p1");

				_soapProxy.logout(p1);

			}
			// else if (op.equals("orderCompleted")) {}
			// else if (op.equals("queueOrder")) {}
			else if (op.equals("register")) {
				String p1 = req.getParameter("p1");
				String p2 = req.getParameter("p2");
				String p3 = req.getParameter("p3");
				String p4 = req.getParameter("p4");
				String p5 = req.getParameter("p5");
				String p6 = req.getParameter("p6");
				BigDecimal p7 = new BigDecimal(req.getParameter("p7"));

				Object result = _soapProxy.register(p1, p2, p3, p4, p5, p6, p7);
				jsonBody = convertResultToJSON(result, op);

			}
			// else if (op.equals("resetTrade")) {}
			else if (op.equals("sell")) {
				String p1 = req.getParameter("p1");
				Integer p2 = new Integer(req.getParameter("p2"));
				int p3 = Integer.parseInt(req.getParameter("p3"));

				Object result = _soapProxy.sell(p1, p2, p3);
				jsonBody = convertResultToJSON(result, op);

			} else if (op.equals("updateAccountProfile")) {
				Object p1 = req.getParameter("p1");

				Object result = _soapProxy.updateAccountProfile((AccountProfileDataBean) p1);
				jsonBody = convertResultToJSON(result, op);
			}
			// else if (op.equals("updateQuotePriceVolume")) {}
			else {
				throw new ServletException("Operation not supported - " + op);
			}

			rsp.setContentType("text/json");
			rsp.getWriter().println(jsonBody);
		}

	}

	String convertResultToJSON(Object result, String op) throws IllegalArgumentException, IntrospectionException,
			IllegalAccessException, InvocationTargetException {
		StringBuffer buf = new StringBuffer("");
		buf.append("{\"" + op + "Return\":{");
		buf.append(beanToJSON(result));
		buf.append("}}");
		return buf.toString();
	}

	String convertResultsToJSON(Object[] results, String op) throws IllegalArgumentException, IntrospectionException,
			IllegalAccessException, InvocationTargetException {
		StringBuffer buf = new StringBuffer("");
		buf.append("{\"" + op + "Return\":{");
		if (results != null) {
			Object beanElement = results[0];
			String className = beanElement.getClass().getName();
			int offset = className.lastIndexOf('.');
			String tagName;

			if (offset < 0) {
				tagName = className;
			} else {
				tagName = className.substring(offset + 1);
			}

			buf.append("\"" + tagName + "\":[{");
			for (int j = 0; j < results.length; j++) {
				beanElement = results[j];
				buf.append(beanToJSON(beanElement) + "}");
				if (j < results.length - 1) {
					buf.append(",{");
				}
			}
			buf.append("]");
		}
		buf.append("}}");
		return buf.toString();
	}

	String beanToJSON(Object bean) throws IntrospectionException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		StringBuffer buf = new StringBuffer("");

		BeanInfo bi = Introspector.getBeanInfo(bean.getClass(), Object.class);
		PropertyDescriptor[] pd = bi.getPropertyDescriptors();

		for (int i = 0; i < pd.length; i++) {

			Method getter = pd[i].getReadMethod();
			if (Modifier.isPublic(getter.getModifiers()) && !Modifier.isStatic(getter.getModifiers())) {
				Object value = pd[i].getReadMethod().invoke(bean, null);
				// TODO: if the return value is null - this blows up!!!
				// CJB: This needs to be added back to the the SOAP Proxy code

				if (value == null) {
					buf.append("\"" + pd[i].getName() + "\":");
					buf.append("\"null\"");
				} else if (!value.getClass().isArray()) {
					// Note: java.util.Date on the original JavaBean appears to
					// be translated into java.util.Calendar after Java2WSDL
					// then WSDL2Java.
					// Calendar.toString() is for debugging purposes.
					if (value instanceof Calendar) {
						buf.append("\"" + pd[i].getName() + "\":");
						buf.append("\"" + ((Calendar) value).getTime() + "\"");
					} else {
						buf.append("\"" + pd[i].getName() + "\":");
						buf.append("\"" + value + "\"");
					}
				} else { // its an Array object
					buf.append("\"" + pd[i].getName() + "\":{");
					Object beanElement = ((Object[]) value)[0];
					String className = beanElement.getClass().getName();
					int offset = className.lastIndexOf('.');
					String tagName;

					if (offset < 0) {
						tagName = className;
					} else {
						tagName = className.substring(offset + 1);
					}

					buf.append("\"" + tagName + "\":[{");

					for (int j = 0; j < ((Object[]) value).length; j++) {
						beanElement = ((Object[]) value)[j];
						buf.append(beanToJSON(beanElement) + "}");
						if (j < ((Object[]) value).length - 1) {
							buf.append(",{");
						}
					}
					buf.append("]}");
				}

				if (i < pd.length - 1) {
					buf.append(",");
				}
			}
		}

		return buf.toString();
	}
}
