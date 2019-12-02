package cloud.goudai.httpclient.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/25
 */
public class AnnotationProcessorContext {
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Messager messager;
    private final Filer filer;
    private final Set<TypeSpec> typeSpecs = new HashSet<>();
    private final Set<MethodSpec> sharedMethodSpec = new HashSet<>();
    private final Set<FieldSpec> sharedFieldSpec = new HashSet<>();

    public AnnotationProcessorContext(Elements elementUtils,
                                      Types typeUtils,
                                      Messager messager,
                                      Filer filer) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.messager = messager;
        this.filer = filer;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Messager getMessager() {
        return messager;
    }

    public Filer getFiler() {
        return filer;
    }

    public Set<TypeSpec> getTypeSpecs() {
        return typeSpecs;
    }

    public Set<MethodSpec> getSharedMethodSpec() {
        return sharedMethodSpec;
    }

    public Set<FieldSpec> getSharedFieldSpec() {
        return sharedFieldSpec;
    }

    public Set<TypeSpec> addSpec(TypeSpec typeSpec) {
        typeSpecs.add(typeSpec);
        return typeSpecs;
    }

    public Set<MethodSpec> addSpec(MethodSpec methodSpec) {
        sharedMethodSpec.add(methodSpec);
        return sharedMethodSpec;
    }

    public Set<FieldSpec> addSpec(FieldSpec fieldSpec) {
        sharedFieldSpec.add(fieldSpec);
        return sharedFieldSpec;
    }
}
