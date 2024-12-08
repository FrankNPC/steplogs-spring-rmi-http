package io.steplogs.spring.rmi.http.subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultThreadBindHttpHeaderTransporter implements HttpHeaderTransporter {
	
	protected ThreadLocal<Map<String, List<String>>> httpHeaderThreadLocal = new ThreadLocal<>();
	
	@Override
	public Map<String, List<String>> getHttpHeaders() {
		return httpHeaderThreadLocal.get();
	}

	@Override
	public Map<String, List<String>> removeHttpHeaders() {
		Map<String, List<String>> headers = httpHeaderThreadLocal.get();
		httpHeaderThreadLocal.remove();
		return headers;
	}

	@Override
	public void addHttpHeaders(String headerKey, List<String> headerValues) {
		Map<String, List<String>> httpHeaders = httpHeaderThreadLocal.get();
		if (httpHeaders==null) {
			httpHeaderThreadLocal.set(httpHeaders = new HashMap<>());
		}
		List<String> exsitingHeaderValues = httpHeaders.get(headerKey);
		if (exsitingHeaderValues==null) {
			httpHeaders.put(headerKey, exsitingHeaderValues = new ArrayList<>());
		}
		exsitingHeaderValues.addAll(headerValues);
	}

	@Override
	public void addHttpHeader(String headerKey, String headerValue) {
		Map<String, List<String>> httpHeaders = httpHeaderThreadLocal.get();
		if (httpHeaders==null) {
			httpHeaderThreadLocal.set(httpHeaders = new HashMap<>());
		}
		List<String> exsitingHeaderValues = httpHeaders.get(headerKey);
		if (exsitingHeaderValues==null) {
			httpHeaders.put(headerKey, exsitingHeaderValues = new ArrayList<>());
		}
		exsitingHeaderValues.add(headerValue);
	}
	
}
