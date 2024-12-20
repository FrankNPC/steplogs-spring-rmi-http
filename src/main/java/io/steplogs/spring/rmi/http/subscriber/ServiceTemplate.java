package io.steplogs.spring.rmi.http.subscriber;

import org.aopalliance.aop.Advice;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

public interface ServiceTemplate<T> {
	
	default String getBaseUrl() {return null;}
	
	default RestClient getRestClient() {return null;}
	
	default HttpHeaderTransporter getHttpHeaderTransporter() {return null;}
	
	default ErrorHandler getErrorHandler() {return null;}

	default T getDefaultErrorResponse() {return null;}
	
	default Advice[] getAdvices() {return null;}
	
}
