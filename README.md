 - make sure turn on -parameters to keep debug info for parameter names of methods in your the declaration package. It's to keep the parameters of html post/get parameters against methods.

```xml
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.13.0</version>
				<configuration>
	                <compilerArgs>
	                    <arg>-parameters</arg>
	                </compilerArgs>
				</configuration>
			</plugin>
		</plugins>
```

 - The class name and method will be translate to path string:

> AccountServiceImpl.login -> /account/login
> so, o not use the duplicated method name in the service.

 - The parameters right on the method in types of primitives will be on the URL as query string.
 
> so keeping the parameter names are different would be important.

 - Configure serializer and deserializer in order to prevent vulnerability.
 
> See [RestClient.builder().messageConverters(HttpMessageConverter)](https://github.com/FrankNPC/steplogs-spring-rmi-http/blob/main/src/main/java/io/steplogs/spring/rmi/http/subscriber/AbstractInvokerClient.java#L103)