package steplogs.spring.rmi.http.prodiver;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import steplogs.spring.rmi.http.prodiver.ServiceProxyFactory.InvokeTarget;

public class ServiceProxyInvoker {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String DEFAULT_ERROR_RESPONSE = "{\"code\": -1, \"message\": \"service doesn't exist: {}\", \"success\": false}";

	private static final ErrorHandler DEFAULT_ERROR_HANDLER = new ErrorHandler() {
		public String handle(String errorMessage) {
			return  DEFAULT_ERROR_RESPONSE.replace("{}", errorMessage);
		}
		public String handle(Throwable thrownException) {
			thrownException.printStackTrace();
			return  DEFAULT_ERROR_RESPONSE;
		}
	};
	
	@Resource
	protected ServiceProxyFactory serviceProxyFactory;
	
	private ErrorHandler errorHandler;
	
	public ServiceProxyInvoker(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler!=null?errorHandler:DEFAULT_ERROR_HANDLER;
	}
	
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	public Object get(HttpServletRequest request, HttpServletResponse response) throws Exception {
		URI url = URI.create(request.getRequestURL().toString());
		String path = url.getPath();
		if (path==null || !path.matches("^/[a-zA-Z0-9_\\-]+/[a-zA-Z0-9_\\-]+.*$")) {
			return errorHandler.handle(path);
		}
		InvokeTarget target = serviceProxyFactory.getServiceInvokeTarget(path);
		if (target==null) {
			return errorHandler.handle(path);
		}
		
		Map<String, String[]> requestMapper = request.getParameterMap();
		
		Parameter[] params = target.getMethod().getParameters();
		Object[] requestParams = new Object[params.length];
		for(int i=0; i<requestParams.length; i++) {
			String[] values = requestMapper.get(params[i].getName());
			if (values!=null) {
				Class<?> clazz = params[i].getType();
				if (values.length==1) {
					requestParams[i] = convert(values[0], clazz);
				}else {
					Object[] objects = new Object[values.length];
					for(int k=0; k<values.length; k++) {
						objects[k] = convert(values[k], clazz);
					}
					requestParams[i] = objects;
				}
			}
		}
		
		return target.getMethod().invoke(target.getBean(), requestParams);
	}
	
	public Object post(HttpServletRequest request, HttpServletResponse response,
								Map<String, Object> formBody) throws Exception {
		URI url = URI.create(request.getRequestURL().toString());
		String path = url.getPath();
		if (path==null || !path.matches("^/[a-zA-Z0-9_\\-]+/[a-zA-Z0-9_\\-]+.*$")) {
				return errorHandler.handle(path);
		}
		InvokeTarget target = serviceProxyFactory.getServiceInvokeTarget(path);
		if (target==null) {
			return errorHandler.handle(path);
		}

		Map<String, String[]> requestMapper = request.getParameterMap();

		Parameter[] params = target.getMethod().getParameters();
		Object[] requestParams = new Object[params.length];
		for(int i=0; i<requestParams.length; i++) {
			String[] values = requestMapper.get(params[i].getName());
			Class<?> clazz = params[i].getType();
			if (values!=null) {
				if (values.length==1) {
					requestParams[i] = convert(values[0], clazz);
				}else {
					Object[] objects = new Object[values.length];
					for(int k=0; k<values.length; k++) {
						objects[k] = convert(values[k], clazz);
					}
					requestParams[i] = objects;
				}
			}else {
				Object obj = formBody.get(params[i].getName());
				requestParams[i] = convert(obj, clazz);
			}
		}
		
		return target.getMethod().invoke(target.getBean(), requestParams);
	}
	
	private Object convert(Object value, Class<?> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (value==null) {
			return null;
		}
		if (String.class.equals(clazz)) {
			try {
				return URLDecoder.decode(String.valueOf(value), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else if (!clazz.isPrimitive()) {
			return objectMapper.convertValue(value, clazz);
		}
		Constructor<?> constructor = clazz.getConstructor(String.class);
		Object instance = constructor.newInstance(value);
		return instance;
	}
}
