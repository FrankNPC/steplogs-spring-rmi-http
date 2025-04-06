package io.steplogs.spring.rmi.http.subscriber;


import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration // declare @Configuration to scan beans
public class ExampleServiceSubscriber<T> extends AbstractServiceSubscriber implements ServiceClientTemplate<T> {

	@Value("${service.host}") // eg http://localhost/api
	private String host;

	@Override
	public String getBaseUrl() {
		return host;// must have url
	}

	// I use a http connection pool for RestClient
	private CloseableHttpClient httpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(1024);
		connectionManager.setDefaultMaxPerRoute(1024);
		connectionManager.setConnectionConfigResolver(route -> {
			return ConnectionConfig.custom()
					.setConnectTimeout(15, TimeUnit.SECONDS)
					.setSocketTimeout(15, TimeUnit.SECONDS)
					.setTimeToLive(2, TimeUnit.MINUTES)
					.setValidateAfterInactivity(30, TimeUnit.SECONDS)
					.build();
		});

		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.setConnectionManagerShared(true)
				.build();
	}
	
	@Override
	public RestClient getRestClient() {
		return RestClient
			.builder(new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient())))
			.baseUrl(getBaseUrl())
//            .messageConverters(HttpMessageConverter) // customize pay load convertors instead of default
			.build();
	}

	// Just an example
	public interface AccountService {//Urls will be http://localhost/api/account/get_by_id 
		Object getById(long userId); // it will be GET account/get_by_id?userId=xxxx
		boolean login(String username, String password); // it will be: url POST account/login {username:xxx, password: xxx} 
	}
	
	@Bean // declare the bean with proxy
	AccountService getAccountService() {
		return this.getProxyFactoryBean(AccountService.class, this);
	}

}
