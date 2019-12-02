package cloud.goudai.httpclient.processor.internal.model.common;

import cloud.goudai.httpclient.processor.internal.utils.Collections;

import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Field implements ModelElement {

    private final String name;
    private final Type type;

    public Field(String name, Type type) {
        this.name = name;
        this.type = type;
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


}
