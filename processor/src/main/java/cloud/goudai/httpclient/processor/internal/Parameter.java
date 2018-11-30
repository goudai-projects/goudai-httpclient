package cloud.goudai.httpclient.processor.internal;

import com.squareup.javapoet.TypeName;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static cloud.goudai.httpclient.processor.internal.Utils.decapitalize;
import static cloud.goudai.httpclient.processor.internal.Utils.getQualifiedName;
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

    private Parameter(Builder builder) {
        setIndex(builder.index);
        setName(builder.name);
        setVariableElement(builder.variableElement);
        typeUtils = builder.typeUtils;
        elementUtils = builder.elementUtils;
        messager = builder.messager;
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

    public List<Property> getProperties() {
        List<Property> properties = new ArrayList<>();
        TypeName typeName = TypeName.get(variableElement.asType());
        if (typeName.isPrimitive() || typeName.isBoxedPrimitive() || typeName.equals(TypeName.get(String.class))) {
            properties.add(Property.newBuilder().name(getPropertyName(variableElement)).reader(this.name).build());
            return properties;
        }
        for (Element member : typeUtils.asElement(variableElement.asType())
                .getEnclosedElements()) {
            if (member instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) member;
                if (isGetter(executableElement)) {
                    properties.add(Property.newBuilder()
                            .name(getPropertyName(executableElement))
                            .reader(this.name + "." + member.getSimpleName().toString() + "()")
                            .build());
                }
            }
        }

        return properties;
    }

    private Boolean isGetter(ExecutableElement executable) {

        return executable != null && executable.getModifiers().contains(Modifier.PUBLIC) &&
                executable.getParameters().isEmpty() &&
                isGetterMethod(executable);

    }

    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        String methodName = getterOrSetterMethod.getSimpleName().toString();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return decapitalize(methodName.substring(3));
        }
        return decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));
    }


    public boolean isGetterMethod(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();

        boolean isNonBooleanGetterName = methodName.startsWith("get") && methodName.length() > 3 &&
                method.getReturnType().getKind() != TypeKind.VOID;

        boolean isBooleanGetterName = methodName.startsWith("is") && methodName.length() > 2;
        boolean returnTypeIsBoolean = method.getReturnType().getKind() == TypeKind.BOOLEAN ||
                "java.lang.Boolean".equals(getQualifiedName(method.getReturnType()));

        return isNonBooleanGetterName || (isBooleanGetterName && returnTypeIsBoolean);
    }


    private String getPropertyName(VariableElement variableElement) {
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

    public static class Property {
        private String name;
        private String reader;

        private Property(Builder builder) {
            setName(builder.name);
            setReader(builder.reader);
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public String getName() {
            return name;
        }

        public Property setName(String name) {
            this.name = name;
            return this;
        }

        public String getReader() {
            return reader;
        }

        public Property setReader(String reader) {
            this.reader = reader;
            return this;
        }


        public static final class Builder {
            private String name;
            private String reader;

            private Builder() {
            }

            public Builder name(String val) {
                name = val;
                return this;
            }

            public Builder reader(String val) {
                reader = val;
                return this;
            }

            public Property build() {
                return new Property(this);
            }
        }
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
