package eu.oberon.oss.tools.converters.fixed;

import java.nio.ByteOrder;

/**
 * Converts a byte array to an object of type T, assuming the native byte order {@link ByteOrder#nativeOrder()}
 *
 * @param <T> The target type of the converter.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface FixedToObjectConverter<T> {

    /**
     * Converts a byte array to an object of type T, assuming the native byte order {@link ByteOrder#nativeOrder()}
     *
     * @param bytes The byte array to convert.
     *
     * @return A function that takes a byte array and returns an object of type T.
     *
     * @since 1.0.0
     */
    T convert(byte[] bytes);

    /**
     * Converts a byte array to an object of type T, using the specified byte order.
     *
     * @param bytes     The byte array to convert.
     * @param byteOrder The byte order to use for conversion.
     *
     * @return A function that takes a byte array and returns an object of type T.
     *
     * @since 1.0.0
     */
    T convert(byte[] bytes, ByteOrder byteOrder);
}

