package cloud.goudai.httpclient.processor.internal;

import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * @author jianglin
 * @date 2018-12-01
 */
public class Property {
    private String name;
    private String parent;
    private String reader;
    private Boolean isArray;
    private Boolean isMap;
    private Boolean isCollection;
    private Boolean isIterable;
    private Boolean isDate;
    private Boolean isEnum;

    private Property(Builder builder) {
        setName(builder.name);
        setParent(builder.parent);
        setReader(builder.reader);
        isArray = builder.isArray;
        isMap = builder.isMap;
        isCollection = builder.isCollection;
        isIterable = builder.isIterable;
        isDate = builder.isDate;
        isEnum = builder.isEnum;

    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public String getName() {
        return name;
    }

    public Property setName(String name) {
        this.name = name;
        return this;
    }

    public String getReader() {
        return reader;
    }

    public Property setReader(String reader) {
        this.reader = reader;
        return this;
    }

    public Boolean getArray() {
        return isArray;
    }

    public Property setArray(Boolean array) {
        isArray = array;
        return this;
    }

    public Boolean getMap() {
        return isMap;
    }

    public Property setMap(Boolean map) {
        isMap = map;
        return this;
    }

    public Boolean getCollection() {
        return isCollection;
    }

    public Property setCollection(Boolean collection) {
        isCollection = collection;
        return this;
    }

    public Boolean getIterable() {
        return isIterable;
    }

    public Property setIterable(Boolean iterable) {
        isIterable = iterable;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public Property setParent(String parent) {
        this.parent = parent;
        return this;
    }


    public String getReadAccessor() {
        if (parent == null || parent.trim().length() == 0) {
            return reader;
        }
        return String.join(".", parent, reader);
    }

    public String getIfNotNullStatement() {
        LinkedList<String> list = new LinkedList<>();
        String temp = getReadAccessor();
        list.add(temp);
        while (temp.indexOf("().") > 0) {
            temp = org.apache.commons.lang3.StringUtils.substringBeforeLast(temp, ".");
            list.addFirst(temp);
        }
        return list.stream().map(s -> s + " != null").collect(Collectors.joining(" && "));
    }

    public Boolean getDate() {
        return isDate;
    }

    public Boolean getEnum() {
        return isEnum;
    }

    public Property setDate(Boolean date) {
        isDate = date;
        return this;
    }

    public static final class Builder {
        private String name;
        private String parent;
        private String reader;
        private Boolean isArray = Boolean.FALSE;
        private Boolean isMap = Boolean.FALSE;
        private Boolean isCollection = Boolean.FALSE;
        private Boolean isIterable = Boolean.FALSE;
        private Boolean isDate = Boolean.FALSE;
        private Boolean isEnum = Boolean.FALSE;

        private Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder parent(String val) {
            parent = val;
            return this;
        }

        public Builder reader(String val) {
            reader = val;
            return this;
        }

        public Builder isArray(Boolean val) {
            isArray = val;
            return this;
        }

        public Builder isMap(Boolean val) {
            isMap = val;
            return this;
        }

        public Builder isCollection(Boolean val) {
            isCollection = val;
            return this;
        }

        public Builder isIterable(Boolean val) {
            isIterable = val;
            return this;
        }

        public Builder isDate(Boolean val) {
            isDate = val;
            return this;
        }

        public Builder isEnum(Boolean val) {
            isEnum = val;
            return this;
        }

        public Property build() {
            return new Property(this);
        }
    }
}
