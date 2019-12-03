package cloud.goudai.httpclient.processor.internal.model.common;

import cloud.goudai.httpclient.processor.internal.utils.Collections;

import javax.lang.model.element.ExecutableElement;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Field implements ModelElement {

    private final String name;
    private final Type type;
    private final ExecutableElement executableElement;

    public Field(String name, Type type, ExecutableElement executableElement) {
        this.name = name;
        this.type = type;
        this.executableElement = executableElement;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Set<Type> getImportTypes() {
        return Collections.asSet(type);
    }

    public ExecutableElement getExecutableElement() {
        return executableElement;
    }
}
