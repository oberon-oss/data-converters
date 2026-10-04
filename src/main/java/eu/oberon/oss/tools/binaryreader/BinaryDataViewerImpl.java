package eu.oberon.oss.tools.binaryreader;

import java.util.Arrays;
import java.util.Objects;

/**
 * Wraps a byte array and allows acces to its data indirectly by obtaining a {@link BinaryDataReader} instance.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class BinaryDataViewerImpl implements BinaryDataViewer {
    private final byte[] data;

    /**
     * Creates a new buffer with the given data using the platform native byte order.
     *
     * @param data the data to buffer
     *
     * @throws NullPointerException if the data is null
     * @since 1.0.0
     */
    public BinaryDataViewerImpl(byte[] data) {
        this.data = Arrays.copyOf(Objects.requireNonNull(data, "Parameter: data"), data.length);
    }

    /**
     * Returns the size of the buffer.
     *
     * @return the size of the buffer
     *
     * @since 1.0.0
     */
    @Override
    public int size() {
        return data.length;
    }

    @Override
    public void ensureByteAvailable(int offset) {
        Objects.checkIndex(offset, data.length);
    }

    @Override
    public void ensureBytesAvailable(int offset, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative");
        }
        Objects.checkFromIndexSize(offset, length, data.length);
    }

    @Override
    public byte peekByte(int offset) {
        ensureByteAvailable(offset);
        return data[offset];
    }

    @Override
    public byte[] peekBytes(int offset, int length) {
        ensureBytesAvailable(offset, length);
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    @Override
    public void peekBytes(byte[] target, int offset) {
        Objects.requireNonNull(target, "Parameter: target");
        ensureBytesAvailable(offset, target.length);
        System.arraycopy(data, offset, target, 0, target.length);

    }

    @Override
    public BinaryDataReader getReader() {
        return new BinaryDataReaderImpl(this);
    }
}
