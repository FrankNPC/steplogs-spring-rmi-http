package io.steplogs.spring.rmi.http.prodiver;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ServiceProviderController {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	@Resource
	protected ServiceProviderInvoker serviceProviderInvoker;

	@GetMapping(value="/*/**", produces= {MediaType.APPLICATION_JSON_VALUE})
	public String get(HttpServletRequest request, HttpServletResponse response) {
		try {
			Object ret = serviceProviderInvoker.get(request, response);
			return objectMapper.writeValueAsString(ret);
		} catch (Exception e) {
			try {
				return objectMapper.writeValueAsString(serviceProviderInvoker.getErrorHandler().handle(e));
			} catch (JsonProcessingException e1) {
				return ServiceProviderInvoker.INTERNAL_SERVICE_ERROR;
			}
		}
	}

	@PostMapping(value="/*/**", produces= {MediaType.APPLICATION_JSON_VALUE}, consumes= {MediaType.APPLICATION_JSON_VALUE})
	public String post(HttpServletRequest request, HttpServletResponse response,
						@RequestBody Map<String, Object> formBody) {
		try {
			Object ret = serviceProviderInvoker.post(request, response, formBody);
			return objectMapper.writeValueAsString(ret);
		} catch (Exception e) {
			try {
				return objectMapper.writeValueAsString(serviceProviderInvoker.getErrorHandler().handle(e));
			} catch (JsonProcessingException e1) {
				return ServiceProviderInvoker.INTERNAL_SERVICE_ERROR;
			}
		}
	}
	
}
