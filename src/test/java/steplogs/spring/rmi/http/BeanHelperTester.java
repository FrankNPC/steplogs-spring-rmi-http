package steplogs.spring.rmi.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import steplogs.spring.rmi.http.prodiver.Provider;

@Provider
public class BeanHelperTester {

	@Test
	public void test_ServiceProviderConfiguration() {
		String name = BeanHelper.parseServiceName("AccountServiceImpl");
		Assertions.assertEquals("account", name);

		String method = BeanHelper.parseMethodName(name, "loginByUserName");
		Assertions.assertEquals("/account/login_by_user_name", method);
	}

	@Provider
	public void test_ServiceProviderConfiguration_detectAnnotationByName() {
		Set<String> annotationNames = new HashSet<>(Arrays.asList("Provider"));
		boolean isPresent = BeanHelper.detectAnnotationByName(
				BeanHelperTester.class.getAnnotations(), annotationNames);
		Assertions.assertTrue(isPresent);

		boolean isPresent1 = Arrays.asList(BeanHelperTester.class.getMethods())
			.stream().anyMatch(method->BeanHelper.detectAnnotationByName(
					method.getAnnotations(), annotationNames));
		Assertions.assertTrue(isPresent1);

		Set<String> annotationNames1 = new HashSet<>(Arrays.asList("id"));
		boolean isPresent2 = Arrays.asList(BeanHelperTester.class.getMethods())
			.stream().anyMatch(method->BeanHelper.detectAnnotationByName(
					method.getAnnotations(), annotationNames1));
		Assertions.assertFalse(isPresent2);
	}
}
