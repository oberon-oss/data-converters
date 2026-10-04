package eu.oberon.oss.tools.binaryreader;

import eu.oberon.oss.tools.converters.BiDirectionalConverter;

/**
 * The binary data reader combines the {@link BinaryDataViewer} class with an updateble cursor.
 * <p>
 * Where the Viewer can only be used for reading, the Reader will update the 'cursor' (offset) appropriately after performing the reading operations. It also
 * allows the user to adjust the offset.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface BinaryDataReader {
    /**
     * Returns the current reader offset.
     *
     * @return the current reader offset
     *
     * @since 1.0.0
     */
    int offset();

    /**
     * Sets the current reader offset.
     *
     * @param offset the new reader offset
     *
     * @throws IndexOutOfBoundsException if the offset is outside the data bounds
     * @since 1.0.0
     */
    void offset(int offset);

    /**
     * Reads a byte from the current reader offset and advances the offset by one.
     *
     * @return the byte at the current reader offset
     *
     * @throws IndexOutOfBoundsException if the current offset is outside the data bounds
     * @since 1.0.0
     */
    byte readByte();

    /**
     * Reads a byte from the given absolute offset and advances the reader offset to the next byte.
     *
     * @param offset the absolute offset to read from
     *
     * @return the byte at the given offset
     *
     * @throws IndexOutOfBoundsException if the offset is outside the data bounds
     * @since 1.0.0
     */
    byte readByte(int offset);

    /**
     * Reads bytes from the current reader offset and advances the offset by the requested length.
     *
     * @param length the number of bytes to read
     *
     * @return a copy of the requested bytes
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws IllegalArgumentException  if the length is negative
     * @since 1.0.0
     */
    byte[] readBytes(int length);

    /**
     * Reads bytes from the given absolute offset and advances the reader offset to the end of the read range.
     *
     * @param offset the absolute offset to read from
     * @param length the number of bytes to read
     *
     * @return a copy of the requested bytes
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws IllegalArgumentException  if the length is negative
     * @since 1.0.0
     */
    byte[] readBytes(int offset, int length);

    /**
     * Reads n bytes from the current reader offset and advances the offset by the number of bytes read.
     * <p>
     * The byte array passed for the target cannot be {@code null}, or a NullPointerException will be thrown. Passing a zero-length array as target parameter is
     * allowed - albeit somewhat pointless.
     *
     * @param target the target array to read into
     * @param offset the zero-based offset to start reading into
     *
     * @throws IndexOutOfBoundsException if the requested range is outside the data bounds
     * @throws NullPointerException      if the target array is null
     * @since 1.0.0
     */
    void readBytes(byte[] target, int offset);

    /**
     * Returns the number of bytes remaining from the current reader position.
     *
     * @return the number of unread bytes
     *
     * @since 1.0.0
     */
    int remaining();

    /**
     * Returns whether at least one byte remains available for reading.
     *
     * @return {@code true} if at least one byte remains, otherwise {@code false}
     *
     * @since 1.0.0
     */
    default boolean hasRemaining() {
        return remaining() > 0;
    }

    /**
     * Returns whether the given number of bytes remains available for reading.
     *
     * @param length the number of bytes to check
     *
     * @return {@code true} if {@code length} bytes remain available, otherwise {@code false}
     *
     * @throws IllegalArgumentException if {@code length} is negative
     * @since 1.0.0
     */
    default boolean hasRemaining(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must not be negative");
        }
        return remaining() >= length;
    }

    /**
     * Returns whether the bytes at the current reader offset match the expected byte sequence.
     * <p>
     * This method does not advance or otherwise change the reader offset.
     *
     * @param expected the expected byte sequence
     *
     * @return {@code true} if the bytes at the current reader offset match {@code expected}, otherwise {@code false}
     *
     * @throws NullPointerException if {@code expected} is null
     * @since 1.0.0
     */
    boolean matches(byte[] expected);

    /**
     * Returns whether the bytes at the given absolute offset match the expected byte sequence.
     * <p>
     * This method does not advance or otherwise change the reader offset.
     *
     * @param offset   the absolute offset to check from
     * @param expected the expected byte sequence
     *
     * @return {@code true} if the bytes at {@code offset} match {@code expected}, otherwise {@code false}
     *
     * @throws NullPointerException      if {@code expected} is null
     * @throws IndexOutOfBoundsException if {@code offset} is outside the data bounds
     * @since 1.0.0
     */
    boolean matches(int offset, byte[] expected);

    /**
     * Advances the reader offset by the given length.
     *
     * @param length the length to skip, this value can be positive (skipt forward) or negative (skip backward), as long as the index does not go outside the
     *               data bounds
     *
     * @throws IllegalArgumentException if {@code length} is negative
     * @since 1.0.0
     */
    void skip(int length);

    /**
     * Returns the viewer that is being used by this reader.
     *
     * @return the viewer being used
     *
     * @since 1.0.0
     */
    BinaryDataViewer getViewer();

    /**
     * Reads a value of type {@code <T>} using the provided bidirectional converter and byte length, advancing the reader offset.
     *
     * @param converter the converter to use
     * @param length    the number of bytes to read
     * @param <T>       the result type
     *
     * @return the deserialized object
     *
     * @since 1.0.0
     */
    default <T> T read(BiDirectionalConverter<T, byte[]> converter, int length) {
        byte[] bytes = readBytes(length);
        return converter.getToSourceFunction().apply(bytes);
    }
}
