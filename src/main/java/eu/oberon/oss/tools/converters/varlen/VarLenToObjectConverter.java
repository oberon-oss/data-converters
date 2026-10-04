package eu.oberon.oss.tools.converters.varlen;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Defines a contract for classes that convert variable-length byte arrays into objects of type {@code <T>}.
 *
 * @param <T> The type of object to convert the byte array into.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface VarLenToObjectConverter<T> {
    /**
     * Converts the specified input byte array into an object of type {@code <T>}, using {@link Charset#defaultCharset()} and {@link ByteOrder#nativeOrder()}.
     *
     * @param input The input byte array to be converted.
     *
     * @return The object of type {@code <T>} created from the input byte array.
     *
     * @since 1.0.0
     */
    T convert(byte[] input);


    /**
     * Converts the specified input byte array into an object of type {@code <T>}, using the specified byte order (ByteOrder) and
     * {@link Charset#defaultCharset()}.
     *
     * @param input     The input byte array to be converted.
     * @param byteOrder The byte order (ByteOrder) to be used for conversion.
     *
     * @return The object of type {@code <T>} created from the input byte array.
     *
     * @since 1.0.0
     */
    T convert(byte[] input, ByteOrder byteOrder);

}
