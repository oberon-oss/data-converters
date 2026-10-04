package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnsignedShortRetrieverTest {

    @Test
    void testGetValueFromViewerBigEndian() {
        byte[] data = {0x00, 0x01}; // 1 in Big Endian
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        UnsignedShortRetriever retriever = new UnsignedShortRetriever();

        assertEquals(1, retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromViewerLittleEndian() {
        byte[] data = {0x01, 0x00}; // 1 in Little Endian
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        UnsignedShortRetriever retriever = new UnsignedShortRetriever();

        assertEquals(1, retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetValueFromReaderBigEndian() {
        byte[] data = {0x00, 0x01}; // 1 in Big Endian
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        UnsignedShortRetriever retriever = new UnsignedShortRetriever();

        assertEquals(1, retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromReaderLittleEndian() {
        byte[] data = {0x01, 0x00}; // 1 in Little Endian
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        UnsignedShortRetriever retriever = new UnsignedShortRetriever();

        assertEquals(1, retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }
}
