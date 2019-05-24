package cloud.goudai.httpclient.processor.internal;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

import static cloud.goudai.httpclient.processor.internal.Utils.decapitalize;
import static cloud.goudai.httpclient.processor.internal.Utils.getQualifiedName;

/**
 * @author jianglin
 * @date 2018-12-01
 */
public class TypeHelper {
    private final Elements elementUtils;
    private final Types typeUtils;
    private final TypeMirror iterableType;
    private final TypeMirror collectionType;
    private final TypeMirror mapType;

    public TypeHelper(Elements elementUtils, Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        iterableType = typeUtils.erasure(elementUtils.getTypeElement(Iterable.class.getCanonicalName()).asType());
        collectionType =
                typeUtils.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType());
        mapType = typeUtils.erasure(elementUtils.getTypeElement(Map.class.getCanonicalName()).asType());
    }

    public boolean isIterableType(TypeMirror typeMirror) {

        return typeUtils.isSubtype(typeMirror, iterableType);
    }

    public boolean isCollectionType(TypeMirror typeMirror) {
        return typeUtils.isSubtype(typeMirror, collectionType);
    }

    public boolean isMapType(TypeMirror typeMirror) {
        return typeUtils.isSubtype(typeMirror, mapType);
    }

    public boolean isArray(TypeMirror typeMirror) {

        return typeMirror.getKind() == TypeKind.ARRAY;
    }

    public boolean isDate(TypeMirror typeMirror) {

        return TypeName.get(typeMirror).equals(TypeName.get(Date.class));
    }

    public boolean isEnum(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            if (declaredType.asElement().getKind() == ElementKind.ENUM) {
                return true;
            }
        }
        return false;
    }

    public Set<Property> getProperties(String parent, String reader, String prefix, String paramName, TypeMirror typeMirror) {
        Set<Property> properties = new HashSet<>();
        TypeName typeName = TypeName.get(typeMirror);
        if (typeName.isPrimitive()
                || typeName.isBoxedPrimitive()
                || typeName.equals(TypeName.get(String.class))) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).build());
            return properties;
        }

        if (isDate(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isDate(true).build());
            return properties;
        }

        if (isArray(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isArray(true).build());
            return properties;
        }

        if (isMapType(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isMap(true).build());
            return properties;
        }


        if (isCollectionType(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isCollection(true).build());
            return properties;
        }


        if (isIterableType(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isIterable(true).build());
            return properties;
        }

        if (isEnum(typeMirror)) {
            properties.add(Property.newBuilder().name(paramName).parent(parent).reader(reader).isEnum(true).build());
            return properties;
        }

        for (Element member : typeUtils.asElement(typeMirror)
                .getEnclosedElements()) {
            if (member instanceof ExecutableElement) {
                ExecutableElement executableElement = (ExecutableElement) member;
                TypeMirror returnType = executableElement.getReturnType();
                if (!isGetter(executableElement)) {
                    continue;
                }
                String name = getPropertyName(executableElement);
                String curParent = parent == null ? reader : String.join(".", parent, reader);
                String curName = prefix == null ? name : String.join(".", prefix, name);
                properties.addAll(getProperties(curParent, executableElement.toString(), curName, curName, returnType));
            }
        }
        return properties;
    }

    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        String methodName = getterOrSetterMethod.getSimpleName().toString();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return decapitalize(methodName.substring(3));
        }
        return decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));
    }

    private Boolean isGetter(ExecutableElement executable) {

        return executable != null && executable.getModifiers().contains(Modifier.PUBLIC) &&
                executable.getParameters().isEmpty() &&
                isGetterMethod(executable);

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

}
