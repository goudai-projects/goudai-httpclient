package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.utils.Collections;

import java.text.SimpleDateFormat;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class DateToStringProvider extends SimpleToStringProvider implements ToStringProvider {

    @Override
    public String getExpresion(ConversionContext context) {
        return getConversionExpression(context, "format");
    }

    @Override
    protected Set<Type> getImportTypes(ConversionContext conversionContext) {
        return Collections.asSet(conversionContext.getTypeFactory().getType(SimpleDateFormat.class));
    }

    private String getConversionExpression(ConversionContext conversionContext, String method) {
        StringBuilder conversionString = new StringBuilder("new ");
        conversionString.append(conversionContext.getTypeFactory().getType(SimpleDateFormat.class).getName());
        conversionString.append('(');

        if (conversionContext.getDatePattern() != null) {
            conversionString.append(" \"");
            conversionString.append(defaultIfBlank(conversionContext.getDatePattern(), "yyyy-MM-dd HH:mm:ss"));
            conversionString.append("\" ");
        }

        conversionString.append(").");
        conversionString.append(method);
        conversionString.append("(<SOURCE>)");

        return conversionString.toString();
    }
}
