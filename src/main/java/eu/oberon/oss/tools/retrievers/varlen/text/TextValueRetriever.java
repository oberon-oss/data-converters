package eu.oberon.oss.tools.retrievers.varlen.text;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.retrievers.varlen.VarLenValueRetriever;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Describes retrieval methods that allow users to retrieve text values from binary data.
 * <p>
 * <b>NOTE</b>:
 * For methods that do not specify the character set and/or byte order:
 *
 * <ul>
 *     <li>The character set used is {@link Charset#defaultCharset()}.</li>
 *     <li>The byte order used is {@link ByteOrder#nativeOrder()}.</li>
 * </ul>
 *
 * @param <T> The type of text object to retrieve.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface TextValueRetriever<T> extends VarLenValueRetriever<T> {

    /**
     * Retrieves an object of type T from a binary data viewer, using the specified charset.
     *
     * @param viewer  The binary data viewer.
     * @param offset  The offset of the bytes to convert in the viewer.
     * @param length  The number of bytes to retrieve.
     * @param charset The charset to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataViewer viewer, int offset, int length, Charset charset);

    /**
     * Retrieves an object of type T from a binary data viewer, using the specified byte order and charset.
     *
     * @param viewer    The binary data viewer.
     * @param offset    The offset of the bytes to convert in the viewer.
     * @param length    The number of bytes to retrieve.
     * @param byteOrder The byte order to use when retrieving the value.
     * @param charset   The charset to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataViewer viewer, int offset, int length, ByteOrder byteOrder, Charset charset);

    /**
     * Retrieves an object of type T from a binary data reader, using the specified charset.
     *
     * @param reader  The binary data viewer.
     * @param length  The number of bytes to retrieve.
     * @param charset The charset to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataReader reader, int length, Charset charset);

    /**
     * Retrieves an object of type T from a binary data reader, updating the cursor maintained with in the reader itself.
     *
     * @param reader    The binary data viewer.
     * @param length    The number of bytes to retrieve.
     * @param byteOrder The byte order to use when retrieving the value.
     * @param charset   The charset to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataReader reader, int length, ByteOrder byteOrder, Charset charset);

}
