package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.FloatConvertProvider;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FloatRetrieverTest {

    @Test
    void testGetValueFromViewerBigEndian() {
        float value = 1.0f;
        FloatConvertProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("FLOAT"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.BIG_ENDIAN);

        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        FloatRetriever retriever = new FloatRetriever();

        assertEquals(value, retriever.getValue(viewer, 0, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromViewerLittleEndian() {
        float value = 1.0f;
        FloatConvertProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("FLOAT"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.LITTLE_ENDIAN);

        BinaryDataViewer viewer = new BinaryDataViewerImpl(data);
        FloatRetriever retriever = new FloatRetriever();

        assertEquals(value, retriever.getValue(viewer, 0, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    void testGetValueFromReaderBigEndian() {
        float value = 1.0f;
        FloatConvertProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("FLOAT"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.BIG_ENDIAN);

        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        FloatRetriever retriever = new FloatRetriever();

        assertEquals(value, retriever.getValue(reader, ByteOrder.BIG_ENDIAN));
    }

    @Test
    void testGetValueFromReaderLittleEndian() {
        float value = 1.0f;
        FloatConvertProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider("FLOAT"));
        byte[] data = provider.getToByteConverter().convert(value, ByteOrder.LITTLE_ENDIAN);

        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));
        FloatRetriever retriever = new FloatRetriever();

        assertEquals(value, retriever.getValue(reader, ByteOrder.LITTLE_ENDIAN));
    }
}
