package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanRetrieverTest {

    @Test
    void testGetFalseValueFromViewerBigEndian() {
        byte[] data = {0x00};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BooleanRetriever retriever = new BooleanRetriever();

        assertFalse(retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetTrueValueFromViewerBigEndian() {
        byte[] data = {0x01};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetFalseValueFromViewerLittleEndian() {
        byte[] data = {0x00};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BooleanRetriever retriever = new BooleanRetriever();

        assertFalse(retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetTrueValueFromViewerLittleEndian() {
        byte[] data = {0x01};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetFalseValueFromReaderBigEndian() {
        byte[] data = {0x00};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        BooleanRetriever retriever = new BooleanRetriever();

        assertFalse(retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetTrueValueFromReaderBigEndian() {
        byte[] data = {0x01};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetFalseValueFromReaderLittleEndian() {
        byte[] data = {0x00};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        BooleanRetriever retriever = new BooleanRetriever();

        assertFalse(retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetTrueValueFromReaderLittleEndian() {
        byte[] data = {0x01};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetNonZeroValueFromViewerReturnsTrue() {
        byte[] data = {0x02};
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetNonZeroValueFromReaderReturnsTrue() {
        byte[] data = {(byte) 0xFF};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        BooleanRetriever retriever = new BooleanRetriever();

        assertTrue(retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }
}