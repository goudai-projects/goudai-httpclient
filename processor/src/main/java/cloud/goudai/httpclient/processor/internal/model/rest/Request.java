package cloud.goudai.httpclient.processor.internal.model.rest;

import cloud.goudai.httpclient.processor.internal.model.common.Type;

import javax.lang.model.element.ExecutableElement;
import java.util.Map;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Request {
    private String name;
    private String path;
    private RequestMethod method;
    private Map<String, Param> namedPathVariable;
    private Map<Integer, Param> indexedPathVariable;
    private Map<String, Param> headers;
    private Map<String, Param> queryParams;
    private Param body;
    private Type responseType;
    private ExecutableElement executableElement;

    private Request(Builder builder) {
        name = builder.name;
        path = builder.path;
        method = builder.method;
        namedPathVariable = builder.namedPathVariable;
        indexedPathVariable = builder.indexedPathVariable;
        headers = builder.headers;
        queryParams = builder.queryParams;
        body = builder.body;
        responseType = builder.responseType;
        executableElement = builder.executableElement;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getPath() {
        return path;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Map<String, Param> getNamedPathVariable() {
        return namedPathVariable;
    }

    public Map<Integer, Param> getIndexedPathVariable() {
        return indexedPathVariable;
    }

    public Map<String, Param> getHeaders() {
        return headers;
    }

    public Map<String, Param> getQueryParams() {
        return queryParams;
    }

    public String getName() {
        return name;
    }

    public Param getBody() {
        return body;
    }

    public Type getResponseType() {
        return responseType;
    }

    public ExecutableElement getExecutableElement() {
        return executableElement;
    }

    public static final class Builder {
        private String path;
        private RequestMethod method;
        private Map<String, Param> namedPathVariable;
        private Map<Integer, Param> indexedPathVariable;
        private Map<String, Param> headers;
        private Map<String, Param> queryParams;
        private Param body;
        private Type responseType;
        private ExecutableElement executableElement;
        private String name;

        private Builder() {
        }

        public Builder path(String val) {
            path = val;
            return this;
        }

        public Builder method(RequestMethod val) {
            method = val;
            return this;
        }

        public Builder namedPathVariable(Map<String, Param> val) {
            namedPathVariable = val;
            return this;
        }

        public Builder indexedPathVariable(Map<Integer, Param> val) {
            indexedPathVariable = val;
            return this;
        }

        public Builder headers(Map<String, Param> val) {
            headers = val;
            return this;
        }

        public Builder queryParams(Map<String, Param> val) {
            queryParams = val;
            return this;
        }

        public Builder body(Param val) {
            body = val;
            return this;
        }

        public Builder responseType(Type val) {
            responseType = val;
            return this;
        }

        public Builder executableElement(ExecutableElement val) {
            executableElement = val;
            return this;
        }

        public Request build() {
            return new Request(this);
        }

        public Builder name(String val) {
            name = val;
            return this;
        }
    }
}
