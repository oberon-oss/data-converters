package eu.oberon.oss.tools.converters.binary;

import eu.oberon.oss.tools.converters.AbstractConverter;
import eu.oberon.oss.tools.converters.BiDirectionalConverter;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides bidirectional conversion between a Java type and a byte array (binary data). Extends {@link BiDirectionalConverter} with source type {@code <T>} and
 * target type {@code byte[]}.
 *
 * @param <T> the type of object that can be converted to and from {@code byte[]}
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface BinaryConverter<T> extends BiDirectionalConverter<T, byte[]> {

    /**
     * Returns the class type of the object that can be converted to and from {@code byte[]}.
     *
     * @return the class type of the object
     *
     * @since 1.0.0
     */
    Class<T> getTypeClass();

    /**
     * Converts the specified object to a byte array using native byte order.
     *
     * @param object the object to convert
     *
     * @return the byte array representation of the object
     *
     * @since 1.0.0
     */
    byte[] toBytes(T object);

    /**
     * Converts the specified byte array to an object using native byte order.
     *
     * @param bytes the byte array to convert
     *
     * @return the object constructed from the byte array
     *
     * @since 1.0.0
     */
    T fromBytes(byte[] bytes);

    /**
     * Converts the specified object to a byte array using the given byte order.
     *
     * @param object    the object to convert
     * @param byteOrder the byte order to use
     *
     * @return the byte array representation of the object
     *
     * @since 1.0.0
     */
    default byte[] toBytes(T object, ByteOrder byteOrder) {
        return toBytes(object);
    }

    /**
     * Converts the specified byte array to an object using the given byte order.
     *
     * @param bytes     the byte array to convert
     * @param byteOrder the byte order to use
     *
     * @return the object constructed from the byte array
     *
     * @since 1.0.0
     */
    default T fromBytes(byte[] bytes, ByteOrder byteOrder) {
        return fromBytes(bytes);
    }

    @Override
    default Class<T> getSourceType() {
        return getTypeClass();
    }

    @Override
    default Class<byte[]> getTargetType() {
        return byte[].class;
    }

    @Override
    default Function<T, byte[]> getToTargetFunction() {
        return this::toBytes;
    }

    @Override
    default Function<byte[], T> getToSourceFunction() {
        return this::fromBytes;
    }

    /**
     * Returns a {@link BiDirectionalConverter} configured for a specific byte order.
     *
     * @param byteOrder the byte order to configure
     *
     * @return a bidirectional converter using the specified byte order
     *
     * @since 1.0.0
     */
    default BiDirectionalConverter<T, byte[]> withByteOrder(ByteOrder byteOrder) {
        Objects.requireNonNull(byteOrder, "Parameter: byteOrder");
        return AbstractConverter.of(
                getSourceType(),
                getTargetType(),
                obj -> toBytes(obj, byteOrder),
                bytes -> fromBytes(bytes, byteOrder));
    }
}
