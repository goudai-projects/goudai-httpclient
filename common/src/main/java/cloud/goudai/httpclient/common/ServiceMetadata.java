package cloud.goudai.httpclient.common;

import lombok.Data;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * service interface metadata
 *
 * @author jianglin
 * @date 2018/11/21
 */
@Data
public class ServiceMetadata implements Serializable {

    private String name;
    private Type type;
    private Annotation[] annotations;
    private List<MethodMetadata> methodMetadataList;
}
