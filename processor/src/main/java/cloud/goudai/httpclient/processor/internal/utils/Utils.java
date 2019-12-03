package cloud.goudai.httpclient.processor.internal.utils;

import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author jianglin
 * @date 2018-11-30
 */
public final class Utils {

    public static String getPath(String pathValue) {
        if (StringUtils.isBlank(pathValue)) return "";
        if (!pathValue.startsWith("/")) {
            pathValue = "/" + pathValue;
        }
        return pathValue;
    }

    public static boolean isSimpleValueType(Class<?> clazz) {
        return (ClassUtils.isPrimitiveOrWrapper(clazz) ||
                Enum.class.isAssignableFrom(clazz) ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                URI.class == clazz || URL.class == clazz ||
                Locale.class == clazz || Class.class == clazz);
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

    public static String getQualifiedName(TypeMirror type) {
        DeclaredType declaredType = type.accept(
                new SimpleTypeVisitor8<DeclaredType, Void>() {
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
                new SimpleElementVisitor8<TypeElement, Void>() {
                    @Override
                    public TypeElement visitType(TypeElement e, Void p) {
                        return e;
                    }
                },
                null
                                                                 );

        return typeElement != null ? typeElement.getQualifiedName().toString() : null;
    }

    public static String getSimpleName(TypeName typeName) {
        if (typeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName pTypeName = (ParameterizedTypeName) typeName;
            List<String> list = new ArrayList<>();
            list.add(pTypeName.rawType.simpleName());
            pTypeName.typeArguments.forEach(a -> list.add(getSimpleName(a)));
            return StringUtils.join(list, "_").toUpperCase();
        }

        return unqualify(typeName.toString()).toUpperCase();
    }

    public static String unqualify(String qualifiedName) {
        return unqualify(qualifiedName, '.');
    }

    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }
}
