package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.utils.Collections;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public abstract class AbstractJavaTimeToStringProvider extends SimpleToStringProvider implements ToStringProvider {

    @Override
    public String getExpresion(ConversionContext context) {
        return dateTimeFormatter(context) + withZone(context) + ".format(<SOURCE>)";
    }

    private String dateTimeFormatter(ConversionContext context) {

        return context.getTypeFactory().getType(DateTimeFormatter.class).getName()
                + ".ofPattern( \"" + StringUtils.defaultIfEmpty(getPattern(context),
                defaultPattern())
                + "\" )";
    }

    private String withZone(ConversionContext context) {
        return ".withZone(" + context.getTypeFactory().getType(ZoneId.class).getName() + ".systemDefault())";
    }

    protected abstract String defaultPattern();

    protected abstract String getPattern(ConversionContext context);

    @Override
    protected Set<Type> getImportTypes(ConversionContext context) {
        return Collections.asSet(
                context.getTypeFactory().getType(DateTimeFormatter.class),
                context.getTypeFactory().getType(ZoneId.class)
                                );
    }
}
