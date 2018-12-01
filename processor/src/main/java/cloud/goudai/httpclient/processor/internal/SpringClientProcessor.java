package cloud.goudai.httpclient.processor.internal;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import javax.lang.model.element.TypeElement;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static cloud.goudai.httpclient.processor.internal.Utils.getPath;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author jianglin
 * @date 2018-11-29
 */
public class SpringClientProcessor implements ClientProcessor {

    private String restTemplateName;
    private String baseUrl;
    private String serviceName;

    public SpringClientProcessor(String restTemplateName,
                                 String serviceName) {
        this.restTemplateName = restTemplateName;
        this.serviceName = serviceName;
    }

    @Override
    public void processType(TypeElement typeElement) {
        String path;
        RequestMapping requestMapping = typeElement.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            path = getPath(this.serviceName + defaultIfBlank(requestMapping.value().length > 0
                            ? requestMapping.value()[0] : null,
                    requestMapping.name()));
        } else {
            path = "";
        }
        this.baseUrl = "http://" + serviceName + path;
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

        for (Parameter queryParam : method.getQueryParams()) {
            if (queryParam.isMap()) {
                builder.addStatement("if($L != null) $L.forEach((k, v) -> builder.queryParam(k, v))", queryParam.getName(), queryParam.getName());
            } else if (queryParam.isArray()) {
                builder.addStatement("if($L != null) builder.queryParam($S,$L)", queryParam.getName(), queryParam.getName(), queryParam.getName());
            } else if (queryParam.isCollection() || queryParam.isIterable()) {
                builder.addStatement("if($L != null) $L.forEach(e -> builder.queryParam($S, e))", queryParam.getName(), queryParam.getName(), queryParam.getName());
            } else {
                queryParam.getProperties().forEach(p ->
                        builder.addStatement("if($L != null) builder.queryParam($S,$L)",
                                p.getReader(),
                                p.getName(),
                                p.getReader()));
            }
        }

        CodeBlock.Builder namedUriVariables = CodeBlock.builder();
        CodeBlock.Builder indexedUriVariables = CodeBlock.builder();
        List<Parameter> uriVariables = method.getUriVariables();
        for (Parameter uriVariable : uriVariables) {
            if (StringUtils.isNotBlank(uriVariable.getUriVariableName())) {
                namedUriVariables.addStatement("uriVariables.put($S, $L)", uriVariable.getUriVariableName(), uriVariable.getName());
                hasNamedUriVariables = true;
            } else {
                indexedUriVariables.addStatement("indexUriVariables.add($L, $L)", uriVariable.getIndex(), uriVariable.getName());
                hasIndexedUriVariables = true;
            }
        }
        if (hasIndexedUriVariables) {
            builder.addStatement("$T<$T> indexUriVariables = new $T<>()", List.class, Object.class, LinkedList.class);
            builder.add(indexedUriVariables.build());
        }
        if (hasNamedUriVariables) {
            builder.addStatement("$T<$T, $T> uriVariables = new $T<>()", Map.class, String.class, Object.class, HashMap.class);
            builder.add(namedUriVariables.build());
        }

        builder.addStatement("$T headers = new $T()", HttpHeaders.class, HttpHeaders.class);
        for (Parameter header : method.getHeaders()) {
            builder.addStatement("headers.add(%S, $L)", header.getHeaderName(), header.getName());
        }
        Parameter body = method.getBody();
        if (body == null) {
            builder.addStatement("$T<Object> httpEntity = new $T<>(null, $L)", HttpEntity.class, HttpEntity.class, "headers");
        } else {
            builder.addStatement("$T<$T> httpEntity = new $T<>($L, $L)", HttpEntity.class,
                    ParameterizedTypeName.get(body.getVariableElement().asType()), HttpEntity.class, body.getName(), "headers");
        }
        if (hasNamedUriVariables) {
            builder.add("$T uri = builder.uriVariables($L)\n", URI.class, "uriVariables");
        } else {
            builder.add("$T uri = builder\n", URI.class);
        }
        if (hasIndexedUriVariables) {
            builder.add("       .buildAndExpand($L.toArray())\n", "indexUriVariables");
        } else {
            builder.add("       .buildAndExpand()\n");
        }
        builder.addStatement("       .toUri()");
        if (method.isReturnVoid()) {
            builder.addStatement("$L.exchange(\n" +
                            "                   uri,\n" +
                            "                   $T.$L,\n" +
                            "                   httpEntity,\n" +
                            "                   new $T<$T>(){}\n" +
                            "                 ).getBody()",
                    this.restTemplateName,
                    HttpMethod.class,
                    method.getMethod(),
                    ParameterizedTypeReference.class,
                    ParameterizedTypeName.get(method.getElement().getReturnType()));
        } else {
            builder.addStatement("return $L.exchange(\n" +
                            "                   uri,\n" +
                            "                   $T.$L,\n" +
                            "                   httpEntity,\n" +
                            "                   new $T<$T>(){}\n" +
                            "                 ).getBody()",
                    this.restTemplateName,
                    HttpMethod.class,
                    method.getMethod(),
                    ParameterizedTypeReference.class,
                    ParameterizedTypeName.get(method.getElement().getReturnType()));
        }
        return builder.build();
    }


    public String getBaseUrl() {
        return baseUrl;
    }
}

