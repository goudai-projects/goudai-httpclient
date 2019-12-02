package cloud.goudai.httpclient.processor.internal.utils;

import cloud.goudai.httpclient.processor.internal.model.common.Field;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author jianglin
 * @date 2019/12/2
 */
public final class AccessorUtils {
    private AccessorUtils() {
    }

    public static String getAccessor(Field field) {
        return getAccessor(field.getType().getTypeMirror(), field.getName());
    }

    public static String getAccessor(TypeMirror typeMirror, String fieldName) {
        String upperCaseStr = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
        if (typeMirror.getKind() == TypeKind.BOOLEAN) {
            return String.format("<SOURCE>.is%s()", upperCaseStr);
        }

        return String.format("<SOURCE>.get%s()", upperCaseStr);
    }
}
