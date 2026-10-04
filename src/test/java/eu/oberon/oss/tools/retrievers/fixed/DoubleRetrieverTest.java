package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.DoubleConverterProvider;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleRetrieverTest {

    @Test
    void testGetValueFromViewerBigEndian() {
        double value = 1.0d;
        DoubleConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("DOUBLE"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.BIG_ENDIAN);

        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        DoubleRetriever retriever = new DoubleRetriever();

        assertEquals(value, retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromViewerLittleEndian() {
        double value = 1.0d;
        DoubleConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("DOUBLE"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.LITTLE_ENDIAN);

        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        DoubleRetriever retriever = new DoubleRetriever();

        assertEquals(value, retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetValueFromReaderBigEndian() {
        double value = 1.0d;
        DoubleConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("DOUBLE"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.BIG_ENDIAN);

        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        DoubleRetriever retriever = new DoubleRetriever();

        assertEquals(value, retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromReaderLittleEndian() {
        double value = 1.0d;
        DoubleConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("DOUBLE"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.LITTLE_ENDIAN);

        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        DoubleRetriever retriever = new DoubleRetriever();

        assertEquals(value, retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }
}
