package org.hc.idd;

import org.hc.idd.compile.annotations.ByBdCompile;
import org.hc.idd.method.annotations.GET;
import org.hc.idd.method.annotations.POST;
import org.springframework.core.ParameterizedTypeReference;

@ByBdCompile
public interface SampleClientB {

  // Non-reactive apis
  @GET
  <T> T api1(String url, ParameterizedTypeReference<T> responseType);

  @POST
  <T> T api2(String url, Object requestBody, ParameterizedTypeReference<T> responseType);
}
