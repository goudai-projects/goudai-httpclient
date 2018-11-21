package cloud.goudai.httpclient.common;

import lombok.Data;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * method metadata
 *
 * @author jianglin
 * @date 2018/11/21
 */
@Data
public class MethodMetadata implements Serializable {

    private String name;
    private Annotation[] annotations;
    private Map<Integer, ParamMetadata> indexParamMap = new HashMap<>();
}
