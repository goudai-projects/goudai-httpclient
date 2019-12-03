package cloud.goudai.httpclient.processor.internal.processor;

import cloud.goudai.httpclient.processor.internal.conversion.ToStringConversion;
import cloud.goudai.httpclient.processor.internal.conversion.ToStringProvider;
import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.model.rest.Client;
import cloud.goudai.httpclient.processor.internal.model.rest.Param;
import cloud.goudai.httpclient.processor.internal.model.rest.Request;
import com.squareup.javapoet.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.*;

/**
 * @author jianglin
 * @date 2019/12/2
 */
public class GenerateSourceProcessor implements ClientProcessor<Client, TypeSpec> {

    @Override
    public TypeSpec process(ProcessorContext context, TypeElement typeElement, Client client) {
        Set<Type> importTypes = new HashSet<>();
        TypeSpec.Builder builder = buildType(context, importTypes, client);
        client.getRequests().forEach(request -> builder.addMethod(buildMethod(context, importTypes, client, request).build()));
        writeToFile(client.getType().getPackageName(), builder.build(), importTypes, context.getFiler());
        builder.build();
        return builder.build();
    }

    private String injectImports(JavaFile javaFile, Set<Type> imports) {
        String rawSource = javaFile.toString();

        List<String> result = new ArrayList<>();
        for (String s : rawSource.split("\n", -1)) {
            result.add(s);
            if (s.startsWith("package ")) {
                result.add("");
                for (Type i : imports) {
                    String importCode = "import " + i.getImportName() + ";";
                    if (rawSource.contains(importCode)) {
                        continue;
                    }
                    result.add(importCode);
                }
            }
        }

        return String.join("\n", result);
    }

    private void writeToFile(String packageName, TypeSpec typeSpec, Set<Type> importTypes, Filer filer) {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .indent("\t")
                .build();

        String javaFileString = injectImports(javaFile, importTypes);
        try {
            writeTo(filer, packageName, typeSpec, javaFileString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTo(Filer filer, String packageName, TypeSpec typeSpec, String javaCode) throws IOException {
        String fileName = packageName.isEmpty()
                ? typeSpec.name
                : packageName + "." + typeSpec.name;
        List<Element> originatingElements = typeSpec.originatingElements;
        JavaFileObject filerSourceFile = filer.createSourceFile(fileName,
                originatingElements.toArray(new Element[originatingElements.size()]));
        try (Writer writer = filerSourceFile.openWriter()) {
            writer.append(javaCode);
        } catch (Exception e) {
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    private TypeSpec.Builder buildType(ProcessorContext context, Set<Type> importTypes, Client client) {
        return TypeSpec.classBuilder(client.getName() + "Connector")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addSuperinterface(TypeName.get(client.getType().getTypeMirror()))
                .addField(FieldSpec.builder(RestTemplate.class, "restTemplate", Modifier.PRIVATE)
                        .build())
                .addField(FieldSpec.builder(String.class, "baseUrl", Modifier.PRIVATE)
                        .addAnnotation(AnnotationSpec.builder(Value.class)
                                .addMember("value", "$S",
                                        "${" + client.getServiceName() + ".baseUrl:" + client.getBaseUrl() + "}" + client.getBasePath())
                                .build())
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(RestTemplate.class, "restTemplate").build())
                        .addStatement("$T.notNull($L, $S);",
                                Assert.class, "restTemplate", "restTemplate must not be null!")
                        .addStatement("this.$L = $L", "restTemplate", "restTemplate")
                        .build());
    }

    private MethodSpec.Builder buildMethod(ProcessorContext context, Set<Type> importTypes, Client client,
                                           Request request) {
        MethodSpec.Builder builder = MethodSpec.overriding(request.getExecutableElement());
        builder.addCode(buildMethodBody(context, importTypes, client, request).build());
        return builder;
    }

    private CodeBlock.Builder buildMethodBody(ProcessorContext context, Set<Type> importTypes, Client client,
                                              Request request) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("$T uriBuilder = $T.fromUriString(this.baseUrl + $S)", UriComponentsBuilder.class,
                UriComponentsBuilder.class,
                request.getPath());
        processQueryParams(context, importTypes, builder, request);
        processPathVariable(context, importTypes, builder, request);
        processHeader(context, importTypes, builder, request);
        processRequestBody(context, importTypes, builder, request);
        processRequest(context, importTypes, builder, request);
        return builder;
    }

    private void processPathVariable(ProcessorContext context, Set<Type> importTypes, CodeBlock.Builder builder,
                                     Request request) {
        if (!request.getNamedPathVariable().isEmpty()) {
            builder.addStatement("$T<$T, $T> uriVariables = new $T<>()",
                    Map.class,
                    String.class,
                    Object.class,
                    HashMap.class);
            for (Map.Entry<String, Param> entry : request.getNamedPathVariable().entrySet()) {
                Param param = entry.getValue();
                Type type = param.getType();
                ToStringProvider toStringProvider = context.getConversions().getToStringProvider(type);
                ToStringConversion toStringConversion = toStringProvider.toString(context.getConversionContext());
                importTypes.addAll(toStringConversion.getImportTypes());
                builder.addStatement("uriVariables.put($S, " + toStringConversion.getOpenExpression() + param.getReader() + toStringConversion.getCloseExpression() + ")", entry.getKey());
            }

            builder.addStatement("uriBuilder.uriVariables($L)", "uriVariables");
        }

        if (request.getIndexedPathVariable().isEmpty()) {
            builder.addStatement("$T uri = uriBuilder.build().toUri()", URI.class);
        } else {
            Set<Map.Entry<Integer, Param>> entries = request.getIndexedPathVariable().entrySet();
            String[] ary = new String[entries.size()];
            for (Map.Entry<Integer, Param> entry : entries) {
                Param param = entry.getValue();
                Type type = param.getType();
                ToStringProvider toStringProvider = context.getConversions().getToStringProvider(type);
                ToStringConversion toStringConversion = toStringProvider.toString(context.getConversionContext());
                importTypes.addAll(toStringConversion.getImportTypes());
                ary[entry.getKey()] =
                        toStringConversion.getOpenExpression() + param.getReader() + toStringConversion.getCloseExpression();
            }
            builder.addStatement("$T uri = uriBuilder.build(" + StringUtils.join(ary, ", ") + ")", URI.class);
        }
    }

    private void processQueryParams(ProcessorContext context, Set<Type> importTypes, CodeBlock.Builder builder,
                                    Request request) {
        if (request.getQueryParams().isEmpty()) {
            return;
        }
        for (Map.Entry<String, Param> stringParamEntry : request.getQueryParams().entrySet()) {
            String paramName = stringParamEntry.getKey();
            Param param = stringParamEntry.getValue();
            Type type = param.getType();
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            if (StringUtils.equalsIgnoreCase(param.getName(), param.getReader())) {
                codeBuilder.beginControlFlow("if($T.nonNull(" + param.getReader() + "))", Objects.class);
            } else {
                codeBuilder.beginControlFlow("if($T.nonNull(" + param.getName() + ") && $T.nonNull(" + param.getReader() + "))", Objects.class, Objects.class);
            }
            if (type.isMapType()) {
                codeBuilder.addStatement(param.getReader() + ".forEach((k, v) -> uriBuilder.queryParam(k, v)))",
                        String.class);
            } else if (type.isArrayType()) {
                codeBuilder.addStatement("$T.stream(" + param.getReader() + ").forEach(next -> uriBuilder.queryParam" +
                                "($S, next))",
                        Arrays.class, paramName);
            } else if (type.isIterableType() || type.isCollectionType()) {
                codeBuilder.addStatement(param.getReader() + ".forEach(next -> p.add($S, next))", paramName);
            } else {
                ToStringProvider toStringProvider = context.getConversions().getToStringProvider(type);
                ToStringConversion toStringConversion = toStringProvider.toString(context.getConversionContext());
                importTypes.addAll(toStringConversion.getImportTypes());
                codeBuilder.addStatement("uriBuilder.queryParam($S, " + toStringConversion.getOpenExpression() + param.getReader() + toStringConversion.getCloseExpression() + ")", paramName);
            }
            codeBuilder.endControlFlow();
            builder.add(codeBuilder.build());
        }
    }

    private void processHeader(ProcessorContext context, Set<Type> importTypes, CodeBlock.Builder builder,
                               Request request) {
        if (request.getHeaders().isEmpty()) {
            builder.addStatement("$T httpHeaders = $T.EMPTY", HttpHeaders.class, HttpHeaders.class);
            return;
        }
        builder.addStatement("$T httpHeaders = new $T()", HttpHeaders.class, HttpHeaders.class);
        for (Map.Entry<String, Param> headerEntry : request.getHeaders().entrySet()) {
            Param param = headerEntry.getValue();
            Type type = param.getType();
            ToStringProvider toStringProvider = context.getConversions().getToStringProvider(type);
            ToStringConversion toStringConversion = toStringProvider.toString(context.getConversionContext());
            importTypes.addAll(toStringConversion.getImportTypes());
            CodeBlock.Builder codeBuilder = CodeBlock.builder();
            codeBuilder.beginControlFlow("if($T.nonNull(" + param.getReader() + "))", Objects.class);
            codeBuilder.addStatement("httpHeaders.add($S, " + toStringConversion.getOpenExpression() + param.getReader() + toStringConversion.getCloseExpression() + ")", headerEntry.getKey());
            codeBuilder.endControlFlow();
            builder.add(codeBuilder.build());
        }
    }

    private void processRequestBody(ProcessorContext context, Set<Type> importTypes, CodeBlock.Builder builder,
                                    Request request) {
        if (request.getBody() == null) {
            builder.addStatement("$T<Object> httpEntity = new $T<>(null, $L)",
                    HttpEntity.class,
                    HttpEntity.class,
                    "httpHeaders");
        } else {
            builder.addStatement("$T<$T> httpEntity = new $T<>($L, $L)",
                    HttpEntity.class,
                    ParameterizedTypeName.get(request.getBody().getType().getTypeMirror()),
                    HttpEntity.class, request.getBody().getName(), "httpHeaders");
        }
    }

    private void processRequest(ProcessorContext context, Set<Type> importTypes, CodeBlock.Builder builder,
                                Request request) {
        TypeName responseType = TypeName.get(request.getResponseType().getTypeMirror());
        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(ClassName.get(ParameterizedTypeReference.class),
                        responseType.box());
        String code = "restTemplate.exchange(\n" +
                "                           uri,\n" +
                "                           $T." + request.getMethod().name() + ",\n" +
                "                           httpEntity,\n" +
                "                           new $T(){}\n" +
                "                         ).getBody()";
        if (!request.getResponseType().isVoid()) {
            code = "return " + code;
        }

        builder.addStatement(code, HttpMethod.class, parameterizedTypeName);

    }


    @Override
    public int getPriority() {
        return 10000;
    }
}
