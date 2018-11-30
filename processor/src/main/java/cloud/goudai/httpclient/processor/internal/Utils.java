package cloud.goudai.httpclient.processor.internal;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * @author jianglin
 * @date 2018-11-30
 */
public class Utils {

    public static String getPath(String pathValue) {
        if (pathValue == null) return "";
        if (!pathValue.startsWith("/")) {
            pathValue = "/" + pathValue;
        }
        return pathValue;
    }

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    protected static String getQualifiedName(TypeMirror type) {
        DeclaredType declaredType = type.accept(
                new SimpleTypeVisitor6<DeclaredType, Void>() {
                    @Override
                    public DeclaredType visitDeclared(DeclaredType t, Void p) {
                        return t;
                    }
                },
                null
        );

        if (declaredType == null) {
            return null;
        }

        TypeElement typeElement = declaredType.asElement().accept(
                new SimpleElementVisitor6<TypeElement, Void>() {
                    @Override
                    public TypeElement visitType(TypeElement e, Void p) {
                        return e;
                    }
                },
                null
        );

        return typeElement != null ? typeElement.getQualifiedName().toString() : null;
    }
}
