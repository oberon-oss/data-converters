package eu.oberon.oss.tools.retrievers.text;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberon.oss.tools.retrievers.varlen.text.StringValueRetriever;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringValueRetrieverTest {
    @Test
    void testGetValueFromViewer() {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(viewer, 0, 5));
        assertEquals("World", retriever.getValue(viewer, 6, 5));
    }

    @Test
    void testGetValueFromViewerWithCharset() {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_16BE);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(viewer, 0, data.length, StandardCharsets.UTF_16BE));
    }

    @Test
    void testGetValueFromViewerWithCharsetAndByteOrder() {
        // UTF-16LE bytes for "Hello" without BOM
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_16LE);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        StringValueRetriever retriever = new StringValueRetriever();

        // If we use UTF-16 and LITTLE_ENDIAN, AbstractTextConverterProvider should handle it
        assertEquals("Hello", retriever.getValue(viewer, 0, data.length, ByteOrder.LITTLE_ENDIAN, StandardCharsets.UTF_16));
    }

    @Test
    void testGetValueFromViewerWithByteOrder() {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(viewer, 0, 5, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromReader() {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BinaryDataReader reader = viewer.getReader();
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(reader, 5));
        reader.readByte(); // skip space
        assertEquals("World", retriever.getValue(reader, 5));
    }

    @Test
    void testGetValueFromReaderWithCharset() {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_16LE);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BinaryDataReader reader = viewer.getReader();
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(reader, data.length, StandardCharsets.UTF_16LE));
    }

    @Test
    void testGetValueFromReaderWithByteOrder() {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        BinaryDataReader reader = viewer.getReader();
        StringValueRetriever retriever = new StringValueRetriever();

        assertEquals("Hello", retriever.getValue(reader, 5, ByteOrder.BIG_ENDIAN));
    }
}
