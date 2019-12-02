package cloud.goudai.httpclient.processor.internal.processor;

import javax.lang.model.element.TypeElement;

/**
 * @author jianglin
 * @date 2019/11/25
 */
public interface ClientProcessor<P, R> {

    R process(ProcessorContext context, TypeElement typeElement, P source);

    int getPriority();
}
