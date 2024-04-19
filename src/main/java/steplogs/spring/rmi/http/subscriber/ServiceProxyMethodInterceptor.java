package steplogs.spring.rmi.http.subscriber;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cglib.proxy.Callback;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

import steplogs.spring.rmi.http.subscriber.PayloadConvertor.FormKeys;

public class ServiceProxyMethodInterceptor<T> extends AbstractInvokerClient<T> implements MethodInterceptor, Callback {
	
	private PayloadConvertor payloadConvertor = new PayloadConvertor();

	public ServiceProxyMethodInterceptor(RestClient restClient, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse) {
		super(restClient, httpHeaderTransporter, errorHandler, errorResponse);
	}
	
	public ServiceProxyMethodInterceptor(String host, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse) {
		super(host, httpHeaderTransporter, errorHandler, errorResponse);
	}
	
	@Override
	public T invoke(MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		FormKeys form = payloadConvertor.parseForm(method);

		Map<String, Object> queryVariables = new HashMap<>();
		Map<String, Object> formData = new HashMap<>();

		Object[] objects = invocation.getArguments();
		for(int i=0; i<form.primativeKeys.length; i++) {
			if (form.primativeKeys[i]!=null) {
				queryVariables.put(form.primativeKeys[i], objects[i]);
			}else if (form.formKeys[i]!=null){
				formData.put(form.formKeys[i], objects[i]);
			}
		}
		
		if (!formData.isEmpty()) {
			return super.post(form.path, queryVariables, formData, ParameterizedTypeReference.forType(method.getGenericReturnType()));
		}else {
			return super.get(form.path, queryVariables, ParameterizedTypeReference.forType(method.getGenericReturnType()));
		}
	}
	

}
