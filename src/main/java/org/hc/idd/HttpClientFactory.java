package org.hc.idd;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Function;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.hc.idd.compile.annotations.ProxyType;
import org.hc.idd.factory.ClientImplClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

public class HttpClientFactory {
  private static final Logger log = LoggerFactory.getLogger(HttpClientFactory.class);
  private final ClientImplClassFactory clientImplClassFactory;

  public HttpClientFactory(ClientImplClassFactory clientImplClassFactory) {
    this.clientImplClassFactory = clientImplClassFactory;
  }

  public <T> T create(Class<T> tClass, WebClient webClient) throws ClassNotFoundException {

    return createInstance(
        tClass,
        aClass -> {
          try {
            return aClass.getConstructor(WebClient.class).newInstance(webClient);
          } catch (InstantiationException
              | NoSuchMethodException
              | IllegalAccessException
              | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        },
        aClass -> {
          try {
            return (DynamicType.Unloaded<T>)
                clientImplClassFactory
                    .createClientImplClassForWebClientBased(
                        new TypeDescription.ForLoadedType(aClass))
                    .make();
          } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        });
  }

  public <T> T create(Class<T> tClass, RestClient restClient) throws ClassNotFoundException {

    return createInstance(
        tClass,
        aClass -> {
          try {
            return aClass.getConstructor(RestClient.class).newInstance(restClient);
          } catch (InstantiationException
              | NoSuchMethodException
              | IllegalAccessException
              | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        },
        aClass -> {
          try {
            return (DynamicType.Unloaded<T>)
                clientImplClassFactory
                    .createClientImplClassForRestClientBased(
                        new TypeDescription.ForLoadedType(aClass))
                    .make();
          } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private <T> T createInstance(
      Class<T> tClass,
      Function<Class<? extends T>, T> constructorFunction,
      Function<Class<T>, DynamicType.Unloaded<T>> byteBuddyHandler)
      throws ClassNotFoundException {
    var proxyTypeAnnotation = tClass.getDeclaredAnnotation(ProxyType.class);
    if (Objects.nonNull(proxyTypeAnnotation)) {
      // impl class is already present due to maven plugin
      var aClass = (Class<T>) Class.forName(proxyTypeAnnotation.value());
      log.info("Loading compiled class: {}", aClass);
      return constructorFunction.apply(aClass);
    } else {
      // need to create class in runtime.
      try (var unloaded = byteBuddyHandler.apply(tClass)) {
        log.info("Loading runtime created class {}", tClass);
        return constructorFunction.apply(
            unloaded
                .load(tClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded());
      }
    }
  }
}
