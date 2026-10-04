package eu.oberon.oss.tools.converters.varlen;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Defines a contract for classes that convert objects from variable length byte arrays.
 *
 * @param <T> The type of object to convert the byte array into.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface VarLenToByteConverter<T> {
    /**
     * Converts the specified input object into a byte array representation, using {@link Charset#defaultCharset()} and {@link ByteOrder#nativeOrder()}.
     *
     * @param input The input object to be converted.
     *
     * @return The byte array representation of the input object.
     *
     * @since 1.0.0
     */
    byte[] convert(T input);

    /**
     * Converts the specified input object into a byte array representation, using {@link Charset#defaultCharset()} and the specified byte order.
     *
     * @param input     The input object to be converted.
     * @param byteOrder The byte order to use for the conversion.
     *
     * @return The byte array representation of the input object.
     *
     * @since 1.0.0
     */
    byte[] convert(T input, ByteOrder byteOrder);

}
