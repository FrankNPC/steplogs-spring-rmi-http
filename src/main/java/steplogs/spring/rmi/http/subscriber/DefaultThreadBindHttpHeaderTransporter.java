package steplogs.spring.rmi.http.subscriber;

import java.util.HashMap;
import java.util.Map;

public class DefaultThreadBindHttpHeaderTransporter implements HttpHeaderTransporter {
	
	protected ThreadLocal<Map<String, String>> httpHeaderThreadLocal = new ThreadLocal<>();
	
	@Override
	public Map<String, String> getHttpHeaders() {
		return httpHeaderThreadLocal.get();
	}

	@Override
	public Map<String, String> removeHttpHeaders() {
		Map<String, String> headers = httpHeaderThreadLocal.get();
		httpHeaderThreadLocal.remove();
		return headers;
	}

	@Override
	public void addHttpHeader(String headerKey, String headerValue) {
		Map<String, String> httpHeaders = httpHeaderThreadLocal.get();
		if (httpHeaders==null) {
			httpHeaderThreadLocal.set(httpHeaders = new HashMap<>());
		}
		if (headerValue!=null) {
			httpHeaders.put(headerKey, headerValue);
		}
	}
	
}
