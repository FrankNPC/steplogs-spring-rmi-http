1: make sure turn on the -parameters to keep debug info for paramter names of methods.It's to keep the parameters of html post form.
2: The class name and method will be translate to path string: AccountServiceImpl.login -> /account/login
3: Do not use the duplicated method name in the service.
4: The parameters right on the method in types of string and primitive will be on the URL as query string. String will be encoded and decoded.
5: Configure your own serializer and deserializer in order to prevent vulnerability.