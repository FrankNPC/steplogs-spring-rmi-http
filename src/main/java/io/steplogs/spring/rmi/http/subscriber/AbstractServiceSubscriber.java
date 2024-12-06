package io.steplogs.spring.rmi.http.subscriber;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.ProxyFactoryBean;

public abstract class AbstractServiceSubscriber {
	/**
	 * Create subscriber to the method from ServiceTemplate.
	 * 
	 * @param <T>					The return type
	 * @param clazz					The class of return type
	 * @param host					Base host url
	 * @param httpHeaderTransporter	Copy, add or transport headers such as Authorization
	 * @param errorHandler			Error handler
	 * @param errorResponse			Default response if there is any error
	 * @param advices				Advices for the proxy
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getProxyFactoryBean(Class<T> clazz, ServiceTemplate<?> serviceTemplate) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		factory.addInterface(clazz);
		if (serviceTemplate.getAdvices()!=null) {
			for(Advice advice : serviceTemplate.getAdvices()) {
				factory.addAdvice(advice);
			}
		}
		factory.addAdvice(new ServiceSubscriberMethodInterceptor<>(serviceTemplate));
		return (T) factory.getObject();
	}
}
