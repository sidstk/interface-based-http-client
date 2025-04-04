package org.hc.idd.proxy;

import net.bytebuddy.dynamic.DynamicType;

public interface HttpMethodProxy {

  DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> addMethodProxy(
      DynamicType.Builder<?> classBuilder) throws NoSuchMethodException;
}
