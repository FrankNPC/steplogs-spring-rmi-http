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

	@Value("${steplogs.service.profile.host}")
	private String profileHost;

	@Override
	public String getBaseUrl() {
		return profileHost;// must have url
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
	public interface AccountService {
		Object getById(long userId); // it will be GET account/get_by_id?userId=xxxx
		boolean login(String username, String password); // it will be: url POST account/login {username:xxx, password: xxx} 
	}
	
	@Bean // declare the bean with proxy
	AccountService getAccountService() {
		return this.getProxyFactoryBean(AccountService.class, this);
	}

}
