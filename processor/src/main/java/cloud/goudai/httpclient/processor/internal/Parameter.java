package cloud.goudai.httpclient.processor.internal;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.processing.Messager;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * @author jianglin
 * @date 2018-11-29
 */
public class Parameter {

    private Integer index;
    private String name;
    private VariableElement variableElement;
    private Types typeUtils;
    private Elements elementUtils;
    private Messager messager;
    private TypeHelper typeHelper;

    private Parameter(Builder builder) {
        setIndex(builder.index);
        setName(builder.name);
        setVariableElement(builder.variableElement);
        typeUtils = builder.typeUtils;
        elementUtils = builder.elementUtils;
        messager = builder.messager;
        typeHelper = new TypeHelper(this.elementUtils, this.typeUtils);
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public boolean isHeader() {

        return variableElement.getAnnotation(RequestHeader.class) != null;
    }

    public String getHeaderName() {
        RequestHeader requestHeader = variableElement.getAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return defaultIfBlank(defaultIfBlank(requestHeader.value(), requestHeader.name()), name);
        }
        return null;
    }

    public boolean isBody() {

        return variableElement.getAnnotation(RequestBody.class) != null;
    }

    public boolean isUriVariable() {

        return variableElement.getAnnotation(PathVariable.class) != null;
    }

    public String getUriVariableName() {
        PathVariable variable = variableElement.getAnnotation(PathVariable.class);

        return defaultIfBlank(variable.value(), variable.name());
    }

    public boolean isQueryParams() {

        return (!isHeader() && !isBody() && !isUriVariable()) || variableElement.getAnnotation(RequestParam.class) != null;
    }

    public boolean isMap() {
        return typeHelper.isMapType(variableElement.asType());
    }

    public boolean isCollection() {
        return typeHelper.isCollectionType(variableElement.asType());
    }

    public boolean isIterable() {
        return typeHelper.isIterableType(variableElement.asType());
    }

    public boolean isArray() {
        return typeHelper.isArray(variableElement.asType());
    }

    public boolean isDate() {
        return typeHelper.isArray(variableElement.asType());
    }

    public Set<Property> getProperties() {

        TypeMirror typeMirror = variableElement.asType();

        return typeHelper.getProperties(null, this.name, null, getParamName(variableElement), typeMirror);
    }

    private String getParamName(VariableElement variableElement) {
        RequestParam requestParam = variableElement.getAnnotation(RequestParam.class);
        String name = null;
        if (requestParam != null) {
            name = defaultIfBlank(requestParam.value(), requestParam.name());
        }
        if (name != null) return name;

        return this.name;
    }

    public Integer getIndex() {
        return index;
    }

    public Parameter setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }

    public Parameter setName(String name) {
        this.name = name;
        return this;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    public Parameter setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
        return this;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Parameter setTypeUtils(Types typeUtils) {
        this.typeUtils = typeUtils;
        return this;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Parameter setElementUtils(Elements elementUtils) {
        this.elementUtils = elementUtils;
        return this;
    }

    public Messager getMessager() {
        return messager;
    }

    public Parameter setMessager(Messager messager) {
        this.messager = messager;
        return this;
    }


    public static final class Builder {
        private Integer index;
        private String name;
        private VariableElement variableElement;
        private Types typeUtils;
        private Elements elementUtils;
        private Messager messager;

        private Builder() {
        }

        public Builder index(Integer val) {
            index = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder variableElement(VariableElement val) {
            variableElement = val;
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

        public Parameter build() {
            return new Parameter(this);
        }
    }
}
