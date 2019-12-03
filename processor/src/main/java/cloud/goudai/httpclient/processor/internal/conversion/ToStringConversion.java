package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.ModelElement;
import cloud.goudai.httpclient.processor.internal.model.common.Type;

import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class ToStringConversion implements ModelElement {

    private static final String SOURCE_REFERENCE_PATTERN = "<SOURCE>";
    private final Set<Type> importTypes;
    private final String openExpression;
    private final String closeExpression;

    public ToStringConversion(Set<Type> importTypes, String expression) {
        this.importTypes = importTypes;
        int patternIndex = expression.indexOf(SOURCE_REFERENCE_PATTERN);
        this.openExpression = expression.substring(0, patternIndex);
        this.closeExpression = expression.substring(patternIndex + 8);
    }

    public String getOpenExpression() {
        return openExpression;
    }

    public String getCloseExpression() {
        return closeExpression;
    }

    @Override
    public Set<Type> getImportTypes() {
        return importTypes;
    }
}
