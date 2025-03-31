package org.hc.idd;

import static net.bytebuddy.matcher.ElementMatchers.not;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.hc.idd.method.annotations.GET;
import org.hc.idd.method.annotations.POST;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.CorePublisher;

class ByBdClientInterfaceImplFactory {

  static DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
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
    return addGetMethodProxy(addPostMethodProxy(builder));
  }

  static DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
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
    return addGetMethodProxy(addPostMethodProxy(builder));
  }

  static DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
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
    return addGetMethodProxy(addPostMethodProxy(builder));
  }

  private static DynamicType.Builder<?> createSubClass(TypeDescription typeDescription) {
    return new ByteBuddy()
        .subclass(typeDescription, ConstructorStrategy.Default.NO_CONSTRUCTORS)
        .name(typeDescription.getName() + "Impl");
  }

  private static DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> addGetMethodProxy(
      DynamicType.Builder<?> classBuilder) throws NoSuchMethodException {
    return classBuilder
        .method(
            ElementMatchers.isAnnotatedWith(GET.class)
                .and(ElementMatchers.returns(ElementMatchers.isSubTypeOf(CorePublisher.class))))
        .intercept(
            MethodCall.invoke(
                    HttpClientService.class.getDeclaredMethod(
                        "get", WebClient.class, String.class, ParameterizedTypeReference.class))
                .withField("webClient")
                .withAllArguments())
        .method(
            ElementMatchers.isAnnotatedWith(GET.class)
                .and(
                    not(ElementMatchers.returns(ElementMatchers.isSubTypeOf(CorePublisher.class)))))
        .intercept(
            MethodCall.invoke(
                    HttpClientService.class.getDeclaredMethod(
                        "get", RestClient.class, String.class, ParameterizedTypeReference.class))
                .withField("restClient")
                .withAllArguments());
  }

  private static DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> addPostMethodProxy(
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
