package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.converters.varlen.VarLenConverterProvider;

/**
 * A converter provider for managing the conversion of text-based values to and from byte arrays.
 * <p>
 * This interface extends {@link VarLenConverterProvider} and provides methods for getting converters that convert objects of type {@code <T>} to bytes and vice
 * versa.
 *
 * @param <T> The type of objects that this converter provider handles.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface TextConverterProvider<T> extends VarLenConverterProvider<T> {

    /**
     * Returns the converter that converts an object of type {@code <T>} to bytes.
     *
     * @return A converter that takes a byte[] and produces an object of type {@code <T>}.
     *
     * @since 1.0.0
     */
    @Override
    TextToObjectConverter<T> getToObjectConverter();

    /**
     * Returns the converter that converts Type {@code <T> } into
     *
     * @return The converter that converts an object of type {@code <T>} to byte[].
     *
     * @since 1.0.0
     */
    @Override
    TextToByteConverter<T> getToByteConverter();
}
