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

        // uri
        builder.addStatement("$T builder = $T.fromUriString($S);", UriComponentsBuilder.class, UriComponentsBuilder.class, baseUrl + method.getPath());

        // TODO queryParams
        method.getQueryParams().stream()
                .flatMap(qp -> qp.getProperties().stream())
                .forEach(p -> builder.addStatement("builder.queryParam($S,$L);", p.getName(), p.getReader()));

        builder.addStatement("$T<$T, $T> uriVariables = new $T<>();", Map.class, String.class, Object.class, HashMap.class);
        builder.addStatement("$T<$T> indexUriVariables = new $T<>();", List.class, Object.class, LinkedList.class);
        List<Parameter> uriVariables = method.getUriVariables();
        for (Parameter uriVariable : uriVariables) {
            if (StringUtils.isNotBlank(uriVariable.getUriVariableName())) {
                builder.addStatement("uriVariables.put($S, $L);", uriVariable.getUriVariableName(), uriVariable.getName());
            } else {
                builder.addStatement("indexUriVariables.add($L, $L);", uriVariable.getIndex(), uriVariable.getName());
            }
        }
        builder.addStatement("$T headers = new $T();", HttpHeaders.class, HttpHeaders.class);
        for (Parameter header : method.getHeaders()) {
            builder.addStatement("headers.add(%S, $L);", header.getHeaderName(), header.getName());
        }
        Parameter body = method.getBody();
        if (body == null) {
            builder.addStatement("$T httpEntity = new $T(null, $L);", HttpEntity.class, HttpEntity.class, "headers");
        } else {
            builder.addStatement("$T httpEntity = new $T($L, $L);", HttpEntity.class, HttpEntity.class, body.getName(), "headers");
        }

        builder.addStatement("$T uri = builder.uriVariables($L)\n" +
                        "       .buildAndExpand($L.toArray())\n" +
                        "       .toUri();",
                URI.class, "uriVariables", "indexUriVariables");
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

}

