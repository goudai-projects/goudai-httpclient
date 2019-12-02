package cloud.goudai.httpclient.processor.internal.conversion;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class DefaultToStringProvider extends SimpleToStringProvider implements ToStringProvider {
    @Override
    public String getExpresion(ConversionContext context) {
        return "String.valueOf(<SOURCE>)";
    }
}
