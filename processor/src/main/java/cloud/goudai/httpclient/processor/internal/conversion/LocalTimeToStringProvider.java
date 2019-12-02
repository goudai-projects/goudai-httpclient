package cloud.goudai.httpclient.processor.internal.conversion;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class LocalTimeToStringProvider extends AbstractJavaTimeToStringProvider {
    @Override
    protected String defaultPattern() {
        return "HH:mm:ss:SSS";
    }

    @Override
    protected String getPattern(ConversionContext context) {
        return context.getTimePattern();
    }
}
