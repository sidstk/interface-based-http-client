package org.hc.idd.example;

import org.hc.idd.compile.annotations.ByBdCompile;
import org.hc.idd.method.annotations.GET;
import org.springframework.core.ParameterizedTypeReference;

@ByBdCompile
public interface SampleClientB {

  // Non-reactive apis
  @GET
  <T> T callApi1(String url, ParameterizedTypeReference<T> responseType);
}
