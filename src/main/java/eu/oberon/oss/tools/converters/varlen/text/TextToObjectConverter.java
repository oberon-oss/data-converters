package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.converters.varlen.VarLenToObjectConverter;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * An interface for converting byte array input into an object of type {@code <T>}. This interface defines methods that allow for customizable conversion using
 * character set (Charset) and byte order (ByteOrder) configurations.
 *
 * @param <T> The type of the object to be created from the input byte array.
 *
 * @author TigerLilly64
 * @since 1.0.0
 *
 */
public interface TextToObjectConverter<T> extends VarLenToObjectConverter<T> {

    /**
     * Converts the specified input byte array into an object of type {@code <T>}, using the specified character set (Charset) and
     * {@link ByteOrder#nativeOrder()}.
     *
     * @param input   The input byte array to be converted.
     * @param charset The character set (Charset) to be used for conversion.
     *
     * @return The object of type {@code <T>} created from the input byte array.
     *
     * @since 1.0.0
     */
    T convert(byte[] input, Charset charset);

    /**
     * Converts the specified input byte array into an object of type {@code <T>}, using the specified character set (Charset) and
     * {@link ByteOrder#nativeOrder()}.
     *
     * @param input     The input byte array to be converted.
     * @param charset   The character set (Charset) to be used for conversion.
     * @param byteOrder The byte order (ByteOrder) to be used for conversion.
     *
     * @return The object of type {@code <T>} created from the input byte array.
     *
     * @since 1.0.0
     */
    T convert(byte[] input, Charset charset, ByteOrder byteOrder);
}
