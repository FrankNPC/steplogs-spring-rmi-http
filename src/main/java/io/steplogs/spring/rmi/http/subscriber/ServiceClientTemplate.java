package io.steplogs.spring.rmi.http.subscriber;

import org.aopalliance.aop.Advice;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

import io.steplogs.spring.rmi.http.HttpHeaderTransporter;

public interface ServiceClientTemplate<T> {
	
	default String getBaseUrl() {return null;}
	
	default RestClient getRestClient() {return null;}

	default HttpHeaderTransporter getRequestHttpHeaderTransporter() {return null;}

	default HttpHeaderTransporter getResponseHttpHeaderTransporter() {return null;}
	
	default ErrorHandler getErrorHandler() {return null;}

	default T getDefaultErrorResponse() {return null;}
	
	default Advice[] getAdvices() {return null;}
	
}
