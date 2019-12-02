package cloud.goudai.httpclient.processor.internal.conversion;

import cloud.goudai.httpclient.processor.internal.model.common.Field;
import cloud.goudai.httpclient.processor.internal.model.common.Parameter;
import cloud.goudai.httpclient.processor.internal.model.common.Type;
import cloud.goudai.httpclient.processor.internal.model.common.TypeFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jianglin
 * @date 2019/11/28
 */
public class Conversions implements ToStringProviderRegister {

    private final Map<Type, ToStringProvider> conversions = new HashMap<>();
    private final ToStringProvider DEFAULT_PROVIDER = new DefaultToStringProvider();
    private final Type enumType;
    private final TypeFactory typeFactory;


    public Conversions(TypeFactory typeFactory,
                       List<ToStringProviderRegisterAdapter> registerAdapters) {
        this.typeFactory = typeFactory;
        this.enumType = typeFactory.getType(Enum.class);

        // register
        registerToStringProvider(Enum.class, new EnumToStringProvider());
        registerToStringProvider(String.class, new StringToStringProvider());
        registerToStringProvider(Date.class, new DateToStringProvider());
        registerToStringProvider(LocalDateTime.class, new LocalDateTimeToStringProvider());
        registerToStringProvider(LocalDate.class, new LocalDateToStringProvider());
        registerToStringProvider(LocalTime.class, new LocalTimeToStringProvider());
        registerToStringProvider(Instant.class, new InstantToStringProvider());
        registerToStringProvider(BigDecimal.class, new BigDecimalToStringProvider());

        if (registerAdapters != null) {
            for (ToStringProviderRegisterAdapter adapter : registerAdapters) {
                adapter.registerToStringProvider(this);
            }
        }
    }

    public void registerToStringProvider(Class<?> clazz, ToStringProvider toStringProvider) {
        Type type = typeFactory.getType(clazz);
        registerToStringProvider(type, toStringProvider);
    }

    public void registerToStringProvider(Type type, ToStringProvider toStringProvider) {
        conversions.put(type, toStringProvider);
    }

    public ToStringProvider getToStringProvider(Type sourceType) {
        if (sourceType.isEnumType()) {
            sourceType = enumType;
        }
        return conversions.getOrDefault(sourceType, DEFAULT_PROVIDER);
    }

    public ToStringProvider getToStringProvider(Parameter parameter) {
        return getToStringProvider(parameter.getType());
    }

    public ToStringProvider getToStringProvider(Field field) {
        return getToStringProvider(field.getType());
    }
}
