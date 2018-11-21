package cloud.goudai.httpclient.common;

import lombok.Data;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * parameters metadata
 *
 * @author jianglin
 * @date 2018/11/21
 */
@Data
public class ParamMetadata implements Serializable {

    private String name;
    private Type type;
    private Annotation[] annotations;
}
