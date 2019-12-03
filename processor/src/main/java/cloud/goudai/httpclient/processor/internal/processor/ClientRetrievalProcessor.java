package cloud.goudai.httpclient.processor.internal.processor;

import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.model.common.TypeFactory;
import cloud.goudai.httpclient.processor.internal.model.rest.Client;

import javax.lang.model.element.TypeElement;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class ClientRetrievalProcessor implements ClientProcessor<Object, Client> {

    @Override
    public Client process(ProcessorContext context, TypeElement typeElement, Object source) {
        TypeFactory typeFactory = context.getTypeFactory();
        Type type = typeFactory.getType(typeElement);
        return context.getClientFactory().getClient(type);
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
