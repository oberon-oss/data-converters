package eu.oberon.oss.tools.binaryreader;

import java.util.Objects;

/**
 * An implementation of the {@link BinaryDataReader} interface that allows reading binary data from a {@link BinaryDataViewer} at specified offsets. The reader
 * maintains an internal offset which will be automatically updated as data is read.
 * <p>
 * This class provides methods for reading individual bytes or arrays of bytes, ensuring that the data being accessed is within the bounds of the underlying
 * binary data.
 * <p>
 * Instances of this class are immutable with respect to the provided {@link BinaryDataViewer}, but the internal offset is mutable and updated as read
 * operations occur.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class BinaryDataReaderImpl implements BinaryDataReader {
    private final BinaryDataViewer viewer;
    private int offset;

    /**
     * Constructs a new {@link BinaryDataReaderImpl} instance with the specified {@link BinaryDataViewer}.
     *
     * @param viewer the {@link BinaryDataViewer} from which binary data will be read
     *
     * @since 1.0.0
     */
    public BinaryDataReaderImpl(BinaryDataViewer viewer) {
        Objects.requireNonNull(viewer, "Parameter: viewer");
        this.viewer = viewer;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public void offset(int offset) {
        viewer.ensureBytesAvailable(offset, 0);
        this.offset = offset;
    }

    @Override
    public byte readByte() {
        return readByte(offset);
    }

    @Override
    public byte readByte(int offset) {
        byte value = viewer.peekByte(offset);
        this.offset = offset + 1;
        return value;
    }

    @Override
    public byte[] readBytes(int length) {
        return readBytes(offset, length);
    }

    @Override
    public byte[] readBytes(int offset, int length) {
        byte[] bytes = viewer.peekBytes(offset, length);
        this.offset = offset + length;
        return bytes;
    }

    @Override
    public void readBytes(byte[] target, int offset) {
        viewer.peekBytes(target, offset);
        this.offset = offset + target.length;
    }

    @Override
    public int remaining() {
        return viewer.size() - offset;
    }

    @Override
    public boolean matches(byte[] expected) {
        return matches(offset, expected);
    }

    @Override
    public boolean matches(int offset, byte[] expected) {
        Objects.requireNonNull(expected, "Parameter: expected");
        Objects.checkFromIndexSize(offset, 0, viewer.size());

        if (expected.length > viewer.size() - offset) {
            return false;
        }

        for (int index = 0; index < expected.length; index++) {
            if (viewer.peekByte(offset + index) != expected[index]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void skip(int length) {
        offset(offset + length);
    }

    @Override
    public BinaryDataViewer getViewer() {
        return viewer;
    }
}