package io.steplogs.spring.rmi.http.prodiver;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.steplogs.spring.rmi.http.HttpHeaderTransporter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ServiceProviderController {

	private static final ObjectMapper objectMapper = JsonMapper.builder()
		.propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
		.build();
	
	@Resource
	@Lazy
	ServiceProviderInvoker serviceProviderInvoker;

	@Bean
	@Lazy
	ServiceProviderInvoker getServiceProviderInvoker() {
		return new ServiceProviderInvoker();
	}

	@GetMapping(value="/*/**", produces= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> get(HttpServletRequest request, HttpServletResponse response) {
		try {
			Object ret = serviceProviderInvoker.get(request, response);
			return ResponseEntity.status(HttpStatus.OK).headers(getHttpHeaders()).body(objectMapper.writeValueAsString(ret));
		} catch (Exception e) {
			try {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(getHttpHeaders()).body(objectMapper.writeValueAsString(serviceProviderInvoker.getErrorHandler().handle(e)));
			} catch (JsonProcessingException e1) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(getHttpHeaders()).body(ServiceProviderInvoker.INTERNAL_SERVICE_ERROR);
			}
		}
	}

	@PostMapping(value="/*/**", produces= {MediaType.APPLICATION_JSON_VALUE}, consumes= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> post(HttpServletRequest request, HttpServletResponse response,
						@RequestBody Map<String, Object> formBody) {
		try {
			Object ret = serviceProviderInvoker.post(request, response, formBody);
			return ResponseEntity.status(HttpStatus.OK).headers(getHttpHeaders()).body(objectMapper.writeValueAsString(ret));
		} catch (Exception e) {
			try {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(getHttpHeaders()).body(objectMapper.writeValueAsString(serviceProviderInvoker.getErrorHandler().handle(e)));
			} catch (JsonProcessingException e1) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(getHttpHeaders()).body(ServiceProviderInvoker.INTERNAL_SERVICE_ERROR);
			}
		}
	}

	private HttpHeaders getHttpHeaders() {
	    HttpHeaders headers = new HttpHeaders();
		HttpHeaderTransporter httpHeaderTransporter = serviceProviderInvoker.getHttpHeaderTransporter();
		if (httpHeaderTransporter!=null) {
			Map<String, List<String>> headerValues = httpHeaderTransporter.getHttpHeaders();
			if (headerValues!=null) {
				for(Map.Entry<String, List<String>> entry : headerValues.entrySet()) {
					headers.addAll(entry.getKey(), entry.getValue());
				}
			}
		}
		return headers;
	}
}
