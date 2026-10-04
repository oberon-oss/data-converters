package eu.oberon.oss.tools.converters.varlen;

import eu.oberon.oss.tools.ValueTypeNames;

/**
 * Abstract base class for providing converters that handle variable-length binary data conversions for a specified target type.
 * <p>
 * Instances of this class must define how to convert the target type to and from binary representations by implementing the required abstract methods.
 *
 * @param <T> The target type that the converters will handle.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractVarLenConverterProvider<T> implements VarLenConverterProvider<T> {

    private final ValueTypeNames valueType;
    private final Class<T> typeClass;
    private final VarLenToObjectConverter<T> toObjectConverter;
    private final VarLenToByteConverter<T> toByteConverter;

    /**
     * Constructs a new instance of {@code AbstractVarLenConverterProvider} with the specified value type.
     *
     * @param valueType The value type that the converters will handle.
     *
     * @since 1.0.0
     */
    protected AbstractVarLenConverterProvider(ValueTypeNames valueType) {
        this(valueType, null);
    }

    /**
     * Constructs a new instance of {@code AbstractVarLenConverterProvider} with the specified value type and target type class.
     *
     * @param valueType The value type that the converters will handle.
     * @param typeClass The class type of the target object.
     *
     * @since 1.0.0
     */
    protected AbstractVarLenConverterProvider(ValueTypeNames valueType, Class<T> typeClass) {
        this.valueType = valueType;
        this.typeClass = typeClass;
        this.toObjectConverter = createToObjectConverter();
        this.toByteConverter = createToByteConverter();
    }

    @Override
    public Class<T> getTypeClass() {
        return typeClass;
    }

    /**
     * Request the extending class to create a converter that converts a byte array to an object of type {@code <T>}.
     *
     * @return The to-object converter.
     *
     * @since 1.0.0
     */
    protected abstract VarLenToObjectConverter<T> createToObjectConverter();

    /**
     * Request the extending class to create a converter that converts an object of type {@code <T>} a byte array.
     *
     * @return The to-byte array converter.
     *
     * @since 1.0.0
     */
    protected abstract VarLenToByteConverter<T> createToByteConverter();

    @Override
    public VarLenToObjectConverter<T> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public VarLenToByteConverter<T> getToByteConverter() {
        return toByteConverter;
    }

    @Override
    public String getValueTypeName() {
        return valueType.name();
    }
}
