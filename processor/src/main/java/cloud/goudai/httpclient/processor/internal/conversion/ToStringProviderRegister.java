package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Type;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public interface ToStringProviderRegister {

    default void registerToStringProvider(Class<?> clazz, ToStringProvider toStringProvider) {

    }

    default void registerToStringProvider(Type type, ToStringProvider toStringProvider) {
        
    }
}
