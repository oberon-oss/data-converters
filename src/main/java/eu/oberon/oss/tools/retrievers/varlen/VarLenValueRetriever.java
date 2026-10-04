package eu.oberon.oss.tools.retrievers.varlen;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.retrievers.ValueRetriever;

import java.nio.ByteOrder;

/**
 * Allows Retrieval user-defined objects by allowing reading variable length values from binary data and perform conversion.
 *
 * @param <T> The type of text object to retrieve.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public interface VarLenValueRetriever<T> extends ValueRetriever {

    /**
     * Retrieves an object of type T from a binary data viewer.
     *
     * @param viewer The binary data viewer.
     * @param offset The offset of the bytes to convert in the viewer.
     * @param length The number of bytes to retrieve.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataViewer viewer, int offset, int length);

    /**
     * Retrieves an object of type T from a binary data viewer, using the specified byte order.
     *
     * @param viewer    The binary data viewer.
     * @param offset    The offset of the bytes to convert in the viewer.
     * @param length    The number of bytes to retrieve.
     * @param byteOrder The byte order to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataViewer viewer, int offset, int length, ByteOrder byteOrder);

    /**
     * Retrieves a text object from a binary data reader, using the specified length.
     *
     * @param reader The binary data viewer.
     * @param length The number of bytes to retrieve.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataReader reader, int length);

    /**
     * Retrieves a text object from a binary data reader, using the specified length.
     *
     * @param reader    The binary data viewer.
     * @param length    The number of bytes to retrieve.
     * @param byteOrder The byte order to use when retrieving the value.
     *
     * @return The retrieved object.
     *
     * @since 1.0.0
     */
    T getValue(BinaryDataReader reader, int length, ByteOrder byteOrder);


}
