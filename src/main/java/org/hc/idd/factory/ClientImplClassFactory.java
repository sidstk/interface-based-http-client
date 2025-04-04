package org.hc.idd.factory;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;

public interface ClientImplClassFactory {
  DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForWebClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException;

  DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForRestClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException;

  DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?>
      createClientImplClassForRestClientAndWebClientBased(TypeDescription typeDescription)
          throws NoSuchMethodException;
}
