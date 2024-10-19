package steplogs.spring.rmi.http.subscriber;

import java.util.Map;

public interface HttpHeaderTransporter {
	
	Map<String, String> getHttpHeaders();

	Map<String, String> removeHttpHeaders();
	
	void addHttpHeader(String headerKey, String headerValue);
	
}
