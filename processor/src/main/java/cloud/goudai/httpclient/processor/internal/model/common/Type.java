package cloud.goudai.httpclient.processor.internal.model.common;

import com.google.auto.common.MoreElements;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Type implements ModelElement {

    private final Types typeUtils;
    private final Elements elementUtils;
    private final TypeFactory typeFactory;

    private final TypeMirror typeMirror;
    private final TypeElement typeElement;
    private final List<Type> typeParameters;

    private final Type componentType;

    private final String packageName;
    private final String name;
    private final String qualifiedName;

    private final boolean isInterface;
    private final boolean isEnumType;
    private final boolean isIterableType;
    private final boolean isCollectionType;
    private final boolean isMapType;
    private final boolean isVoid;
    private final boolean isStream;
    private final boolean isLiteral;

    private Boolean isToBeImported;

    private List<ExecutableElement> allMethods = null;
    private List<VariableElement> allFields = null;

    private Type boundingBase = null;

    public Type(Types typeUtils, Elements elementUtils, TypeFactory typeFactory, TypeMirror typeMirror,
                TypeElement typeElement, List<Type> typeParameters, Type componentType,
                String packageName, String name, String qualifiedName, boolean isInterface, boolean isEnumType,
                boolean isIterableType, boolean isCollectionType, boolean isMapType, boolean isStream,
                Boolean isToBeImported, boolean isLiteral) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.typeFactory = typeFactory;
        this.typeMirror = typeMirror;
        this.typeElement = typeElement;
        this.typeParameters = typeParameters;
        this.componentType = componentType;
        this.packageName = packageName;
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.isInterface = isInterface;
        this.isEnumType = isEnumType;
        this.isIterableType = isIterableType;
        this.isCollectionType = isCollectionType;
        this.isMapType = isMapType;
        this.isVoid = typeMirror.getKind() == TypeKind.VOID;
        this.isStream = isStream;
        this.isToBeImported = isToBeImported;
        this.isLiteral = isLiteral;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public List<Type> getTypeParameters() {
        return typeParameters;
    }

    public Type getComponentType() {
        return componentType;
    }

    public boolean isPrimitive() {
        return typeMirror.getKind().isPrimitive();
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isEnumType() {
        return isEnumType;
    }

    public boolean isIterableType() {
        return isIterableType;
    }

    public boolean isCollectionType() {
        return isCollectionType;
    }

    public boolean isMapType() {
        return isMapType;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public boolean isStream() {
        return isStream;
    }

    public boolean isLiteral() {
        return isLiteral;
    }

    public boolean isArrayType() {
        return componentType != null;
    }

    public Boolean getToBeImported() {
        return isToBeImported;
    }

    public String getImportName() {
        return isArrayType() ? trimSimpleClassName(qualifiedName) : qualifiedName;
    }

    public boolean isStreamType() {
        return isStream;
    }

    public List<ExecutableElement> getAllMethods() {
        if (allMethods == null) {
            allMethods = MoreElements.getLocalAndInheritedMethods(typeElement, typeUtils, elementUtils).asList();
        }
        return allMethods;
    }

    public List<VariableElement> getAllFields() {
        if (allFields == null) {
            allFields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        }
        return allFields;
    }

    public boolean isWildCardSuperBound() {
        boolean result = false;
        if (typeMirror.getKind() == TypeKind.WILDCARD) {
            WildcardType wildcardType = (WildcardType) typeMirror;
            result = wildcardType.getSuperBound() != null;
        }
        return result;
    }

    public boolean isWildCardExtendsBound() {
        boolean result = false;
        if (typeMirror.getKind() == TypeKind.WILDCARD) {
            WildcardType wildcardType = (WildcardType) typeMirror;
            result = wildcardType.getExtendsBound() != null;
        }
        return result;
    }

    @Override
    public Set<Type> getImportTypes() {
        Set<Type> result = new HashSet<>();

        if (getTypeMirror().getKind() == TypeKind.DECLARED) {
            result.add(this);
        }

        if (componentType != null) {
            result.addAll(componentType.getImportTypes());
        }

        for (Type parameter : typeParameters) {
            result.addAll(parameter.getImportTypes());
        }

        if ((isWildCardExtendsBound() || isWildCardSuperBound()) && getTypeBound() != null) {
            result.addAll(getTypeBound().getImportTypes());
        }

        return result;
    }

    public Type getTypeBound() {
        if (boundingBase != null) {
            return boundingBase;
        }

        boundingBase = typeFactory.getType(typeFactory.getTypeBound(getTypeMirror()));

        return boundingBase;
    }

    private String trimSimpleClassName(String className) {
        if (className == null) {
            return null;
        }
        String trimmedClassName = className;
        while (trimmedClassName.endsWith("[]")) {
            trimmedClassName = trimmedClassName.substring(0, trimmedClassName.length() - 2);
        }
        return trimmedClassName;
    }

    @Override
    public int hashCode() {
        // javadoc typemirror: "Types should be compared using the utility methods in
        // Types. There is no guarantee
        // that any particular type will always be represented by the same object." This
        // is true when the objects
        // are in another jar than the mapper. So the qualfiedName is a better
        // candidate.
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;

        return typeUtils.isSameType(typeMirror, other.typeMirror);
    }
}
