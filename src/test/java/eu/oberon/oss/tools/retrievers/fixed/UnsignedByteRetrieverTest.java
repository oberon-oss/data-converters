package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnsignedByteRetrieverTest {

    @Test
    void testGetValueFromViewerBigEndian() {
        byte[] data = {0x01};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        UnsignedByteRetriever retriever = new UnsignedByteRetriever();

        assertEquals(1, retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromViewerLittleEndian() {
        byte[] data = {0x01};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        UnsignedByteRetriever retriever = new UnsignedByteRetriever();

        assertEquals(1, retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetValueFromReaderBigEndian() {
        byte[] data = {0x01};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        UnsignedByteRetriever retriever = new UnsignedByteRetriever();

        assertEquals(1, retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromReaderLittleEndian() {
        byte[] data = {0x01};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        UnsignedByteRetriever retriever = new UnsignedByteRetriever();

        assertEquals(1, retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }
}
