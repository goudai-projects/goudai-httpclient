package cloud.goudai.httpclient.processor.internal.utils;

import com.google.auto.common.AnnotationMirrors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;

/**
 * @author jianglin
 * @date 2019/11/29
 */

public final class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static AnnotationValue getAnnotationValueWithDefault(AnnotationMirror annotationMirror,
                                                                final String elementName) {
        Map.Entry<ExecutableElement, AnnotationValue> elementAndValue =
                AnnotationMirrors.getAnnotationElementAndValue(annotationMirror, elementName);

        return elementAndValue.getValue() != null ? elementAndValue.getValue() :
                elementAndValue.getKey().getDefaultValue();
    }
}
