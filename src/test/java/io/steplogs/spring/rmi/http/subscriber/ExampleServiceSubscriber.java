package io.steplogs.spring.rmi.http.subscriber;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;

@Configuration // declare @Configuration to scan beans
public class ExampleServiceSubscriber<T> extends AbstractServiceSubscriber implements ServiceClientTemplate<T> {

	@Value("${service.host}") // eg http://localhost/api
	private String host;

	@Override
	public String getBaseUrl() {
		return host;// must have url
	}

	@Resource 
	CloseableHttpClient closeableHttpClient;

	@Override
	public RestClient getRestClient() {
		return RestClient
			.builder(new RestTemplate(new HttpComponentsClientHttpRequestFactory(closeableHttpClient)))
			.baseUrl(getBaseUrl())
//            .messageConverters(HttpMessageConverter)
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
