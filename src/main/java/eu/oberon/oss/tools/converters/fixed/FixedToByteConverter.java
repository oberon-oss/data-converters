package eu.oberon.oss.tools.converters.fixed;

import java.nio.ByteOrder;

/**
 * Contract for converting fixed-width data types into byte arrays.
 *
 * @param <T> The type of Class that will be converted into a byte array.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface FixedToByteConverter<T> {
    /**
     * Converts the given object into a byte array, assuming the byte order defined {@link ByteOrder#nativeOrder()}
     *
     * @param object The object to convert.
     *
     * @return The byte array representation of the object.
     *
     * @since 1.0.0
     */
    byte[] convert(T object);

    /**
     * Converts the given object into a byte array, assuming the byte order specified by the caller.
     *
     * @param object    The object to convert.
     * @param byteOrder The byte order to use for the conversion.
     *
     * @return The byte array representation of the object.
     *
     * @since 1.0.0
     */
    byte[] convert(T object, ByteOrder byteOrder);
}

