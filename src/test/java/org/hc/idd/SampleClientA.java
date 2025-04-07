package org.hc.idd;

import org.hc.idd.compile.annotations.ByBdCompile;
import org.hc.idd.method.annotations.GET;
import org.hc.idd.method.annotations.POST;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@ByBdCompile
public interface SampleClientA {

  @GET
  <T> Mono<ResponseEntity<T>> api(String url, ParameterizedTypeReference<T> responseType);

  @POST
  <T> Mono<ResponseEntity<T>> api2(
      String url, Object requestBody, ParameterizedTypeReference<T> responseType);
}
