package steplogs.spring.rmi.http.subscriber;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

public abstract class AbstractServiceProxySubscriber {

	/**
	 * Create proxy to the method.
	 * 
	 * @param <T>					The return type
	 * @param clazz					The class of return type
	 * @param restClient			RestClient instance
	 * @param httpHeaderTransporter	Copy, add or transport headers such as Authorization
	 * @param errorHandler			Error handler
	 * @param errorResponse			Default response if there is any error
	 * @param advices				Advices for the proxy
	 */
	protected <T> Object getProxyFactoryBean(Class<?> clazz, RestClient restClient, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse, Advice... advices) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		factory.addInterface(clazz);
		if (advices!=null) {
			for(Advice advice : advices) {
				factory.addAdvice(advice);
			}
		}
		factory.addAdvice(new ServiceProxyMethodInterceptor<>(restClient, httpHeaderTransporter, errorHandler, errorResponse));
		return factory.getObject();
	}

	/**
	 * Create proxy to the method.
	 * 
	 * @param <T>					The return type
	 * @param clazz					The class of return type
	 * @param host					Base host url
	 * @param httpHeaderTransporter	Copy, add or transport headers such as Authorization
	 * @param errorHandler			Error handler
	 * @param errorResponse			Default response if there is any error
	 * @param advices				Advices for the proxy
	 */
	protected <T> Object getProxyFactoryBean(Class<?> clazz, String host, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse, Advice... advices) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		factory.addInterface(clazz);
		if (advices!=null) {
			for(Advice advice : advices) {
				factory.addAdvice(advice);
			}
		}
		factory.addAdvice(new ServiceProxyMethodInterceptor<>(host, httpHeaderTransporter, errorHandler, errorResponse));
		return factory.getObject();
	}

}
