package steplogs.spring.rmi.http.prodiver;

import java.util.Set;

import org.springframework.context.annotation.Import;

@Import(ServiceProxyController.class)
public class ServiceProxyConfiguration {
	
	public static ServiceProxyFactory getServiceProxyFactory(Set<Class<?>> serviceClasses) {
		return new ServiceProxyFactory(serviceClasses);
	}
	
	public static ServiceProxyInvoker getServiceProxyInvoker(ErrorHandler errorHandler) {
		return new ServiceProxyInvoker(errorHandler);
	}

}
