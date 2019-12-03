package cloud.goudai.httpclient.processor.internal.model.rest;

import cloud.goudai.httpclient.common.GoudaiClient;
import cloud.goudai.httpclient.processor.internal.model.common.Field;
import cloud.goudai.httpclient.processor.internal.model.common.Parameter;
import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.model.common.TypeFactory;
import cloud.goudai.httpclient.processor.internal.utils.AccessorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static cloud.goudai.httpclient.processor.internal.utils.Utils.getPath;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class ClientFactory {

    private final Elements elementUtils;
    private final Types typeUtils;
    private final TypeFactory typeFactory;

    public ClientFactory(Elements elementUtils, Types typeUtils, TypeFactory typeFactory) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.typeFactory = typeFactory;
    }

    public Client getClient(Type type) {

        GoudaiClient goudaiClient = type.getTypeElement().getAnnotation(GoudaiClient.class);
        if (goudaiClient == null) {
            return null;
        }
        String basePath = "";
        RequestMapping requestMapping = type.getTypeElement().getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            if (requestMapping.value().length > 0) {
                basePath = requestMapping.value()[0];
            }
            if (requestMapping.path().length > 0) {
                basePath = defaultIfBlank(basePath, requestMapping.path()[0]);
            }
            basePath = getPath(defaultIfBlank(basePath, ""));
        }

        Set<Request> requests = new HashSet<>();
        for (ExecutableElement method : type.getAllMethods()) {
            requests.add(getRequest(type.getTypeElement(), method));
        }
        return Client.newBuilder()
                .name(type.getName())
                .serviceName(goudaiClient.value())
                .requests(requests)
                .basePath(basePath)
                .type(type)
                .baseUrl("http://" + goudaiClient.value())
                .build();
    }

    public Request getRequest(TypeElement type, ExecutableElement executableElement) {
        ExecutableType methodType = typeFactory.getMethodType((DeclaredType) type.asType(), executableElement);
        Type returnType = typeFactory.getReturnType(methodType);
        Request.Builder builder = Request.newBuilder()
                .name(executableElement.getSimpleName().toString())
                .executableElement(executableElement)
                .responseType(returnType);

        // @RequestMapping
        parseMethod(builder, executableElement);

        List<Parameter> parameters = typeFactory.getParameters(methodType, executableElement);
        parseParameters(builder, parameters);
        return builder
                .build();
    }

    private void parseMethod(Request.Builder builder, ExecutableElement element) {
        RequestMapping requestMapping = element.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            org.springframework.web.bind.annotation.RequestMethod[] methods = requestMapping.method();
            builder.path(getPath(defaultIfBlank(getOne(requestMapping.value()), getOne(requestMapping.path()))));
            RequestMethod method = RequestMethod.resolve(methods.length > 0 ? methods[0].name() :
                    RequestMethod.GET.name());
            builder.method(method);
            return;
        }
        GetMapping getMapping = element.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            builder.path(getPath(defaultIfBlank(getOne(getMapping.value()), getOne(getMapping.path()))));
            builder.method(RequestMethod.GET);
            return;
        }
        PostMapping postMapping = element.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            builder.path(getPath(defaultIfBlank(getOne(postMapping.value()), getOne(postMapping.path()))));
            builder.method(RequestMethod.POST);
            return;
        }
        PutMapping putMapping = element.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            builder.path(getPath(defaultIfBlank(getOne(putMapping.value()), getOne(putMapping.path()))));
            builder.method(RequestMethod.PUT);
            return;
        }
        DeleteMapping deleteMapping = element.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            builder.path(getPath(defaultIfBlank(getOne(deleteMapping.value()), getOne(deleteMapping.path()))));
            builder.method(RequestMethod.DELETE);
            return;
        }
        PatchMapping patchMapping = element.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            builder.path(getPath(defaultIfBlank(getOne(patchMapping.value()), getOne(patchMapping.path()))));
            builder.method(RequestMethod.PATCH);
            return;
        }
    }

    private void parseParameters(Request.Builder builder, List<Parameter> parameters) {
        Map<String, Param> namedPathVariable = new HashMap<>();
        Map<Integer, Param> indexedPathVariable = new HashMap<>();
        Map<String, Param> headers = new HashMap<>();
        Map<String, Param> queryParams = new HashMap<>();
        for (Parameter parameter : parameters) {
            boolean flag = true;
            VariableElement typeElement = parameter.getVariableElement();
            String parameterName = parameter.getName();
            Type parameterType = parameter.getType();
            if (parameter.isVarArgs()) {
                // TODO
                continue;
            }
            RequestBody requestBody = typeElement.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                builder.body(new Param(parameterName, parameterType, parameterName));
                flag = false;
                continue;
            }
            RequestHeader requestHeader = typeElement.getAnnotation(RequestHeader.class);
            if (requestHeader != null) {
                headers.put(defaultIfBlank(defaultIfBlank(requestHeader.value(), requestHeader.name()),
                        parameterName), new Param(parameterName, parameterType, parameterName));
                flag = false;
            }
            CookieValue cookieValue = typeElement.getAnnotation(CookieValue.class);
            if (cookieValue != null) {
                headers.put("Cookie", new Param(parameterName, parameterType, parameterName));
                flag = false;
            }
            RequestPart requestPart = typeElement.getAnnotation(RequestPart.class);
            if (requestPart != null) {
                // TODO
                flag = false;
            }
            PathVariable pathVariable = typeElement.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String name = defaultIfBlank(pathVariable.value(), pathVariable.name());
                if (StringUtils.isBlank(name)) {
                    indexedPathVariable.put(parameter.getIndex(), new Param(parameterName, parameterType,
                            parameterName));
                } else {
                    namedPathVariable.put(name, new Param(parameterName, parameterType, parameterName));
                }
                flag = false;
            }
            RequestParam requestParam = typeElement.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String name = defaultIfBlank(defaultIfBlank(requestParam.value(), requestParam.name()),
                        parameterName);
                queryParams.put(name, new Param(parameterName, parameterType, parameterName));
                flag = false;
            }
            if (flag) {
                if (parameterType.isSimpleValueType()) {
                    queryParams.put(parameterName, new Param(parameterName, parameterType, parameterName));
                } else {
                    List<Field> fields = typeFactory.getFields(parameterType);
                    for (Field field : fields) {
                        queryParams.put(field.getName(), new Param(parameterName, field.getType(),
                                AccessorUtils.getAccessor(field).replaceAll("<SOURCE>", parameterName)));
                        log(field.getName() + " " + AccessorUtils.getAccessor(field));
                    }
                }
            }
        }
        builder.headers(headers)
                .queryParams(queryParams)
                .indexedPathVariable(indexedPathVariable)
                .namedPathVariable(namedPathVariable);
    }

    private void log(String message) {
        //        typeFactory.getMessager().printMessage(Diagnostic.Kind.NOTE,
        //                message);
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    private String getOne(String[] values) {

        return Arrays.stream(values)
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse(null);
    }
}
