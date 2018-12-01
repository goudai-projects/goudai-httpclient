package cloud.goudai.httpclient.processor.internal;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Map;

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

}
