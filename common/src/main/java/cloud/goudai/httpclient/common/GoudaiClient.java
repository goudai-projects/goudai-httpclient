package cloud.goudai.httpclient.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jianglin
 * @date 2018/11/22
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GoudaiClient {

    String value();

    String datePattern() default "";
}
