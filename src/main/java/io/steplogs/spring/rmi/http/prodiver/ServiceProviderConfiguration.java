package io.steplogs.spring.rmi.http.prodiver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;

import io.steplogs.spring.rmi.http.BeanHelper;

@Import({ServiceProviderController.class, ServiceProviderInvoker.class})
public class ServiceProviderConfiguration implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.initialize(event.getApplicationContext());
	}

	private Map<String, InvokeTarget> beans = new HashMap<>();
	
	InvokeTarget getServiceInvokeTarget(String url) {
		return beans.get(url);
	}
	
	private void initialize(ApplicationContext applicationContext) {
		if (beans.isEmpty()) {
			synchronized (this) {
				if (beans.isEmpty()) {
					Set<String> annotationNames = new HashSet<>(Arrays.asList("Provider"));
					Map<String, InvokeTarget> prepBeans = new HashMap<>(beans);
					
					Map<String, ?> map = applicationContext.getBeansOfType(Object.class);
					for (Map.Entry<String, ?> entry : map.entrySet()) {
						String serviceName = BeanHelper.parseServiceName(entry.getKey());
						Method[] methods = ClassUtils.getUserClass(entry.getValue()).getDeclaredMethods();
						boolean isProviderPresent = BeanHelper.detectAnnotationByName(
								ClassUtils.getUserClass(entry.getValue()).getAnnotations(), annotationNames);
						for (Method method : methods) {
							if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers())
									&& Modifier.isPublic(method.getModifiers())
									&& (isProviderPresent || BeanHelper.detectAnnotationByName(method.getAnnotations(), annotationNames))
									) {
								String path = BeanHelper.parseMethodName(serviceName, method.getName());
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
