package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;

import java.util.Objects;

/**
 * Contract for converting fixed-width data types into byte arrays.
 *
 * @param <T> The target to convert to or from byte arrays.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractFixedConverterProvider<T> extends AbstractConverterProvider implements FixedConverterProvider<T> {
    private final Class<T> typeClass;
    private final int expectedByteArraySize;

    /**
     * Constructs an instance of {@code AbstractFixedConverterProvider} with the specified value type and the expected size of the byte array.
     *
     * @param valueType             The {@link ValueTypeNames} representing the type of value being converted.
     * @param expectedByteArraySize The fixed size of the byte array required/expected for conversions.
     *
     * @since 1.0.0
     */
    protected AbstractFixedConverterProvider(ValueTypeNames valueType, int expectedByteArraySize) {
        this(valueType, null, expectedByteArraySize);
    }

    /**
     * Constructs an instance of {@code AbstractFixedConverterProvider} with the specified value type, type class, and the expected size of the byte array.
     *
     * @param valueType             The {@link ValueTypeNames} representing the type of value being converted.
     * @param typeClass             The class type of the target object.
     * @param expectedByteArraySize The fixed size of the byte array required/expected for conversions.
     *
     * @since 1.0.0
     */
    protected AbstractFixedConverterProvider(ValueTypeNames valueType, Class<T> typeClass, int expectedByteArraySize) {
        super(valueType);
        this.typeClass = typeClass;
        this.expectedByteArraySize = expectedByteArraySize;
    }

    @Override
    public Class<T> getTypeClass() {
        return typeClass;
    }

    /**
     * Checks that the specified byte array has the expected size. The length of the provided byte array must match the size as set at construction time. The
     * required size can be viewed by calling {@link #getExpectedByteArraySize()}.
     *
     * @param byteArray The byte array to check.
     *
     * @throws NullPointerException     if the specified byte array is null
     * @throws IllegalArgumentException if the specified byte array has the wrong size.
     * @since 1.0.0
     */
    protected void checkByteArraySize(final byte[] byteArray) {
        Objects.requireNonNull(byteArray, "Parameter: byteArray");
        if (byteArray.length != expectedByteArraySize) {
            throw new IllegalArgumentException("Parameter: byteArray has wrong size: " + byteArray.length + "; expected size was " + expectedByteArraySize);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return The expected byte size of the input/output byte array. The specified size is at least 1, or -1 to indicate that the size is variable. A value of
     *         0 will result in an IllegalArgumentException being thrown by the converters provided by this provider.
     *
     * @since 1.0.0
     */
    @Override
    public int getExpectedByteArraySize() {
        return expectedByteArraySize;
    }

}
