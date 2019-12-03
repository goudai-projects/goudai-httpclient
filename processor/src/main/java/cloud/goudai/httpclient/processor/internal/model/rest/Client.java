package cloud.goudai.httpclient.processor.internal.model.rest;

import cloud.goudai.httpclient.processor.internal.model.common.Type;

import java.util.Set;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Client {

    private String name;
    private String serviceName;
    private Type type;
    private String baseUrl;
    private String basePath;
    private Set<Request> requests;

    private Client(Builder builder) {
        name = builder.name;
        serviceName = builder.serviceName;
        type = builder.type;
        baseUrl = builder.baseUrl;
        basePath = builder.basePath;
        requests = builder.requests;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Type getType() {
        return type;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Set<Request> getRequests() {
        return requests;
    }

    public String getBasePath() {
        return basePath;
    }

    public static final class Builder {
        private String name;
        private String serviceName;
        private Type type;
        private String baseUrl;
        private String basePath;
        private Set<Request> requests;

        private Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder serviceName(String val) {
            serviceName = val;
            return this;
        }

        public Builder type(Type val) {
            type = val;
            return this;
        }

        public Builder baseUrl(String val) {
            baseUrl = val;
            return this;
        }

        public Builder basePath(String val) {
            basePath = val;
            return this;
        }

        public Builder requests(Set<Request> val) {
            requests = val;
            return this;
        }

        public Client build() {
            return new Client(this);
        }
    }
}
