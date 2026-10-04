package eu.oberon.oss.tools.converters.varlen;

import eu.oberon.oss.tools.converters.ConverterProvider;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;

import java.nio.ByteOrder;

/**
 * Provider for methods that convert a specific target class to bytes, or vice versa. Extends {@link BinaryConverter} to seamlessly participate in bidirectional
 * conversions.
 *
 * @param <T> The type of class that will be either consumed or produced by the converters.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface VarLenConverterProvider<T> extends ConverterProvider, BinaryConverter<T> {

    /**
     * Returns the converter that converts an object of type {@code <T>} to bytes.
     *
     * @return A converter that takes a byte[] and produces an object of type {@code <T>}.
     *
     * @since 1.0.0
     */
    VarLenToObjectConverter<T> getToObjectConverter();

    /**
     * Returns the converter that converts Type {@code <T> } into a byte array.
     *
     * @return The converter that converts an object of type {@code <T>} to byte[].
     *
     * @since 1.0.0
     */
    VarLenToByteConverter<T> getToByteConverter();

    @Override
    default byte[] toBytes(T object) {
        return getToByteConverter().convert(object);
    }

    @Override
    default T fromBytes(byte[] bytes) {
        return getToObjectConverter().convert(bytes);
    }

    @Override
    default byte[] toBytes(T object, ByteOrder byteOrder) {
        return getToByteConverter().convert(object, byteOrder);
    }

    @Override
    default T fromBytes(byte[] bytes, ByteOrder byteOrder) {
        return getToObjectConverter().convert(bytes, byteOrder);
    }
}
