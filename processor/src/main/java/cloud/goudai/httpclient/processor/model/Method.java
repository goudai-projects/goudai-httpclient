package cloud.goudai.httpclient.processor.model;

import javax.lang.model.element.Modifier;
import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/22
 */
public class Method implements Metadata {

    private String name;
    private Modifier[] modifiers;
    private Type returnType;

    @Override
    public Set<Type> importedTypes() {
        return null;
    }
}
