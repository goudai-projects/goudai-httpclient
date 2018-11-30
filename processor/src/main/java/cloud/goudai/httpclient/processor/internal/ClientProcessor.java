package cloud.goudai.httpclient.processor.internal;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;

/**
 * @author jianglin
 * @date 2018-11-29
 */
public interface ClientProcessor {

    void processType(TypeElement typeElement);

    CodeBlock processMethod(Method method);
}
