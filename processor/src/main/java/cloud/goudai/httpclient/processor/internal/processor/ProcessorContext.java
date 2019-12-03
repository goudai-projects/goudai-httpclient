package cloud.goudai.httpclient.processor.internal.processor;

import cloud.goudai.httpclient.processor.AnnotationProcessorContext;
import cloud.goudai.httpclient.processor.internal.conversion.ConversionContext;
import cloud.goudai.httpclient.processor.internal.conversion.Conversions;
import cloud.goudai.httpclient.processor.internal.conversion.ToStringProviderRegisterAdapter;
import cloud.goudai.httpclient.processor.internal.model.common.TypeFactory;
import cloud.goudai.httpclient.processor.internal.model.rest.ClientFactory;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;

/**
 * @author jianglin
 * @date 2019/11/25
 */
public class ProcessorContext {

    private final Types typeUtils;
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final AnnotationProcessorContext globalContext;
    private final ConversionContext conversionContext;
    private final TypeFactory typeFactory;
    private final ClientFactory clientFactory;
    private final Conversions conversions;

    public ProcessorContext(Types typeUtils, Elements elementUtils, Filer filer, Messager messager,
                            AnnotationProcessorContext globalContext,
                            String datePattern, String timePattern, String decimalFormat,
                            Map<String, String> notToBeImported,
                            List<ToStringProviderRegisterAdapter> registerAdapters) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.messager = messager;
        this.globalContext = globalContext;
        this.typeFactory = new TypeFactory(elementUtils, typeUtils, messager, notToBeImported);
        this.conversionContext = new ConversionContext(this.typeFactory, datePattern, timePattern, decimalFormat);
        this.clientFactory = new ClientFactory(elementUtils, typeUtils, typeFactory);
        this.conversions = new Conversions(typeFactory, registerAdapters);
    }

    public Types getTypeUtils() {
        return typeUtils;
    }


    public Elements getElementUtils() {
        return elementUtils;
    }


    public Filer getFiler() {
        return filer;
    }

    public Messager getMessager() {
        return messager;
    }


    public AnnotationProcessorContext getGlobalContext() {
        return globalContext;
    }


    public ConversionContext getConversionContext() {
        return conversionContext;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public Conversions getConversions() {
        return conversions;
    }
}
