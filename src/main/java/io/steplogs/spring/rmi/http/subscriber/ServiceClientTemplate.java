package io.steplogs.spring.rmi.http.subscriber;

import org.springframework.aop.Advisor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

public interface ServiceClientTemplate<T> {
	
	default String getBaseUrl() {return null;}
	
	default RestClient getRestClient() {return null;}

//	default HttpHeaderTransporter getResponseHttpHeaderTransporter() {return null;}
	
	default ErrorHandler getErrorHandler() {return null;}

	default T getDefaultErrorResponse() {return null;}
	
	default Advisor[] getAdvisors() {return null;}
	
}
