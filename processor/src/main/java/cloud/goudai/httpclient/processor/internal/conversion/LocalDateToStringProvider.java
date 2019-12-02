package cloud.goudai.httpclient.processor.internal.conversion;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class LocalDateToStringProvider extends AbstractJavaTimeToStringProvider {
    @Override
    protected String defaultPattern() {
        return "yyyy-MM-dd";
    }

    @Override
    protected String getPattern(ConversionContext context) {
        return context.getDatePattern();
    }
}
