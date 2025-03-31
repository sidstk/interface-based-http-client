package org.hc.idd;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import org.hc.idd.compile.annotations.ByBdCompile;
import org.hc.idd.compile.annotations.ProxyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByBdClientImplMavenPlugin implements Plugin, Plugin.Factory {
  private static final Logger log = LoggerFactory.getLogger(ByBdClientImplMavenPlugin.class);

  public ByBdClientImplMavenPlugin() {
    log.debug("Init ByBdClientImplMavenPlugin");
  }

  @Override
  public boolean matches(TypeDescription type) {
    // Controlling where plugin needs to apply the case.
    for (AnnotationDescription declaredAnnotation : type.getDeclaredAnnotations()) {
      if (declaredAnnotation
          .getAnnotationType()
          .getSimpleName()
          .equals(ByBdCompile.class.getSimpleName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public DynamicType.Builder<?> apply(
      DynamicType.Builder<?> builder,
      TypeDescription typeDescription,
      ClassFileLocator classFileLocator) {
    log.info("Transforming class {}", typeDescription.getSimpleName());
    boolean isReactiveMethod = false;
    boolean isNonReactiveMethod = false;
    for (MethodDescription.SignatureToken signatureToken :
        typeDescription.getDeclaredMethods().asSignatureTokenList()) {
      if ("Mono".equals(signatureToken.getReturnType().getSimpleName())
          || "Flux".equals(signatureToken.getReturnType().getSimpleName())) {
        isReactiveMethod = true;
      } else {
        isNonReactiveMethod = true;
      }
    }
    try {
      DynamicType helper;
      if (isReactiveMethod && isNonReactiveMethod) {
        helper =
            ByBdClientInterfaceImplFactory.createClientImplClassForRestClientAndWebClientBased(
                    typeDescription)
                .make();

      } else if (isReactiveMethod) {
        helper =
            ByBdClientInterfaceImplFactory.createClientImplClassForWebClientBased(typeDescription)
                .make();
      } else {
        helper =
            ByBdClientInterfaceImplFactory.createClientImplClassForRestClientBased(typeDescription)
                .make();
      }
      boolean isProxyTypeAnnotationExist =
          typeDescription.getDeclaredAnnotations().stream()
              .anyMatch(
                  annotationDescription ->
                      annotationDescription
                          .getAnnotationType()
                          .getSimpleName()
                          .equals(ProxyType.class.getSimpleName()));

      DynamicType.Builder<?> updatedBuilder = builder.require(helper);
      if (!isProxyTypeAnnotationExist) {
        log.info("Adding ProxyType annotation to class {}", typeDescription.getSimpleName());
        updatedBuilder =
            updatedBuilder.annotateType(
                AnnotationDescription.Builder.ofType(ProxyType.class)
                    .define("value", helper.getTypeDescription().getName())
                    .build());
      }
      return updatedBuilder;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {}

  @Override
  public Plugin make() {
    return this;
  }
}
