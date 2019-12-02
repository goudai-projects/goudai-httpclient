package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.utils.Collections;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class BigDecimalToStringProvider extends SimpleToStringProvider implements ToStringProvider {

    @Override
    public String getExpresion(ConversionContext context) {
        new DecimalFormat("").format(BigDecimal.ONE);
        if (StringUtils.isEmpty(context.getDecimalFormat())) {
            return "new " + context.getTypeFactory().getType(DecimalFormat.class).getName() + "().format(<SOURCE>)";
        } else {
            return "new " + context.getTypeFactory().getType(DecimalFormat.class).getName() + "(\"" + context.getDecimalFormat() + "\").format(<SOURCE>)";
        }
    }

    @Override
    public Set<Type> getImportTypes(ConversionContext context) {
        return Collections.asSet(context.getTypeFactory().getType(DecimalFormat.class));
    }
}
