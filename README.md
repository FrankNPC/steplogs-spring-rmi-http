1: The class name and method will be translate to path string: AccountServiceImpl.login -> /account/login
2: Do not use the duplicated method name in the service.
3: The parameters right on the method in types of string and primitive will be on the URL as query string. String will be encoded and decoded.
4: Configure your own serializer and deserializer in order to prevent vulnerability.