package cloud.goudai.httpclient.processor.internal.model.common;

import cloud.goudai.httpclient.processor.internal.utils.Collections;

import javax.lang.model.element.VariableElement;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Parameter implements ModelElement {
    private final String name;
    private final String originalName;
    private final VariableElement variableElement;
    private final Type type;
    private final Integer index;

    private final boolean varArgs;

    public Parameter(String name, VariableElement variableElement, Type type, Integer index, boolean varArgs) {
        this.name = name;
        this.originalName = name;
        this.variableElement = variableElement;
        this.type = type;
        this.index = index;
        this.varArgs = varArgs;
    }

    public static Parameter forElementAndType(VariableElement element, Type parameterType, Integer index,
                                              boolean isVarArgs) {
        return new Parameter(
                element.getSimpleName().toString(),
                element,
                parameterType,
                index,
                isVarArgs);
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Type getType() {
        return type;
    }

    public Integer getIndex() {
        return index;
    }

    public boolean isVarArgs() {
        return varArgs;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    @Override
    public Set<Type> getImportTypes() {
        return Collections.asSet(type);
    }
}
