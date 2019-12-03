package cloud.goudai.httpclient.processor.internal.model.rest;

import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public enum RequestMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;


    private static final Map<String, RequestMethod> mappings = new HashMap<>(16);

    static {
        for (RequestMethod requestMethod : values()) {
            mappings.put(requestMethod.name(), requestMethod);
        }
    }

    @Nullable
    public static RequestMethod resolve(@Nullable String method) {
        return (method != null ? mappings.get(method) : null);
    }


    public boolean matches(String method) {
        return (this == resolve(method));
    }
}
