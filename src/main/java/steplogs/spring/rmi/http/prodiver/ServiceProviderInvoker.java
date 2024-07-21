package steplogs.spring.rmi.http.prodiver;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ServiceProviderInvoker {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String DEFAULT_ERROR_RESPONSE = "{\"code\": -1, \"message\": \"service doesn't exist\", \"success\": false}";
	public static final String INTERNAL_SERVICE_ERROR = "{\"code\": -1, \"message\": \"Internal Servie Error\", \"success\": false}";

	private static final ErrorHandler DEFAULT_ERROR_HANDLER = new ErrorHandler() {
		public String handle(Throwable thrownException) {
			thrownException.printStackTrace();
			return  INTERNAL_SERVICE_ERROR;
		}
	};
	
	@Resource
	protected ServiceProviderConfiguration serviceProviderConfiguration;

	@Autowired(required = false)
	private ErrorHandler errorHandler;
	
	public ErrorHandler getErrorHandler() {
		return errorHandler = errorHandler!=null?errorHandler:DEFAULT_ERROR_HANDLER;
	}
	
	public Object get(HttpServletRequest request, HttpServletResponse response) throws Exception {
		URI url = URI.create(request.getRequestURL().toString());
		String path = url.getPath();
		if (path==null || !path.matches("^/[a-zA-Z0-9_\\-]+/[a-zA-Z0-9_\\-]+.*$")) {
			return DEFAULT_ERROR_RESPONSE;
		}
		InvokeTarget target = serviceProviderConfiguration.getServiceInvokeTarget(path);
		if (target==null) {
			return DEFAULT_ERROR_RESPONSE;
		}
		
		Map<String, String[]> requestMapper = request.getParameterMap();
		
		Parameter[] params = target.getMethod().getParameters();
		Object[] requestParams = new Object[params.length];
		for(int i=0; i<requestParams.length; i++) {
			String[] values = requestMapper.get(params[i].getName());
			if (values!=null) {
				Class<?> clazz = params[i].getType();
				if (values.length==1) {
					requestParams[i] = parseValue(clazz, values[0]);
				}else {
					Object[] objects = new Object[values.length];
					for(int k=0; k<values.length; k++) {
						objects[k] = parseValue(clazz, values[k]);
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
			return DEFAULT_ERROR_RESPONSE;
		}
		InvokeTarget target = serviceProviderConfiguration.getServiceInvokeTarget(path);
		if (target==null) {
			return DEFAULT_ERROR_RESPONSE;
		}

		Map<String, String[]> requestMapper = request.getParameterMap();

		Parameter[] params = target.getMethod().getParameters();
		Object[] requestParams = new Object[params.length];
		for(int i=0; i<requestParams.length; i++) {
			String[] values = requestMapper.get(params[i].getName());
			Class<?> clazz = params[i].getType();
			if (values!=null) {
				if (values.length==1) {
					requestParams[i] = parseValue(clazz, values[0]);
				}else {
					Object[] objects = new Object[values.length];
					for(int k=0; k<values.length; k++) {
						objects[k] = parseValue(clazz, values[k]);
					}
					requestParams[i] = objects;
				}
			}else {
				Object obj = formBody.get(params[i].getName());
				requestParams[i] = parseValue(clazz, obj);
			}
		}
		
		return target.getMethod().invoke(target.getBean(), requestParams);
	}

	public static final Object parseValue(Class<?> clazz, Object value) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (value==null) {
			return null;
		}
		if (String.class.equals(clazz)) {
			try {
				return URLDecoder.decode(String.valueOf(value), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else if (Void.class.equals(clazz)) {
			return null;
		}else if (clazz.isPrimitive()) {
			String typeName = clazz.getTypeName();
			String strValue = value.toString();
			if ("boolean".equals(typeName)) {
				return strValue.isEmpty() ? false : Boolean.parseBoolean(strValue);
			}
			if ("byte".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Byte.parseByte(strValue);
			}
			if ("char".equals(typeName)) {
				return strValue.isEmpty() ? 0 : strValue.charAt(0);
			}
			if ("short".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Short.parseShort(strValue);
			}
			if ("int".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Integer.parseInt(strValue);
			}
			if ("long".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Long.parseLong(strValue);
			}
			if ("float".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Float.parseFloat(strValue);
			}
			if ("double".equals(typeName)) {
				return strValue.isEmpty() ? 0 : Double.parseDouble(strValue);
			}
		} else {
			return objectMapper.convertValue(value, clazz);
		}
		
		Constructor<?> constructor = clazz.getConstructor(String.class);
		Object instance = constructor.newInstance(value);
		return instance;
	}
	
}
