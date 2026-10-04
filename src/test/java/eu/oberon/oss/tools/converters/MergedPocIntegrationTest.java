package eu.oberon.oss.tools.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;
import eu.oberon.oss.tools.converters.string.Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Merged Project POC Integration Tests")
class MergedPocIntegrationTest {

    @Test
    @DisplayName("Complete end-to-end flow: Object -> Binary (BiDirectionalConverter) -> Reader -> Object -> String (Converter)")
    void testEndToEndConversionPipeline() {
        // 1. Registries
        BiDirectionalConvertersRegistry biDirectionalRegistry = new BiDirectionalConvertersRegistry();
        ConvertersRegistry stringConvertersRegistry = new ConvertersRegistry();
        BinaryConvertersRegistry binaryConvertersRegistry = new BinaryConvertersRegistry(biDirectionalRegistry);

        // 2. Convert Integer to byte[] using BiDirectionalConverter / BinaryConverter
        BinaryConverter<Integer> intBinaryConverter = binaryConvertersRegistry.getConverterForClassType(Integer.class);
        assertNotNull(intBinaryConverter, "Binary converter for Integer should be discovered");

        int testInteger = 123456789;
        byte[] binaryData = intBinaryConverter.toBytes(testInteger, ByteOrder.BIG_ENDIAN);
        assertEquals(4, binaryData.length);

        // 3. Inspect binary data with BinaryDataReader / BinaryDataViewer
        BinaryDataViewer viewer = new BinaryDataViewerImpl(binaryData);
        BinaryDataReader reader = new BinaryDataReaderImpl(viewer);

        // Peek and read using BiDirectionalConverter directly from reader
        Integer peekedValue = viewer.peek(intBinaryConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 0, 4);
        assertEquals(testInteger, peekedValue);

        Integer readValue = reader.read(intBinaryConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 4);
        assertEquals(testInteger, readValue);
        assertEquals(0, reader.remaining());

        // 4. Convert retrieved Integer to String using String Converter<Integer>
        Converter<Integer> stringConverter = stringConvertersRegistry.getConverterForClassType(Integer.class);
        assertNotNull(stringConverter, "String converter for Integer should be discovered");

        String stringRepresentation = stringConverter.convertToString().apply(readValue);
        assertEquals("123456789", stringRepresentation);

        Integer fromStringValue = stringConverter.convertFromString().apply(stringRepresentation);
        assertEquals(testInteger, fromStringValue);
    }

    @Test
    @DisplayName("Composite binary packet decoding with mixed types via BiDirectionalConverters")
    void testCompositeBinaryPacketReading() {
        BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry();

        BinaryConverter<Integer> intConverter = binaryRegistry.getConverterForValueType(ValueTypeNames.SIGNED_INTEGER);
        BinaryConverter<Long> longConverter = binaryRegistry.getConverterForValueType(ValueTypeNames.SIGNED_LONG);
        BinaryConverter<Boolean> boolConverter = binaryRegistry.getConverterForValueType(ValueTypeNames.BOOLEAN);

        assertNotNull(intConverter);
        assertNotNull(longConverter);
        assertNotNull(boolConverter);

        // Build composite binary packet: [4-byte int][8-byte long][1-byte bool]
        byte[] intBytes = intConverter.toBytes(42, ByteOrder.BIG_ENDIAN);
        byte[] longBytes = longConverter.toBytes(100000000000L, ByteOrder.BIG_ENDIAN);
        byte[] boolBytes = boolConverter.toBytes(true);

        byte[] packet = new byte[intBytes.length + longBytes.length + boolBytes.length];
        System.arraycopy(intBytes, 0, packet, 0, 4);
        System.arraycopy(longBytes, 0, packet, 4, 8);
        System.arraycopy(boolBytes, 0, packet, 12, 1);

        // Read sequentially via BinaryDataReader
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(packet));
        int readInt = reader.read(intConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 4);
        long readLong = reader.read(longConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 8);
        boolean readBool = reader.read(boolConverter, 1);

        assertEquals(42, readInt);
        assertEquals(100000000000L, readLong);
        assertTrue(readBool);
        assertEquals(0, reader.remaining());
    }
}
