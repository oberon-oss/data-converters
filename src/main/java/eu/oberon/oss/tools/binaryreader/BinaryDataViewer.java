package eu.oberon.oss.tools.binaryreader;

import eu.oberon.oss.tools.converters.BiDirectionalConverter;

/**
 * Represents an interface for viewing binary data.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface BinaryDataViewer {

    /**
     * Returns the size of the binary data.
     *
     * @return the size of the binary data
     *
     * @since 1.0.0
     */
    int size();

    /**
     * Ensures that a byte can be read at the given offset.
     *
     * @param offset the zero-based offset to check
     *
     * @throws IndexOutOfBoundsException if the offset is outside the data bounds
     * @since 1.0.0
     */
    void ensureByteAvailable(int offset);

    /**
     * Ensures that the requested number of bytes can be read from the given offset.
     *
     * @param offset the zero-based offset to start checking from
     * @param length the number of bytes to check
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws IllegalArgumentException  if the length is negative
     * @since 1.0.0
     *
     */
    void ensureBytesAvailable(int offset, int length);

    /**
     * Returns the byte at the given offset without changing any reader state.
     *
     * @param offset the zero-based offset to peek at
     *
     * @return the byte at the given offset
     *
     * @throws IndexOutOfBoundsException if the offset is outside the data bounds
     * @since 1.0.0
     */
    byte peekByte(int offset);

    /**
     * Returns a copy of bytes starting at the given offset without changing any reader state.
     *
     * @param offset the zero-based offset to start peeking from
     * @param length the number of bytes to peek
     *
     * @return a copy of the requested bytes
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws IllegalArgumentException  if the length is negative
     * @since 1.0.0
     */
    byte[] peekBytes(int offset, int length);

    /**
     * Reads n bytes starting at the given offset, where n is the length of the target array.
     *
     * @param target the target array to read into
     * @param offset the zero-based offset to start peeking from
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws NullPointerException      if the target array is null
     * @throws IllegalArgumentException  If the length of the target array is less {@code <= 0}
     * @since 1.0.0
     */
    void peekBytes(byte[] target, int offset);

    /**
     * Returns a reader for the given viewer.
     *
     * @return A reader for the given viewer
     *
     * @since 1.0.0
     */
    BinaryDataReader getReader();

    /**
     * Peeks a value of type {@code <T>} starting at the given offset without modifying reader state.
     *
     * @param converter the converter to use
     * @param offset    the zero-based offset to start peeking from
     * @param length    the number of bytes to peek
     * @param <T>       the result type
     *
     * @return the deserialized object
     *
     * @since 1.0.0
     */
    default <T> T peek(BiDirectionalConverter<T, byte[]> converter, int offset, int length) {
        byte[] bytes = peekBytes(offset, length);
        return converter.getToSourceFunction().apply(bytes);
    }
}