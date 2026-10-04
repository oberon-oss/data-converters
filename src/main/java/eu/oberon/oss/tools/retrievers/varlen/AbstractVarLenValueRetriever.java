package eu.oberon.oss.tools.retrievers.varlen;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.converters.varlen.VarLenToObjectConverter;
import eu.oberon.oss.tools.retrievers.AbstractValueRetriever;

import java.nio.ByteOrder;

/**
 * Abstract base class for retrieving variable-length values and converting them into objects of type {@code <T>}.
 *
 * @param <T> The type of object to retrieve.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractVarLenValueRetriever<T> extends AbstractValueRetriever implements VarLenValueRetriever<T> {
    private final VarLenToObjectConverter<T> converter;

    /**
     * Constructs a new instance of {@code AbstractVarLenValueRetriever} with the specified converter.
     *
     * @param converter      The converter to use for converting variable-length values to objects of type {@code <T>}.
     * @param valueTypeNames The value type name of the type of object to retrieve.
     *
     * @since 1.0.0
     */
    protected AbstractVarLenValueRetriever(ValueTypeNames valueTypeNames, VarLenToObjectConverter<T> converter) {
        super(valueTypeNames);
        this.converter = converter;
    }

    /**
     * Returns the converter specified at construction time.
     *
     * @return The converter used for converting variable-length values to objects of type {@code <T>}.
     *
     * @since 1.0.0
     */
    protected VarLenToObjectConverter<T> getConverter() {
        return converter;
    }

    @Override
    public T getValue(BinaryDataViewer viewer, int offset, int length) {
        return getValue(viewer, offset, length, ByteOrder.nativeOrder());
    }

    @Override
    public T getValue(BinaryDataViewer viewer, int offset, int length, ByteOrder byteOrder) {
        return converter.convert(viewer.peekBytes(offset, length), byteOrder);
    }

    @Override
    public T getValue(BinaryDataReader reader, int length) {
        return getValue(reader, length, ByteOrder.nativeOrder());
    }

    @Override
    public T getValue(BinaryDataReader reader, int length, ByteOrder byteOrder) {
        return converter.convert(reader.readBytes(length), byteOrder);
    }
}
