package cloud.goudai.httpclient.processor.model;

import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/22
 */
public class Param implements Metadata {

    private Type type;
    private String name;
    private int idx;


    @Override
    public Set<Type> importedTypes() {
        return null;
    }
}
