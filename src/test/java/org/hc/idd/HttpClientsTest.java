package org.hc.idd;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import org.hc.idd.proxy.HttpGetMethodProxy;
import org.hc.idd.proxy.HttpPostMethodProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class HttpClientsTest {
  static HttpClientFactory httpClientFactory;

  @BeforeAll
  public static void setup() {
    httpClientFactory =
        new HttpClientFactory(
            new ByBdClientImplClassFactory(
                List.of(new HttpGetMethodProxy(), new HttpPostMethodProxy())));
  }

  @Test
  public void callApiForSampleClientA() throws ClassNotFoundException {
    SampleClientA instance =
        httpClientFactory.create(SampleClientA.class, WebClient.builder().build());
    try (MockedStatic<HttpClientService> mockedStatic =
        Mockito.mockStatic(HttpClientService.class)) {
      ResponseEntity<String> mockedResponse = ResponseEntity.ok().body("dummy response");
      mockedStatic
          .when(() -> HttpClientService.get(any(WebClient.class), any(), any()))
          .thenReturn(Mono.just(mockedResponse));
      mockedStatic
          .when(() -> HttpClientService.post(any(WebClient.class), any(), any(), any()))
          .thenReturn(Mono.just(mockedResponse));
      StepVerifier.create(
              instance.api(
                  "https://jsonplaceholder.typicode.com/todos/1",
                  new ParameterizedTypeReference<String>() {}))
          .expectNext(mockedResponse)
          .verifyComplete();

      StepVerifier.create(
              instance.api2(
                  "https://jsonplaceholder.typicode.com/todos/1",
                  "",
                  new ParameterizedTypeReference<String>() {}))
          .expectNext(mockedResponse)
          .verifyComplete();
    }
  }

  @Test
  public void callApiForSampleClientB() throws ClassNotFoundException {
    SampleClientB instance =
        httpClientFactory.create(SampleClientB.class, RestClient.builder().build());
    try (MockedStatic<HttpClientService> mockedStatic =
        Mockito.mockStatic(HttpClientService.class)) {
      String mockedResponse = "dummy response";
      mockedStatic
          .when(() -> HttpClientService.get(any(RestClient.class), any(), any()))
          .thenReturn(mockedResponse);
      mockedStatic
          .when(() -> HttpClientService.post(any(RestClient.class), any(), any(), any()))
          .thenReturn(mockedResponse);
      Assertions.assertEquals(
          mockedResponse,
          instance.api1(
              "https://jsonplaceholder.typicode.com/todos/1",
              ParameterizedTypeReference.forType(String.class)));
      Assertions.assertEquals(
          mockedResponse,
          instance.api2(
              "https://jsonplaceholder.typicode.com/todos/1",
              "",
              ParameterizedTypeReference.forType(String.class)));
    }
  }
}
