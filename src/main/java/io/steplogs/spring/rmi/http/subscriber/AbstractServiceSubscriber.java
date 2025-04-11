package io.steplogs.spring.rmi.http.subscriber;

import org.springframework.aop.Advisor;
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
	 * @param advisors				Advisor for the proxy
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getProxyFactoryBean(Class<T> clazz, ServiceClientTemplate<?> serviceClientTemplate) {
		ProxyFactoryBean factory = new ProxyFactoryBean();
		factory.addInterface(clazz);
		if (serviceClientTemplate.getAdvisors()!=null) {
			for(Advisor advisor : serviceClientTemplate.getAdvisors()) {
				factory.addAdvisor(advisor);
			}
		}
		factory.addAdvice(new ServiceSubscriberMethodInterceptor<>(serviceClientTemplate));
		return (T) factory.getObject();
	}
}
