package org.hc.idd;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class HttpClientService {

  public static <T> Mono<ResponseEntity<T>> get(
      WebClient webClient, String url, ParameterizedTypeReference<T> responseType) {
    return webClient
        .get()
        .uri(url)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(responseType));
  }

  public static <T> Mono<ResponseEntity<T>> post(
      WebClient webClient,
      String url,
      Object requestBody,
      ParameterizedTypeReference<T> responseType) {
    return webClient
        .post()
        .uri(url)
        .bodyValue(requestBody)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(responseType));
  }

  public static <T> Mono<ResponseEntity<T>> put(
      WebClient webClient,
      String url,
      Object requestBody,
      ParameterizedTypeReference<T> responseType) {
    return webClient
        .put()
        .uri(url)
        .bodyValue(requestBody)
        .exchangeToMono(clientResponse -> clientResponse.toEntity(responseType));
  }

  public static <T> T get(
      RestClient restClient, String url, ParameterizedTypeReference<T> responseType) {
    return restClient.get().uri(url).retrieve().body(responseType);
  }

  public static <T> T post(
      RestClient restClient,
      String url,
      Object requestBody,
      ParameterizedTypeReference<T> responseType) {
    return restClient.post().uri(url).body(requestBody).retrieve().body(responseType);
  }

  public static <T> T put(
      RestClient restClient,
      String url,
      Object requestBody,
      ParameterizedTypeReference<T> responseType) {
    return restClient.put().uri(url).body(requestBody).retrieve().body(responseType);
  }
}
