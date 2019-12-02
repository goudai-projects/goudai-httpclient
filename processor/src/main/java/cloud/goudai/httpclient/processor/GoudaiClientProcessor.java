package cloud.goudai.httpclient.processor;

import cloud.goudai.httpclient.common.GoudaiClient;
import cloud.goudai.httpclient.processor.internal.conversion.ToStringProviderRegisterAdapter;
import cloud.goudai.httpclient.processor.internal.processor.ClientProcessor;
import cloud.goudai.httpclient.processor.internal.processor.ProcessorContext;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CLASS;

/**
 * @author jianglin
 * @date 2018/11/21
 */
@SupportedAnnotationTypes({
        "cloud.goudai.httpclient.common.GoudaiClient"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class GoudaiClientProcessor extends AbstractProcessor {
    private AnnotationProcessorContext annotationProcessorContext;
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        annotationProcessorContext = new AnnotationProcessorContext(elementUtils, typeUtils, messager, filer);
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        Set<TypeElement> clients = getClients(annotations, roundEnv);
        processClients(clients);
        return false;
    }

    private void processClients(final Set<TypeElement> clients) {

        for (TypeElement typeElement : clients) {
            GoudaiClient goudaiClient = typeElement.getAnnotation(GoudaiClient.class);
            if (goudaiClient == null) {
                continue;
            }
            String datePattern = goudaiClient.datePattern();
            String timePattern = goudaiClient.timePattern();
            String decimalFormat = goudaiClient.decimalFormat();
            ProcessorContext processorContext = new ProcessorContext(
                    typeUtils,
                    elementUtils,
                    filer,
                    messager,
                    annotationProcessorContext,
                    datePattern, timePattern, decimalFormat,
                    getDeclaredTypesNotToBeImported(typeElement),
                    getProviders());
            process(typeElement, processorContext);
        }
    }

    private void process(TypeElement typeElement, ProcessorContext processorContext) {
        Object model = null;
        for (ClientProcessor<?, ?> processor : getProcessors()) {
            process(processorContext, processor, typeElement, model);
        }
    }

    private <P, R> R process(ProcessorContext context, ClientProcessor<P, R> processor,
                             TypeElement mapperTypeElement, Object modelElement) {
        @SuppressWarnings("unchecked")
        P sourceElement = (P) modelElement;
        return processor.process(context, mapperTypeElement, sourceElement);
    }

    private Iterable<ClientProcessor<?, ?>> getProcessors() {

        Iterator<ClientProcessor> processorIterator = ServiceLoader.load(
                ClientProcessor.class,
                GoudaiClientProcessor.class.getClassLoader())
                .iterator();
        List<ClientProcessor<?, ?>> processors = new ArrayList<>();

        while (processorIterator.hasNext()) {
            processors.add(processorIterator.next());
        }
        Collections.sort(processors, new ProcessorComparator());
        return processors;
    }

    private List<ToStringProviderRegisterAdapter> getProviders() {

        Iterator<ToStringProviderRegisterAdapter> providerIterator = ServiceLoader.load(
                ToStringProviderRegisterAdapter.class,
                GoudaiClientProcessor.class.getClassLoader())
                .iterator();
        List<ToStringProviderRegisterAdapter> providers = new ArrayList<>();

        while (providerIterator.hasNext()) {
            providers.add(providerIterator.next());
        }
        return providers;
    }


    private Map<String, String> getDeclaredTypesNotToBeImported(TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(e -> CLASS.equals(e.getKind()))
                .map(Element::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.toMap(k -> k, v -> element.getQualifiedName().toString() + "." + v));
    }

    private Set<TypeElement> getClients(final Set<? extends TypeElement> annotations,
                                        final RoundEnvironment roundEnv) {
        Set<TypeElement> clients = new HashSet<>();

        for (TypeElement annotation : annotations) {
            //Indicates that the annotation's type isn't on the class path of the compiled
            //project. Let the compiler deal with that and print an appropriate error.
            if (annotation.getKind() != ElementKind.ANNOTATION_TYPE) {
                continue;
            }

            try {
                Set<? extends Element> annotatedMappers = roundEnv.getElementsAnnotatedWith(annotation);
                for (Element mapperElement : annotatedMappers) {
                    if (mapperElement.getKind() != ElementKind.INTERFACE) {
                        continue;
                    }
                    TypeElement mapperTypeElement = asTypeElement(mapperElement);

                    // on some JDKs, RoundEnvironment.getElementsAnnotatedWith( ... ) returns types with
                    // annotations unknown to the compiler, even though they are not declared Mappers
                    if (mapperTypeElement != null) {
                        clients.add(mapperTypeElement);
                    }
                }
            } catch (Throwable t) { // whenever that may happen, but just to stay on the save side
                handleUncaughtError(annotation, t);
                continue;
            }
        }
        return clients;
    }

    private TypeElement asTypeElement(Element element) {
        return element.accept(
                new ElementKindVisitor8<TypeElement, Void>() {
                    @Override
                    public TypeElement visitTypeAsInterface(TypeElement e, Void p) {
                        return e;
                    }

                    @Override
                    public TypeElement visitTypeAsClass(TypeElement e, Void p) {
                        return e;
                    }

                }, null
                             );
    }

    private void handleUncaughtError(Element element, Throwable thrown) {
        StringWriter sw = new StringWriter();
        thrown.printStackTrace(new PrintWriter(sw));

        String reportableStacktrace = sw.toString().replace(System.getProperty("line.separator"), "  ");

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "Internal error in the mapping processor: " + reportableStacktrace, element);
    }

    private static class ProcessorComparator implements Comparator<ClientProcessor<?, ?>> {

        @Override
        public int compare(ClientProcessor<?, ?> o1,
                           ClientProcessor<?, ?> o2) {
            return Integer.compare(o1.getPriority(), o2.getPriority());
        }
    }
}
