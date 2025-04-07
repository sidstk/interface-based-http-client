interface-based-http-client README
=========================

DESCRIPTION
-----------
HttpClientFactory simplifies the creation of typed HTTP client instances by mapping interface methods to HTTP operations. It supports both asynchronous (reactive) and synchronous HTTP calls using frameworks such as Spring WebFlux’s WebClient and Spring’s RestClient. 

This library leverages the Byte Buddy tool to dynamically create classes at compile-time or runtime. Byte Buddy enables runtime code generation, allowing flexible proxy creation without requiring compile-time definitions.

FEATURES
--------
- Typed Client Generation: Create client instances by providing your API interface and an HTTP client builder.
- Reactive & Synchronous Support: Use the same API definitions for both reactive and synchronous calls.
- Seamless Integration: Works with Spring Web, Spring WebFlux, and Reactor for reactive programming.
- Byte Buddy-Based Dynamic Class Creation: Generates proxy classes at runtime for flexible API client definitions.

GETTING STARTED
---------------
Prerequisites:
- Java JDK 11 or higher.
- Maven or Gradle as your build tool.
- Dependencies: Spring Web/Reactive, Reactor Core, Mockito, JUnit 5, and Byte Buddy.

Installation:
1. Clone the repository:
   - git clone https://github.com/sidstk/interface-based-http-client.git
   - cd interface-based-http-client

2. Build the project:
    - mvn clean install

USAGE
-----
1. Define Your API Client Interface

   For a reactive client using WebClient:
   --------------------------------------------------
   ```
   public interface SampleClientA {
   
    @GET
    <T> Mono<ResponseEntity<T>> api1(String url, ParameterizedTypeReference<T> responseType);
   
    @POST 
    <T> Mono<ResponseEntity<T>> api2(String url, Object requestBody, ParameterizedTypeReference<T> responseType);
   
   }
   ```

   For a synchronous client using RestClient:
   --------------------------------------------------
   ```
   public interface SampleClientB {
   
    @GET
    String api1(String url, ParameterizedTypeReference<String> responseType);
   
    @POST
    String api2(String url, String body, ParameterizedTypeReference<String> responseType);
   
   }
   ```

2. Create an Instance Using HttpClientFactory

   Initialize the factory with the necessary HTTP method proxies:
   --------------------------------------------------
   ```
   HttpClientFactory httpClientFactory = new HttpClientFactory(new ByBdClientImplClassFactory(List.of(new HttpGetMethodProxy(), new HttpPostMethodProxy())));
   ```

   Create your client instances:
    - For a reactive client (using WebClient):
      ```
      SampleClientA clientA = httpClientFactory.create(SampleClientA.class,WebClient.builder().build());
      ```
    - For a synchronous client (using RestClient):
      ```
      SampleClientB clientB = httpClientFactory.create(SampleClientB.class,RestClient.builder().build());
      ```

3. Calling API Methods

   Example for reactive API call:
   --------------------------------------------------
   ```
   Mono<ResponseEntity<String>> apiResponse = clientA.api1("https://jsonplaceholder.typicode.com/todos/1",new ParameterizedTypeReference<String>() {});
   apiResponse.subscribe(response -> System.out.println(response.getBody()));
   ```

   Example for synchronous API call:
   --------------------------------------------------
   ```
   String response = clientB.callApi1("https://jsonplaceholder.typicode.com/todos/1",ParameterizedTypeReference.forType(String.class));
   System.out.println(response);

BYTE BUDDY INTEGRATION
----------------------
The HttpClientFactory leverages Byte Buddy to dynamically generate proxy classes at runtime. This eliminates the need for manually creating implementation classes and allows developers to define API interfaces without worrying about their concrete implementations.

Example of dynamic class creation with Byte Buddy:
--------------------------------------------------
```
new ByteBuddy()
.subclass(Object.class)
.method(ElementMatchers.named("toString"))
.intercept(FixedValue.value("Generated class"))
.make()
.load(getClass().getClassLoader())
.getLoaded();
```

The library internally uses similar mechanisms to provide runtime-generated HTTP client implementations, enhancing flexibility and maintainability.

CONTRIBUTING
------------
Contributions are welcome. Feel free to fork the repository, open issues for feedback, and submit pull requests with improvements or fixes. Please adhere to the existing coding standards and include tests for any new features.

LICENSE
-------
[Specify your project's license here, e.g., MIT License]

ADDITIONAL INFORMATION
----------------------
For more details on how the proxies (e.g., HttpGetMethodProxy and HttpPostMethodProxy) handle interface methods or to integrate additional HTTP methods, refer to the source code documentation. Further enhancements such as custom client configurations and additional methods may be explored as needed.

Enjoy building robust HTTP clients with minimal boilerplate!