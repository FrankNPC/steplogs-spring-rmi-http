package steplogs.spring.rmi.http.prodiver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Import({ServiceProviderController.class, ServiceProviderInvoker.class})
public class ServiceProviderConfiguration implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.initialize(event.getApplicationContext());
	}

	Map<String, InvokeTarget> beans = new HashMap<>();
	
	InvokeTarget getServiceInvokeTarget(String url) {
		return beans.get(url);
	}
	
	private void initialize(ApplicationContext applicationContext) {
		if (beans.isEmpty()) {
			synchronized (this) {
				if (beans.isEmpty()) {
					Map<String, InvokeTarget> prepBeans = new HashMap<>(beans);
					
					Map<String, ?> map = applicationContext.getBeansOfType(Object.class);
					for (Map.Entry<String, ?> entry : map.entrySet()) {
						String serviceName = entry.getKey();
						serviceName = PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE.translate(serviceName);
						serviceName = serviceName.replace("_service_impl", "");
						serviceName = serviceName.replace("_impl", "");
						boolean isProviderPresent = ClassUtils.getUserClass(entry.getValue()).isAnnotationPresent(Provider.class);
						Method[] methods = ClassUtils.getUserClass(entry.getValue()).getDeclaredMethods();
						for (Method method : methods) {
							if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers())
									&& Modifier.isPublic(method.getModifiers())
									&& (isProviderPresent || method.isAnnotationPresent(Provider.class))
									) {
								String methodName = method.getName();
								methodName = PropertyNamingStrategies.SnakeCaseStrategy.INSTANCE
										.translate(methodName);

								String path = "/" + serviceName + "/" + methodName;

								prepBeans.put(path, new InvokeTarget(entry.getValue(), method));
							}
						}
					}

					beans = prepBeans;
				}
			}
		}
	}

}
