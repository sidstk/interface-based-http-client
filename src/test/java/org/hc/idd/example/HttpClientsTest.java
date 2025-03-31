package org.hc.idd.example;

import org.hc.idd.HttpClientFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class HttpClientsTest {

  @Test
  public void callApiForSampleClientA() throws ClassNotFoundException {
    SampleClientA instance =
        HttpClientFactory.create(SampleClientA.class, WebClient.builder().build());
    System.out.println(
        instance
            .callApi1(
                "https://jsonplaceholder.typicode.com/todos/1",
                ParameterizedTypeReference.forType(String.class))
            .block());

    System.out.println(
        instance
            .callApi2(
                "https://jsonplaceholder.typicode.com/todos/1",
                "",
                ParameterizedTypeReference.forType(String.class))
            .block());
  }

  @Test
  public void callApiForSampleClientB() throws ClassNotFoundException {
    SampleClientB instance =
        HttpClientFactory.create(SampleClientB.class, RestClient.builder().build());
    System.out.println(
        (String)
            instance.callApi1(
                "https://jsonplaceholder.typicode.com/todos/1",
                ParameterizedTypeReference.forType(String.class)));
  }
}
