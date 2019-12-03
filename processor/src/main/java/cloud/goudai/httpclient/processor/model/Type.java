package cloud.goudai.httpclient.processor.model;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/22
 */
public class Type implements Metadata {

    private final Types typeUtils;
    private final Elements elementUtils;

    private final TypeMirror typeMirror;
    private final TypeElement typeElement;

    private final Set<Type> implementsTypes;
    private final String packageName;
    private final String name;
    private final String qualifiedName;

    private final Set<Method> methods;

    public Type(Types typeUtils, Elements elementUtils, TypeMirror typeMirror, TypeElement typeElement,
                Set<Type> implementsTypes, String packageName, String name, String qualifiedName, Set<Method> methods) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.typeMirror = typeMirror;
        this.typeElement = typeElement;
        this.implementsTypes = implementsTypes;
        this.packageName = packageName;
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.methods = methods;
    }

    @Override
    public Set<Type> importedTypes() {
        Set<Type> result = new HashSet<>();

        if (getTypeMirror().getKind() == TypeKind.DECLARED) {
            result.add(this);
        }

        if (implementsTypes != null) {
            for (Type implementsType : implementsTypes) {
                result.addAll(implementsType.importedTypes());
            }
        }

        return result;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }
}
