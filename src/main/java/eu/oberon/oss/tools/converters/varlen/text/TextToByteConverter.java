package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.converters.varlen.VarLenToByteConverter;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * An interface for converting text-based input of type {@code <T>} into a byte array representation. Implementations of this interface allow the conversion to
 * be customized using a character set (Charset) and a byte order (ByteOrder).
 *
 * @param <T> The type of the input object to be converted to bytes.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface TextToByteConverter<T> extends VarLenToByteConverter<T> {

    /**
     * Converts the specified input object into a byte array representation, using the user-provided character set and {@link ByteOrder#nativeOrder()}.
     *
     * @param input   The input object to be converted.
     * @param charset The character set to use for the conversion.
     *
     * @return The byte array representation of the input object.
     *
     * @since 1.0.0
     */
    byte[] convert(T input, Charset charset);

    /**
     * Converts the specified input object into a byte array representation, using the user-provided character set and byte order.
     *
     * @param input     The input object to be converted.
     * @param charset   The character set to use for the conversion.
     * @param byteOrder The byte order to use for the conversion.
     *
     * @return The byte array representation of the input object.
     *
     * @since 1.0.0
     */
    byte[] convert(T input, Charset charset, ByteOrder byteOrder);
}
