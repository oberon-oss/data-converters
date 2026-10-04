package eu.oberon.oss.tools.retrievers.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.converters.varlen.text.TextToObjectConverter;
import eu.oberon.oss.tools.retrievers.varlen.AbstractVarLenValueRetriever;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Base class for text value retrievers.
 *
 * @param <T> The type of object to retrieve.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractTextValueRetriever<T> extends AbstractVarLenValueRetriever<T> implements TextValueRetriever<T> {

    /**
     * Constructs a new instance of AbstractTextValueRetriever.
     *
     * @param converter      The text-to-object converter to use.
     * @param valueTypeNames The value type names.
     *
     * @since 1.0.0
     */
    protected AbstractTextValueRetriever(TextToObjectConverter<T> converter, ValueTypeNames valueTypeNames) {
        super(valueTypeNames, converter);
    }

    @Override
    public T getValue(BinaryDataReader reader, int length, ByteOrder byteOrder, Charset charset) {
        TextToObjectConverter<T> converter = (TextToObjectConverter<T>) getConverter();
        return converter.convert(reader.readBytes(length), charset, byteOrder);
    }

    @Override
    public T getValue(BinaryDataViewer viewer, int offset, int length, ByteOrder byteOrder, Charset charset) {
        TextToObjectConverter<T> converter = (TextToObjectConverter<T>) getConverter();
        return converter.convert(viewer.peekBytes(offset, length), charset, byteOrder);
    }

    @Override
    public T getValue(BinaryDataViewer viewer, int offset, int length, Charset charset) {
        return getValue(viewer, offset, length, ByteOrder.nativeOrder(), charset);
    }

    @Override
    public T getValue(BinaryDataReader reader, int length, Charset charset) {
        return getValue(reader, length, ByteOrder.nativeOrder(), charset);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method overrides the implementation in {@link AbstractVarLenValueRetriever} to provide a default charset.
     *
     */
    @Override
    public final T getValue(BinaryDataViewer viewer, int offset, int length) {
        return getValue(viewer, offset, length, ByteOrder.nativeOrder(), Charset.defaultCharset());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method overrides the implementation in {@link AbstractVarLenValueRetriever} to provide a default charset.
     *
     */
    @Override
    public final T getValue(BinaryDataViewer viewer, int offset, int length, ByteOrder byteOrder) {
        return getValue(viewer, offset, length, byteOrder, Charset.defaultCharset());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method overrides the implementation in {@link AbstractVarLenValueRetriever} to provide a default charset.
     *
     */
    @Override
    public final T getValue(BinaryDataReader reader, int length) {
        return getValue(reader, length, ByteOrder.nativeOrder(), Charset.defaultCharset());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method overrides the implementation in {@link AbstractVarLenValueRetriever} to provide a default charset.
     *
     */
    @Override
    public final T getValue(BinaryDataReader reader, int length, ByteOrder byteOrder) {
        return getValue(reader, length, byteOrder, Charset.defaultCharset());
    }

}
