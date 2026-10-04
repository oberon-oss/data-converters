package eu.oberon.oss.tools.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for managing and retrieving {@link BinaryConverter} instances and binary converter providers.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class BinaryConvertersRegistry {
    /**
     * -- GETTER --
     *  Returns the underlying bidirectional converters registry.
     *
     * @return the underlying bidirectional converters registry
     *
     *
     */
    @Getter
    private final BiDirectionalConvertersRegistry biDirectionalConvertersRegistry;
    private final ConcurrentHashMap<String, BinaryConverter<?>> nameToConverterMap;

    /**
     * Constructs a new registry with default bidirectional converters' registry.
     *
     * @since 1.0.0
     */
    public BinaryConvertersRegistry() {
        this(new BiDirectionalConvertersRegistry());
    }

    /**
     * Constructs a new registry with the specified bidirectional converters' registry.
     *
     * @param biDirectionalConvertersRegistry the underlying bidirectional converters registry
     *
     * @since 1.0.0
     */
    public BinaryConvertersRegistry(@NotNull BiDirectionalConvertersRegistry biDirectionalConvertersRegistry) {
        this.biDirectionalConvertersRegistry = Objects.requireNonNull(biDirectionalConvertersRegistry, "Parameter: biDirectionalConvertersRegistry");
        this.nameToConverterMap = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves a binary converter for a specific Java class type using native byte order.
     *
     * @param classType The class type for which to retrieve the binary converter.
     * @param <T>       The type of the objects handled.
     *
     * @return The binary converter for the specified type, or null if none is found.
     *
     * @since 1.0.0
     */
    public <T> @Nullable BinaryConverter<T> getConverterForClassType(Class<T> classType) {
        Objects.requireNonNull(classType, "Parameter: classType");
        BiDirectionalConverter<T, byte[]> converter = biDirectionalConvertersRegistry.getConverterForClassTypes(classType, byte[].class);
        if (converter instanceof BinaryConverter) {
            return (BinaryConverter<T>) converter;
        }
        if (converter != null) {
            return new BinaryConverter<>() {
                @Override
                public Class<T> getTypeClass() {
                    return classType;
                }

                @Override
                public byte[] toBytes(T object) {
                    return converter.getToTargetFunction().apply(object);
                }

                @Override
                public T fromBytes(byte[] bytes) {
                    return converter.getToSourceFunction().apply(bytes);
                }
            };
        }
        return null;
    }

    /**
     * Retrieves a binary converter for a specific value type name (e.g., SIGNED_INTEGER, UNSIGNED_INTEGER).
     *
     * @param valueTypeName The value type name.
     * @param <T>           The type of the objects handled.
     *
     * @return The binary converter for the specified value type name, or null if none is found.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable BinaryConverter<T> getConverterForValueType(String valueTypeName) {
        Objects.requireNonNull(valueTypeName, "Parameter: valueTypeName");
        ConverterProvider provider = AbstractConverterProvider.getConverterProvider(valueTypeName);
        if (provider instanceof BinaryConverter) {
            return (BinaryConverter<T>) provider;
        }
        return (BinaryConverter<T>) nameToConverterMap.get(valueTypeName);
    }

    /**
     * Retrieves a binary converter for a specific {@link ValueTypeNames} enum.
     *
     * @param valueTypeName The value type enum.
     * @param <T>           The type of the objects handled.
     *
     * @return The binary converter for the specified value type, or null if none is found.
     *
     * @since 1.0.0
     */
    public <T> @Nullable BinaryConverter<T> getConverterForValueType(ValueTypeNames valueTypeName) {
        Objects.requireNonNull(valueTypeName, "Parameter: valueTypeName");
        return getConverterForValueType(valueTypeName.name());
    }

    /**
     * Retrieves a configured {@link BiDirectionalConverter} for a given class type and byte order.
     *
     * @param classType The class type.
     * @param byteOrder The byte order to apply.
     * @param <T>       The type of the objects handled.
     *
     * @return The configured converter, or null if none is found.
     *
     * @since 1.0.0
     */
    public <T> @Nullable BiDirectionalConverter<T, byte[]> getConverterForClassType(Class<T> classType, ByteOrder byteOrder) {
        BinaryConverter<T> converter = getConverterForClassType(classType);
        if (converter == null) {
            return null;
        }
        return converter.withByteOrder(byteOrder);
    }

    /**
     * Registers a binary converter.
     *
     * @param converter The binary converter to register.
     *
     * @since 1.0.0
     */
    public void registerConverter(@NotNull BinaryConverter<?> converter) {
        Objects.requireNonNull(converter, "Parameter: converter");
        biDirectionalConvertersRegistry.registerConverter(converter);
    }

    /**
     * Registers a named binary converter.
     *
     * @param name      The value type name.
     * @param converter The binary converter.
     *
     * @since 1.0.0
     */
    public void registerConverter(String name, @NotNull BinaryConverter<?> converter) {
        Objects.requireNonNull(name, "Parameter: name");
        Objects.requireNonNull(converter, "Parameter: converter");
        nameToConverterMap.put(name, converter);
    }

}
