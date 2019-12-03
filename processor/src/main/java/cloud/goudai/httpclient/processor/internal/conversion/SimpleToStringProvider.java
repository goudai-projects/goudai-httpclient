package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Type;

import java.util.Collections;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public abstract class SimpleToStringProvider implements ToStringProvider {

    @Override
    public ToStringConversion toString(ConversionContext context) {
        return new ToStringConversion(
                getImportTypes(context),
                getExpresion(context)
        );
    }

    public abstract String getExpresion(ConversionContext context);

    protected Set<Type> getImportTypes(ConversionContext conversionContext) {
        return Collections.emptySet();
    }
}
