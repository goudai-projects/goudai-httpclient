package cloud.goudai.httpclient.processor.internal.model.common;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class AnnotationProcessingException extends RuntimeException {
    private final Element element;
    private final AnnotationMirror annotationMirror;
    private final AnnotationValue annotationValue;

    public AnnotationProcessingException(String message) {
        this(message, null, null, null);
    }

    public AnnotationProcessingException(String message, Element element) {
        this(message, element, null, null);
    }

    public AnnotationProcessingException(String message, Element element, AnnotationMirror annotationMirror) {
        this(message, element, annotationMirror, null);
    }

    public AnnotationProcessingException(String message, Element element, AnnotationMirror annotationMirror,
                                         AnnotationValue annotationValue) {
        super(message);
        this.element = element;
        this.annotationMirror = annotationMirror;
        this.annotationValue = annotationValue;
    }

    public Element getElement() {
        return element;
    }

    public AnnotationMirror getAnnotationMirror() {
        return annotationMirror;
    }

    public AnnotationValue getAnnotationValue() {
        return annotationValue;
    }
}
