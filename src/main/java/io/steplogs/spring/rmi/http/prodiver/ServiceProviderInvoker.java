package io.steplogs.spring.rmi.http.prodiver;

import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
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
	
	static String defaultCharSet = Charset.defaultCharset().name();

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
		Object[] requestParams = parseParameters(requestMapper, params, null);
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
		Object[] requestParams = parseParameters(requestMapper, params, formBody);
		return target.getMethod().invoke(target.getBean(), requestParams);
	}
	
	static Object[] parseParameters(Map<String, String[]> requestMapper, Parameter[] params, Map<String, Object> formBody) throws Exception {
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
			}else if (formBody!=null){
				Class<?> clazz = params[i].getType();
				Object obj = formBody.get(params[i].getName());
				requestParams[i] = parseValue(clazz, obj);
			}
		}
		return requestParams;
	}
	
	private static interface Converter {
		Object convert(String strValue, Class<?> toClass) throws Exception;
	}
	private static Map<Class<?>, Converter> converters = initiateConverters();
	static Map<Class<?>, Converter> initiateConverters(){
		Map<Class<?>, Converter> theConverters = new HashMap<>();
		theConverters.put(String.class, (strValue, toClass) -> strValue.isEmpty() ? "" : URLDecoder.decode(String.valueOf(strValue), defaultCharSet));
		theConverters.put(Void.class, (strValue, toClass) -> null);
		theConverters.put(Boolean.class, (strValue, toClass) -> strValue.isEmpty() ? false : Boolean.parseBoolean(strValue));
		theConverters.put(Byte.class, (strValue, toClass) -> strValue.isEmpty() ? (byte)0 : Byte.parseByte(strValue));
		theConverters.put(Character.class, (strValue, toClass) -> strValue.isEmpty() ? '0' : strValue.charAt(0));
		theConverters.put(Short.class, (strValue, toClass) -> strValue.isEmpty() ? (short)0 : Short.parseShort(strValue));
		theConverters.put(Integer.class, (strValue, toClass) -> strValue.isEmpty() ? 0 : Integer.parseInt(strValue));
		theConverters.put(Long.class, (strValue, toClass) -> strValue.isEmpty() ? 0L : Long.parseLong(strValue));
		theConverters.put(Float.class, (strValue, toClass) -> strValue.isEmpty() ? 0F : Float.parseFloat(strValue));
		theConverters.put(Double.class, (strValue, toClass) -> strValue.isEmpty() ? 0D : Double.parseDouble(strValue));
		return theConverters;
	}

	static Object parseValue(Class<?> clazz, Object value) throws Exception {
		if (value==null) {
			return null;
		}
		Converter converter = converters.get(clazz);
		if (converter==null) {
			return objectMapper.convertValue(value, clazz);
		}else {
			return converter.convert(String.valueOf(value), clazz);
		}
	}
	
}