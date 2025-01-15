package io.steplogs.spring.rmi.http.prodiver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ClassUtils;

import io.steplogs.spring.rmi.http.BeanHelper;

@Import({ServiceProviderController.class})
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
					Map<String, InvokeTarget> prepBeans = new HashMap<>(beans);
					
					Map<String, ?> map = applicationContext.getBeansOfType(Object.class);
					for (Map.Entry<String, ?> entry : map.entrySet()) {
						String serviceName = BeanHelper.parseServiceName(entry.getKey());
						
						Provider classProvider = (Provider) Stream.of(ClassUtils.getUserClass(entry.getValue()).getAnnotations()).findFirst().orElse(null);
						
						Class<?>[] interfaces = ClassUtils.getUserClass(entry.getValue().getClass()).getInterfaces();
						for (Class<?> iface : interfaces) {
							Method[] methods = iface.getDeclaredMethods();
							for (Method method : methods) {
								if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers())
										&& Modifier.isPublic(method.getModifiers())
										) {
									
									Provider methodProvider = (Provider) Stream.of(method.getAnnotations()).findFirst().orElse(null);
									if (methodProvider!=null) {
										String path = BeanHelper.parseMethodName(methodProvider, serviceName, method.getName());
										prepBeans.put(path, new InvokeTarget(entry.getValue(), method));
									}else if (classProvider!=null) {
										String path = BeanHelper.parseMethodName(classProvider, serviceName, method.getName());
										prepBeans.put(path, new InvokeTarget(entry.getValue(), method));
									}
								}
							}
						}
					}
					beans = prepBeans;
				}
			}
		}
	}

}
