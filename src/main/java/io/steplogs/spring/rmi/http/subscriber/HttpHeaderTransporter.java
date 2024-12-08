package io.steplogs.spring.rmi.http.subscriber;

import java.util.List;
import java.util.Map;

public interface HttpHeaderTransporter {
	
	Map<String, List<String>> getHttpHeaders();

	Map<String, List<String>> removeHttpHeaders();
	
	void addHttpHeaders(String headerKey, List<String> headerValues);
	
	void addHttpHeader(String headerKey, String headerValue);
}
