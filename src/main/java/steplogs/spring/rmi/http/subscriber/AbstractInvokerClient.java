package steplogs.spring.rmi.http.subscriber;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public abstract class AbstractInvokerClient<T> {

	protected final ServiceTemplate<T> serviceTemplate;
	
	public AbstractInvokerClient(ServiceTemplate<T> serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	/**
	 * @param path				path
	 * @param queryVariables	Query variables on the URL
	 * @param typeRef			Type convert to
	 */
	protected T get(String path, Map<String, Object> queryVariables, ParameterizedTypeReference<T> typeRef) {
		RestClient restClient;
		if (serviceTemplate.getRestClient()!=null) {
			restClient = serviceTemplate.getRestClient();
		}else {
			restClient = RestClient
				.builder().requestFactory(new HttpComponentsClientHttpRequestFactory())
				.baseUrl(serviceTemplate.getBaseUrl())
//				.messageConverters(null)
				.build();
		}
		
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
					if (serviceTemplate.getHttpHeaderTransporter()!=null) {
						Map<String, String> httpHeaders = serviceTemplate.getHttpHeaderTransporter().getHttpHeaders();
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
						if (serviceTemplate.getErrorHandler()!=null) {
							serviceTemplate.getErrorHandler().handle(request, response);
						}
						return serviceTemplate.getDefaultErrorResponse();
					} else {
						try {
							return response.bodyTo(typeRef);
						}catch(Exception e) {
							return serviceTemplate.getDefaultErrorResponse();
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
		RestClient restClient;
		if (serviceTemplate.getRestClient()!=null) {
			restClient = serviceTemplate.getRestClient();
		}else {
			restClient = RestClient
				.builder().requestFactory(new HttpComponentsClientHttpRequestFactory())
				.baseUrl(serviceTemplate.getBaseUrl())
//				.messageConverters(null)
				.build();
		}
		
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
					if (serviceTemplate.getHttpHeaderTransporter()!=null) {
						Map<String, String> httpHeaders = serviceTemplate.getHttpHeaderTransporter().getHttpHeaders();
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
						if (serviceTemplate.getErrorHandler()!=null) {
							serviceTemplate.getErrorHandler().handle(request, response);
						}
						return serviceTemplate.getDefaultErrorResponse();
					} else {
						try {
							return response.bodyTo(typeRef);
						}catch(Exception e) {
							return serviceTemplate.getDefaultErrorResponse();
						}
					}
				});
	}
	
}
