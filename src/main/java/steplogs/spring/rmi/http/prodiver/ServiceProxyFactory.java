package steplogs.spring.rmi.http.prodiver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class ServiceProxyFactory implements ApplicationListener<ContextRefreshedEvent> {

	private final Set<Class<?>> serviceClasses;

	public ServiceProxyFactory(Set<Class<?>> serviceClasses) {
		this.serviceClasses = serviceClasses;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.initialize(event.getApplicationContext());
	}

	protected class InvokeTarget {
		private Object bean;
		private Method method;

		public InvokeTarget(Object b, Method m) {
			bean = b;
			method = m;
		}

		public Object getBean() {
			return bean;
		}

		public void setBean(Object bean) {
			this.bean = bean;
		}

		public Method getMethod() {
			return method;
		}

		public void setMethod(Method method) {
			this.method = method;
		}
	}

	protected Map<String, InvokeTarget> beans = new HashMap<>();

	private void initialize(ApplicationContext applicationContext) {
		if (beans.isEmpty()) {
			synchronized (ServiceProxyFactory.class) {
				if (beans.isEmpty()) {
					Map<String, InvokeTarget> prepBeans = new HashMap<>(beans);

					for (Class<?> clazz : serviceClasses) {
						Map<String, ?> map = applicationContext.getBeansOfType(clazz);
						for (Map.Entry<String, ?> entry : map.entrySet()) {
							String serviceName = entry.getKey();
							serviceName = PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE.translate(serviceName);
							serviceName = serviceName.replace("_service_impl", "");
							serviceName = serviceName.replace("_impl", "");

							Method[] methods = clazz.getDeclaredMethods();
							for (Method m : methods) {
								if (!Modifier.isStatic(m.getModifiers()) && !Modifier.isFinal(m.getModifiers())
										&& Modifier.isPublic(m.getModifiers())) {
									String methodName = m.getName();
									methodName = PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE
											.translate(methodName);

									String path = "/" + serviceName + "/" + methodName;

									prepBeans.put(path, new InvokeTarget(entry.getValue(), m));
								}
							}
						}
					}

					beans = prepBeans;
				}
			}
		}
	}

	public InvokeTarget getServiceInvokeTarget(String url) {
		return beans.get(url);
	}

}
