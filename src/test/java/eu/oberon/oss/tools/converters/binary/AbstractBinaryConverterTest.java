package eu.oberon.oss.tools.converters.binary;

import eu.oberon.oss.tools.converters.BiDirectionalConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AbstractBinaryConverter Tests")
class AbstractBinaryConverterTest {

    private static class TestIntBinaryConverter extends AbstractBinaryConverter<Integer> {
        public TestIntBinaryConverter() {
            super(
                    Integer.class,
                    (val, order) -> ByteBuffer.allocate(4).order(order).putInt(val).array(),
                    (bytes, order) -> ByteBuffer.wrap(bytes).order(order).getInt()
            );
        }

        public TestIntBinaryConverter(
                Class<Integer> typeClass,
                BiFunction<Integer, ByteOrder, byte[]> toBytes,
                BiFunction<byte[], ByteOrder, Integer> fromBytes) {
            super(typeClass, toBytes, fromBytes);
        }
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when required parameters are null")
    void testConstructorNullChecks() {
        BiFunction<Integer, ByteOrder, byte[]> toBytes = (val, order) -> new byte[4];
        BiFunction<byte[], ByteOrder, Integer> fromBytes = (bytes, order) -> 0;

        NullPointerException exType = assertThrows(NullPointerException.class, () ->
                new TestIntBinaryConverter(null, toBytes, fromBytes));
        assertEquals("Parameter: typeClass", exType.getMessage());

        NullPointerException exToBytes = assertThrows(NullPointerException.class, () ->
                new TestIntBinaryConverter(Integer.class, null, fromBytes));
        assertEquals("Parameter: toBytesWithOrder", exToBytes.getMessage());

        NullPointerException exFromBytes = assertThrows(NullPointerException.class, () ->
                new TestIntBinaryConverter(Integer.class, toBytes, null));
        assertEquals("Parameter: fromBytesWithOrder", exFromBytes.getMessage());
    }

    @Test
    @DisplayName("Type getters return expected class types")
    void testTypeGetters() {
        TestIntBinaryConverter converter = new TestIntBinaryConverter();

        assertEquals(Integer.class, converter.getTypeClass());
        assertEquals(Integer.class, converter.getSourceType());
        assertEquals(byte[].class, converter.getTargetType());
    }

    @Test
    @DisplayName("Conversions with explicit ByteOrder function properly")
    void testConversionsWithByteOrder() {
        TestIntBinaryConverter converter = new TestIntBinaryConverter();
        int value = 0x11223344;

        byte[] bigEndianBytes = converter.toBytes(value, ByteOrder.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0x11, 0x22, 0x33, 0x44}, bigEndianBytes);
        assertEquals(value, converter.fromBytes(bigEndianBytes, ByteOrder.BIG_ENDIAN));

        byte[] littleEndianBytes = converter.toBytes(value, ByteOrder.LITTLE_ENDIAN);
        assertArrayEquals(new byte[]{0x44, 0x33, 0x22, 0x11}, littleEndianBytes);
        assertEquals(value, converter.fromBytes(littleEndianBytes, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    @DisplayName("Conversions without explicit ByteOrder use native byte order")
    void testConversionsWithNativeByteOrder() {
        TestIntBinaryConverter converter = new TestIntBinaryConverter();
        int value = 0x11223344;

        byte[] expectedNativeBytes = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(value).array();

        byte[] nativeBytes = converter.toBytes(value);
        assertArrayEquals(expectedNativeBytes, nativeBytes);
        assertEquals(value, converter.fromBytes(nativeBytes));
    }

    @Test
    @DisplayName("BiDirectionalConverter functional methods delegate correctly")
    void testBidirectionalFunctionalMethods() {
        TestIntBinaryConverter converter = new TestIntBinaryConverter();
        int value = 0x01020304;

        byte[] convertedBytes = converter.getToTargetFunction().apply(value);
        assertNotNull(convertedBytes);

        int roundTrip = converter.getToSourceFunction().apply(convertedBytes);
        assertEquals(value, roundTrip);
    }

    @Test
    @DisplayName("withByteOrder returns BiDirectionalConverter configured for specific ByteOrder")
    void testWithByteOrder() {
        TestIntBinaryConverter converter = new TestIntBinaryConverter();
        int value = 0x01020304;

        BiDirectionalConverter<Integer, byte[]> beConverter = converter.withByteOrder(ByteOrder.BIG_ENDIAN);
        byte[] beBytes = beConverter.getToTargetFunction().apply(value);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, beBytes);
        assertEquals(value, beConverter.getToSourceFunction().apply(beBytes));

        BiDirectionalConverter<Integer, byte[]> leConverter = converter.withByteOrder(ByteOrder.LITTLE_ENDIAN);
        byte[] leBytes = leConverter.getToTargetFunction().apply(value);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, leBytes);
        assertEquals(value, leConverter.getToSourceFunction().apply(leBytes));
    }
}
