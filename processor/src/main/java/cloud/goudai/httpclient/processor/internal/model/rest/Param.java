package cloud.goudai.httpclient.processor.internal.model.rest;

import cloud.goudai.httpclient.processor.internal.model.common.Type;

/**
 * @author jianglin
 * @date 2019/12/2
 */
public class Param {

    private String name;
    private Type type;
    private String reader;

    public Param(String name, Type type, String reader) {
        this.name = name;
        this.type = type;
        this.reader = reader;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getReader() {
        return reader;
    }
}
