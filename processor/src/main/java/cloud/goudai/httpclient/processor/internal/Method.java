package cloud.goudai.httpclient.processor.internal;

import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author jianglin
 * @date 2018-11-29
 */
public class Method {
    private String name;
    private ExecutableElement element;
    private List<Parameter> parameters;
    private Types typeUtils;
    private Elements elementUtils;
    private Messager messager;
    private String path;
    private String method;

    private Method(Builder builder) {
        setName(builder.name);
        setElement(builder.element);
        setParameters(builder.parameters);
        setTypeUtils(builder.typeUtils);
        setElementUtils(builder.elementUtils);
        setMessager(builder.messager);
        parseAnno();
    }

    private void parseAnno() {
        RequestMapping requestMapping = element.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            this.path = defaultIfBlank(getOne(requestMapping.value()), getOne(requestMapping.path()));
            RequestMethod[] methods = requestMapping.method();
            this.method = methods.length > 0 ? methods[0].name() : RequestMethod.GET.name();
            return;
        }
        GetMapping getMapping = element.getAnnotation(GetMapping.class);
        if (getMapping != null) {
            this.path = defaultIfBlank(getOne(getMapping.value()), getOne(getMapping.path()));
            this.method = RequestMethod.GET.name();
            return;
        }
        PostMapping postMapping = element.getAnnotation(PostMapping.class);
        if (postMapping != null) {
            this.path = defaultIfBlank(getOne(postMapping.value()), getOne(postMapping.path()));
            this.method = RequestMethod.POST.name();
            return;
        }
        PutMapping putMapping = element.getAnnotation(PutMapping.class);
        if (putMapping != null) {
            this.path = defaultIfBlank(getOne(putMapping.value()), getOne(putMapping.path()));
            this.method = RequestMethod.PUT.name();
            return;
        }
        DeleteMapping deleteMapping = element.getAnnotation(DeleteMapping.class);
        if (deleteMapping != null) {
            this.path = defaultIfBlank(getOne(deleteMapping.value()), getOne(deleteMapping.path()));
            this.method = RequestMethod.DELETE.name();
            return;
        }
        PatchMapping patchMapping = element.getAnnotation(PatchMapping.class);
        if (patchMapping != null) {
            this.path = defaultIfBlank(getOne(patchMapping.value()), getOne(patchMapping.path()));
            this.method = RequestMethod.PATCH.name();
            return;
        }
    }

    public String getOne(String[] values) {

        return Arrays.stream(values)
                .filter(StringUtils::isNotBlank)
                .findFirst().orElse(null);
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public String getPath() {

        return Utils.getPath(this.path);
    }

    public String getMethod() {

        return this.method;
    }

    public Boolean isReturnVoid() {
        return TypeName.get(element.getReturnType()) == TypeName.VOID;
    }

    public List<Parameter> getHeaders() {
        return this.parameters.stream()
                .filter(Parameter::isHeader)
                .collect(Collectors.toList());

    }

    public Parameter getBody() {
        for (Parameter parameter : this.parameters) {
            if (parameter.isBody()) {
                return parameter;
            }
        }

        return null;
    }

    public List<Parameter> getUriVariables() {
        return this.parameters.stream()
                .filter(Parameter::isUriVariable)
                .collect(Collectors.toList());

    }

    public List<Parameter> getQueryParams() {
        return this.parameters.stream()
                .filter(Parameter::isQueryParams)
                .collect(Collectors.toList());

    }

    public String getName() {
        return name;
    }

    public Method setName(String name) {
        this.name = name;
        return this;
    }

    public ExecutableElement getElement() {
        return element;
    }

    public Method setElement(ExecutableElement element) {
        this.element = element;
        return this;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Method setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }


    public Types getTypeUtils() {
        return typeUtils;
    }

    public Method setTypeUtils(Types typeUtils) {
        this.typeUtils = typeUtils;
        return this;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Method setElementUtils(Elements elementUtils) {
        this.elementUtils = elementUtils;
        return this;
    }

    public Messager getMessager() {
        return messager;
    }

    public Method setMessager(Messager messager) {
        this.messager = messager;
        return this;
    }


    public static final class Builder {
        private String name;
        private ExecutableElement element;
        private List<Parameter> parameters;
        private Types typeUtils;
        private Elements elementUtils;
        private Messager messager;

        private Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder element(ExecutableElement val) {
            element = val;
            return this;
        }

        public Builder parameters(List<Parameter> val) {
            parameters = val;
            return this;
        }

        public Builder typeUtils(Types val) {
            typeUtils = val;
            return this;
        }

        public Builder elementUtils(Elements val) {
            elementUtils = val;
            return this;
        }

        public Builder messager(Messager val) {
            messager = val;
            return this;
        }

        public Method build() {
            return new Method(this);
        }
    }
}
