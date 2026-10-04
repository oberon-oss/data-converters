package eu.oberon.oss.tools.converters.binary;

import eu.oberon.oss.tools.converters.BiDirectionalConverter;
import eu.oberon.oss.tools.converters.fixed.SignedIntegerConverterProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BinaryConverter Interface Tests")
class BinaryConverterTest {

    @Test
    @DisplayName("Fixed converter provider implements BinaryConverter and BiDirectionalConverter")
    void testFixedConverterProviderAsBinaryConverter() {
        BinaryConverter<Integer> converter = new SignedIntegerConverterProvider();

        assertEquals(Integer.class, converter.getTypeClass());
        assertEquals(Integer.class, converter.getSourceType());
        assertEquals(byte[].class, converter.getTargetType());

        byte[] bytes = converter.toBytes(0x12345678, ByteOrder.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, bytes);

        int value = converter.fromBytes(bytes, ByteOrder.BIG_ENDIAN);
        assertEquals(0x12345678, value);

        // Test BiDirectionalConverter functional methods
        byte[] convertedBytes = converter.getToTargetFunction().apply(0x12345678);
        assertNotNull(convertedBytes);
        int roundTrip = converter.getToSourceFunction().apply(convertedBytes);
        assertEquals(0x12345678, roundTrip);
    }

    @Test
    @DisplayName("withByteOrder returns BiDirectionalConverter configured for specific endianness")
    void testWithByteOrder() {
        BinaryConverter<Integer> converter = new SignedIntegerConverterProvider();

        BiDirectionalConverter<Integer, byte[]> bigEndianConverter = converter.withByteOrder(ByteOrder.BIG_ENDIAN);
        byte[] beBytes = bigEndianConverter.getToTargetFunction().apply(0x01020304);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, beBytes);
        assertEquals(0x01020304, bigEndianConverter.getToSourceFunction().apply(beBytes));

        BiDirectionalConverter<Integer, byte[]> littleEndianConverter = converter.withByteOrder(ByteOrder.LITTLE_ENDIAN);
        byte[] leBytes = littleEndianConverter.getToTargetFunction().apply(0x01020304);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, leBytes);
        assertEquals(0x01020304, littleEndianConverter.getToSourceFunction().apply(leBytes));
    }
}
