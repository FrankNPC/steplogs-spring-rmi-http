package steplogs.spring.rmi.http.subscriber;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;

public abstract class AbstractInvokerClient<T> {

	protected final RestClient restClient;
	protected final ErrorHandler errorHandler;
	protected final T errorResponse;
	protected final HttpHeaderTransporter httpHeaderTransporter;
	
	/**
	 * 
	 * @param restClient				RestClient instance
	 * @param httpHeaderTransporter		Copy, add or transport headers such as Authorization
	 * @param errorHandler				Error handler
	 * @param errorResponse				Return the default object if there is an error
	 */
	public AbstractInvokerClient(RestClient restClient, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse) {
		this.restClient = restClient;
		this.errorHandler = errorHandler;
		this.httpHeaderTransporter = httpHeaderTransporter;
		this.errorResponse = errorResponse;
	}

	/**
	 * 
	 * @param host						Host
	 * @param httpHeaderTransporter		Copy, add or transport headers such as Authorization
	 * @param errorHandler				Error handler
	 * @param errorResponse				Return the default object if there is an error
	 */
	public AbstractInvokerClient(String host, HttpHeaderTransporter httpHeaderTransporter, ErrorHandler errorHandler, T errorResponse) {
		this(
				RestClient
					.builder().requestFactory(new HttpComponentsClientHttpRequestFactory())
					.baseUrl(host)
	//				.messageConverters(null)
					.build(), 
				httpHeaderTransporter, errorHandler, errorResponse);
	}
	
	/**
	 * @param path				path
	 * @param queryVariables	Query variables on the URL
	 * @param typeRef			Type convert to
	 */
	protected T get(String path, Map<String, Object> queryVariables, ParameterizedTypeReference<T> typeRef) {
		return restClient
				.get()
				.uri(builder -> {
					builder.path(path);
					for (Map.Entry<String, Object> entry : queryVariables.entrySet()) {
						Object obj = entry.getValue();
						if (obj instanceof String) {
							String val = (String) obj;
							try {
								val = URLEncoder.encode(val, "UTF-8");
								builder.queryParam(entry.getKey(), val);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}else {
							builder.queryParam(entry.getKey(), obj);
						}
					}
					return builder.build();
				})
				.headers(headers -> {
					if (httpHeaderTransporter!=null) {
						Map<String, String> httpHeaders = httpHeaderTransporter.getHttpHeaders();
						if (httpHeaders!=null) {
							for(Map.Entry<String, String> entry : httpHeaders.entrySet()) {
								headers.addIfAbsent(entry.getKey(), entry.getValue());
							}
						}
					}
				})
				.accept(MediaType.APPLICATION_JSON)
				.exchange((request, response) -> {
					if (response.getStatusCode().isError()) {
						if (errorHandler!=null) {
							errorHandler.handle(request, response);
						}
						return errorResponse;
					} else {
						try {
							return response.bodyTo(typeRef);
						}catch(Exception e) {
							return errorResponse;
						}
					}
				});
	}

	/**
	 * No URL query parameters
	 * @param path				path
	 * @param queryVariables	Query variables on the URL
	 * @param formData			Form data
	 * @param typeRef			Type convert to
	 */
	protected T post(String path, Map<String, Object> queryVariables, Map<String, Object> formData, ParameterizedTypeReference<T> typeRef) {
		return restClient
				.post()
				.uri(builder -> {
					builder.path(path);
					for (Map.Entry<String, Object> entry : queryVariables.entrySet()) {
						Object obj = entry.getValue();
						if (obj instanceof String) {
							String val = (String) obj;
							try {
								val = URLEncoder.encode(val, "UTF-8");
								builder.queryParam(entry.getKey(), val);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}else {
							builder.queryParam(entry.getKey(), obj);
						}
					}
					return builder.build();
				})
				.headers(headers -> {
					if (httpHeaderTransporter!=null) {
						Map<String, String> httpHeaders = httpHeaderTransporter.getHttpHeaders();
						if (httpHeaders!=null) {
							for(Map.Entry<String, String> entry : httpHeaders.entrySet()) {
								headers.addIfAbsent(entry.getKey(), entry.getValue());
							}
						}
					}
				})
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.body(formData)
				.exchange((request, response) -> {
					if (response.getStatusCode().isError()) {
						if (errorHandler!=null) {
							errorHandler.handle(request, response);
						}
						return errorResponse;
					} else {
						try {
							return response.bodyTo(typeRef);
						}catch(Exception e) {
							return errorResponse;
						}
					}
				});
	}
	
}
