package cloud.goudai.httpclient.processor.internal;

import cloud.goudai.httpclient.common.GoudaiClient;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.net.URI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static cloud.goudai.httpclient.processor.internal.Utils.getPath;
import static cloud.goudai.httpclient.processor.internal.Utils.getSimpleName;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author jianglin
 * @date 2018-11-29
 */
public class SpringClientProcessor implements ClientProcessor {

    private String restTemplateName;
    private String baseUrl;
    private String basePath;
    private String serviceName;
    private Types typeUtils;
    private Elements elementUtils;
    private Messager messager;
    private String datePattern;
    private Set<FieldSpec> staticFields = new HashSet<>();

    public SpringClientProcessor(String restTemplateName,
                                 String serviceName,
                                 Types typeUtils,
                                 Elements elementUtils,
                                 Messager messager, String datePattern) {
        this.restTemplateName = restTemplateName;
        this.serviceName = serviceName;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.datePattern = datePattern;
    }

    @Override
    public TypeSpec.Builder processType(TypeElement typeElement) {
        RequestMapping requestMapping = typeElement.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            this.basePath = getPath(defaultIfBlank(requestMapping.value().length > 0
                            ? requestMapping.value()[0] : null,
                    requestMapping.name()));
        } else {
            this.basePath = "";
        }
        this.baseUrl = "http://" + serviceName;

        String name = typeElement.getAnnotation(GoudaiClient.class).value();
        String className = typeElement.getSimpleName().toString() + "Connector";

        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addSuperinterface(TypeName.get(typeElement.asType()))
                .addModifiers(new Modifier[]{Modifier.PUBLIC})
//                .addAnnotation(AnnotationSpec.builder(ClassName.get(CircuitBreaker.class))
//                        .addMember("name", "$S", name)
//                        .build())

                .addAnnotation(AnnotationSpec.builder(ClassName.get(Service.class))
                        .addMember("value", "$S",
                                className.substring(0, 1).toLowerCase() + className.substring(1))
                        .build())
                .addField(FieldSpec.builder(RestTemplate.class, restTemplateName, Modifier.PRIVATE)
//                            .addAnnotation(AnnotationSpec.builder(Autowired.class).build())
//                            .addAnnotation(AnnotationSpec.builder(LoadBalanced.class).build())
                        .build())
                .addField(FieldSpec.builder(String.class, "baseUrl", Modifier.PRIVATE)
                        .addAnnotation(AnnotationSpec.builder(Value.class)
                                .addMember("value", "$S",
                                        "${" + name + ".baseUrl:" + this.baseUrl + "}" + this.basePath)
                                .build())
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(AnnotationSpec.builder(Autowired.class).build())
                        .addParameter(ParameterSpec.builder(RestTemplate.class, restTemplateName).build())
                        .addStatement("$T.notNull($L, $S);",
                                Assert.class, restTemplateName, restTemplateName + " must not be null!")
                        .addStatement("this.$L = $L", restTemplateName, restTemplateName)
                        .build());
        if (isNotBlank(this.datePattern)) {
            builder.addField(FieldSpec.builder(String.class, "DATE_FORMAT_PATTERN")
                    .initializer("$S", this.datePattern)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .build());

        }
        return builder;
    }

    @Override
    public CodeBlock processMethod(Method method) {
        CodeBlock.Builder builder = CodeBlock.builder();
        boolean hasNamedUriVariables = false;
        boolean hasIndexedUriVariables = false;
        // uri
        builder.addStatement("$T builder = $T.fromUriString($L + $S)",
                UriComponentsBuilder.class,
                UriComponentsBuilder.class,
                "this.baseUrl", method.getPath());
        List<Parameter> queryParams = method.getQueryParams();
        if (queryParams.size() > 0) {
            builder.addStatement("$T p = new $T<>()",
                    ParameterizedTypeName.get(MultiValueMap.class, String.class, String.class),
                    TypeName.get(LinkedMultiValueMap.class)
            );
        }
        for (Parameter queryParam : queryParams) {
            if (queryParam.isMap()) {
                builder.addStatement("if($L != null) $L.forEach((k, v) -> p.add(k, $T.valueOf(v)))",
                        queryParam.getName(),
                        queryParam.getName(),
                        TypeName.get(String.class));
            } else if (queryParam.isArray()) {
                builder.addStatement("if($L != null) $T.stream($L).forEach(s -> p.add($S, $T.valueOf(s)))",
                        queryParam.getName(),
                        Arrays.class,
                        queryParam.getName(),
                        queryParam.getName(),
                        TypeName.get(String.class));
            } else if (queryParam.isCollection() || queryParam.isIterable()) {
                builder.addStatement("if($L != null) $L.forEach(e -> p.add($S, $T.valueOf(e)))",
                        queryParam.getName(),
                        queryParam.getName(),
                        queryParam.getName(),
                        TypeName.get(String.class));
            } else {
                queryParam.getProperties().forEach(p -> {
                            CodeBlock.Builder builder1 = CodeBlock.builder()
                                    .add("if($L)\n", p.getIfNotNullStatement());
                            if (p.getArray()) {
                                builder1.addStatement("\t\t$T.stream($L).forEach(s -> p.add($S, $T.valueOf(s)))",
                                        Arrays.class,
                                        p.getReadAccessor(),
                                        p.getName(),
                                        TypeName.get(String.class)
                                );
                            } else if (p.getMap()) {
                                builder1.addStatement("\t\t$L.forEach((k, v) -> p.add($S + k, $T.valueOf(v)))",
                                        p.getReadAccessor(),
                                        p.getName() + ".",
                                        TypeName.get(String.class)
                                );
                            } else if (p.getIterable() || p.getCollection()) {
                                builder1.addStatement("\t\t$L.forEach(e -> p.add($S, $T.valueOf(e)))",
                                        p.getReadAccessor(),
                                        p.getName(),
                                        TypeName.get(String.class)
                                );
                            } else if (p.getDate() && isNotBlank(this.datePattern)) {
                                staticFields.add(FieldSpec.builder(DateTimeFormatter.class, "dateTimeFormatter")
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                        .initializer("$T.ofPattern(DATE_FORMAT_PATTERN)\n.withZone($T.systemDefault())", DateTimeFormatter.class, ZoneId.class)
                                        .build());
                                builder1.addStatement("\t\tp.add($S, dateTimeFormatter.format($L.toInstant()))",
                                        p.getName(),
                                        p.getReadAccessor()
                                );
                            } else if (p.getEnum()) {
                                builder1.addStatement("\t\tp.add($S, $L.name())",
                                        p.getName(),
                                        p.getReadAccessor()
                                );
                            } else {
                                builder1.addStatement("\t\tp.add($S, $T.valueOf($L))",
                                        p.getName(),
                                        TypeName.get(String.class),
                                        p.getReadAccessor()
                                );
                            }
                            builder.add(builder1.build());
                        }
                );
            }
        }

        if (queryParams.size() > 0) {
            builder.addStatement("builder.queryParams(p)");
        }

        CodeBlock.Builder namedUriVariables = CodeBlock.builder();
        CodeBlock.Builder indexedUriVariables = CodeBlock.builder();
        List<Parameter> uriVariables = method.getUriVariables();
        for (Parameter uriVariable : uriVariables) {
            if (StringUtils.isNotBlank(uriVariable.getUriVariableName())) {
                namedUriVariables.addStatement("uriVariables.put($S, $L)",
                        uriVariable.getUriVariableName(),
                        uriVariable.getName());
                hasNamedUriVariables = true;
            } else {
                indexedUriVariables.addStatement("indexUriVariables.add($L, $L)",
                        uriVariable.getIndex(),
                        uriVariable.getName());
                hasIndexedUriVariables = true;
            }
        }
        if (hasIndexedUriVariables) {
            builder.addStatement("$T<$T> indexUriVariables = new $T<>()",
                    List.class,
                    Object.class,
                    LinkedList.class);
            builder.add(indexedUriVariables.build());
        }
        if (hasNamedUriVariables) {
            builder.addStatement("$T<$T, $T> uriVariables = new $T<>()",
                    Map.class,
                    String.class,
                    Object.class,
                    HashMap.class);
            builder.add(namedUriVariables.build());
        }

        builder.addStatement("$T headers = new $T()",
                HttpHeaders.class,
                HttpHeaders.class);
        for (Parameter header : method.getHeaders()) {
            builder.addStatement("headers.add($S, $L)", header.getHeaderName(), header.getName());
        }
        Parameter body = method.getBody();
        if (body == null) {
            builder.addStatement("$T<Object> httpEntity = new $T<>(null, $L)",
                    HttpEntity.class,
                    HttpEntity.class,
                    "headers");
        } else {
            builder.addStatement("$T<$T> httpEntity = new $T<>($L, $L)",
                    HttpEntity.class,
                    ParameterizedTypeName.get(body.getVariableElement().asType()),
                    HttpEntity.class, body.getName(), "headers");
        }

        builder.add("$T uri = builder\n", URI.class);

        if (hasIndexedUriVariables) {
            builder.add("\t\t.buildAndExpand($L.toArray())\n", "indexUriVariables");
        } else {
            builder.add("\t\t.build()\n");
        }
        if (hasNamedUriVariables) {
            builder.add("\t\t.expand($L)\n", "uriVariables");
        }
        builder.addStatement("\t\t.toUri()");

        // ParameterizedTypeReference 可重用
        TypeName responseType = TypeName.get(method.getElement().getReturnType());
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(ClassName.get(ParameterizedTypeReference.class),
                        responseType.box());
        String simpleName = getSimpleName(responseType);
        FieldSpec fieldSpec = FieldSpec.builder(parameterizedTypeName, simpleName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("\n\t\tnew $T(){}", parameterizedTypeName)
                .build();

        if (method.isReturnVoid()) {
            builder.addStatement("$L.exchange(\n" +
                            "\t\turi,\n" +
                            "\t\t$T.$L,\n" +
                            "\t\thttpEntity,\n" +
                            "\t\t$L\n" +
                            ").getBody()",
                    this.restTemplateName,
                    HttpMethod.class,
                    method.getMethod(),
                    simpleName
            );
        } else {
            builder.addStatement("return $L.exchange(\n" +
                            "\t\turi,\n" +
                            "\t\t$T.$L,\n" +
                            "\t\thttpEntity,\n" +
                            "\t\t$L\n" +
                            ").getBody()",
                    this.restTemplateName,
                    HttpMethod.class,
                    method.getMethod(),
                    simpleName);
        }

        staticFields.add(fieldSpec);

        return builder.build();
    }

    public void afterProcess(TypeSpec.Builder builder) {

        builder.addFields(staticFields);
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

