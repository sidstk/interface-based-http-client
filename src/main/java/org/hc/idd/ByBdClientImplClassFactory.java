package org.hc.idd;

import java.util.List;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import org.hc.idd.factory.ClientImplClassFactory;
import org.hc.idd.proxy.HttpMethodProxy;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

class ByBdClientImplClassFactory implements ClientImplClassFactory {
  private final List<HttpMethodProxy> httpMethodProxyList;

  ByBdClientImplClassFactory(List<HttpMethodProxy> httpMethodProxyList) {
    this.httpMethodProxyList = httpMethodProxyList;
  }

  @Override
  public DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForWebClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException {
    var builder =
        createSubClass(typeDescription)
            .defineField("webClient", WebClient.class, Opcodes.ACC_PRIVATE)
            .defineConstructor(Opcodes.ACC_PUBLIC)
            .withParameters(WebClient.class)
            .intercept(
                MethodCall.invoke(Object.class.getConstructor())
                    .andThen(FieldAccessor.ofField("webClient").setsArgumentAt(0)));
    return addMethodProxies(builder);
  }

  @Override
  public DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForRestClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException {
    var builder =
        createSubClass(typeDescription)
            .defineField("restClient", RestClient.class, Opcodes.ACC_PRIVATE)
            .defineConstructor(Opcodes.ACC_PUBLIC)
            .withParameters(RestClient.class)
            .intercept(
                MethodCall.invoke(Object.class.getConstructor())
                    .andThen(FieldAccessor.ofField("restClient").setsArgumentAt(0)));
    return addMethodProxies(builder);
  }

  @Override
  public DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForRestClientAndWebClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException {
    var builder =
        createSubClass(typeDescription)
            .defineField("webClient", WebClient.class, Opcodes.ACC_PRIVATE)
            .defineField("restClient", RestClient.class, Opcodes.ACC_PRIVATE)
            .defineConstructor(Opcodes.ACC_PUBLIC)
            .withParameters(WebClient.class, RestClient.class)
            .intercept(
                MethodCall.invoke(Object.class.getConstructor())
                    .andThen(FieldAccessor.ofField("webClient").setsArgumentAt(0))
                    .andThen(FieldAccessor.ofField("restClient").setsArgumentAt(1)));
    return addMethodProxies(builder);
  }

  private DynamicType.Builder<?> createSubClass(TypeDescription typeDescription) {
    return new ByteBuddy()
        .subclass(typeDescription, ConstructorStrategy.Default.NO_CONSTRUCTORS)
        .name(typeDescription.getName() + "Impl");
  }

  private DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> addMethodProxies(
      DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> builder)
      throws NoSuchMethodException {
    for (HttpMethodProxy httpMethodProxy : httpMethodProxyList) {
      builder = httpMethodProxy.addMethodProxy(builder);
    }
    return builder;
  }
}
