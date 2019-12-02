package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.TypeFactory;

/**
 * @author jianglin
 * @date 2019/11/25
 */
public class ConversionContext {

    private TypeFactory typeFactory;
    private String datePattern;
    private String timePattern;
    private String decimalFormat;

    public ConversionContext(TypeFactory typeFactory,
                             String datePattern, String timePattern, String decimalFormat) {
        this.typeFactory = typeFactory;
        this.datePattern = datePattern;
        this.timePattern = timePattern;
        this.decimalFormat = decimalFormat;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public String getDecimalFormat() {
        return decimalFormat;
    }


    public TypeFactory getTypeFactory() {
        return typeFactory;
    }
}
