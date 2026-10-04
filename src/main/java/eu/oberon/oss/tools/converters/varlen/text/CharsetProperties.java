package eu.oberon.oss.tools.converters.varlen.text;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * Small utility describing properties of a {@link Charset} that are relevant to endian-aware text encoding/decoding.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
final class CharsetProperties {

    private CharsetProperties() {
    }

    /**
     * Attempts to determine if the character set encodes every character to exactly one byte.
     *
     * @param charset the charset to check
     *
     * @return true if every character encodes to exactly one byte
     *
     * @since 1.0.0
     */
    static boolean isSingleByte(Charset charset) {
        return charset.newEncoder().maxBytesPerChar() == 1.0f;
    }

    /**
     * Determines if the character set is byte order sensitive.
     *
     * @param charset the charset to check
     *
     * @return true if the character set is byte order sensitive
     *
     * @since 1.0.0
     */
    static boolean isByteOrderSensitive(Charset charset) {
        if (isSingleByte(charset)) {
            return false;
        }
        String name = charset.name();
        return name.equalsIgnoreCase("UTF-16") || name.equalsIgnoreCase("UTF-32");
    }

    /**
     * Tests the parameters for validity.
     *
     * @param input     the input object
     * @param charset   the charset
     * @param byteOrder the byte order
     *
     * @throws NullPointerException if any of the parameters is null
     * @since 1.0.0
     */
    static void testParameters(Object input, Charset charset, ByteOrder byteOrder) {
        Objects.requireNonNull(input, "Parameter: input");
        Objects.requireNonNull(charset, "Parameter: charset");
        Objects.requireNonNull(byteOrder, "Parameter: byteOrder");
    }
}