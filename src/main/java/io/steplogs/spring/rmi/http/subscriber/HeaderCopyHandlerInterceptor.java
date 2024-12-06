package io.steplogs.spring.rmi.http.subscriber;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderCopyHandlerInterceptor implements HandlerInterceptor {

	private final List<String> headersForCopy;

	private final HttpHeaderTransporter defaultHttpHeaderTransporter;
	
	public HeaderCopyHandlerInterceptor(HttpHeaderTransporter defaultHttpHeaderTransporter, List<String> headers) {
		this.headersForCopy = headers;
		this.defaultHttpHeaderTransporter = defaultHttpHeaderTransporter;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		defaultHttpHeaderTransporter.removeHttpHeaders();
		for(String header : headersForCopy) {
			String value = request.getHeader(header);
			if (value!=null) {
				defaultHttpHeaderTransporter.addHttpHeader(header, value);
			}
		}
		return true;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		defaultHttpHeaderTransporter.removeHttpHeaders();
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
		defaultHttpHeaderTransporter.removeHttpHeaders();
	}
	
}
