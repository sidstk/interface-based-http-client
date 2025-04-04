package org.hc.idd.proxy;

import static net.bytebuddy.matcher.ElementMatchers.not;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.hc.idd.HttpClientService;
import org.hc.idd.method.annotations.POST;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CorePublisher;

public class HttpPostMethodProxy implements HttpMethodProxy {

  @Override
  public DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> addMethodProxy(
      DynamicType.Builder<?> classBuilder) throws NoSuchMethodException {
    return classBuilder
        .method(
            ElementMatchers.isAnnotatedWith(POST.class)
                .and(ElementMatchers.returns(ElementMatchers.isSubTypeOf(CorePublisher.class))))
        .intercept(
            MethodCall.invoke(
                    HttpClientService.class.getDeclaredMethod(
                        "post",
                        WebClient.class,
                        String.class,
                        Object.class,
                        ParameterizedTypeReference.class))
                .withField("webClient")
                .withAllArguments())
        .method(
            ElementMatchers.isAnnotatedWith(POST.class)
                .and(
                    not(ElementMatchers.returns(ElementMatchers.isSubTypeOf(CorePublisher.class)))))
        .intercept(
            MethodCall.invoke(
                    HttpClientService.class.getDeclaredMethod(
                        "post",
                        RestClient.class,
                        String.class,
                        Object.class,
                        ParameterizedTypeReference.class))
                .withField("restClient")
                .withAllArguments());
  }
}
