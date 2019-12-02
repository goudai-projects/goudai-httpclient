package cloud.goudai.httpclient.processor.model;

import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/22
 */
public class Property implements Metadata {

    private String name;
    private Type type;

    public Property(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Set<Type> importedTypes() {
        return type.importedTypes();
    }
}
