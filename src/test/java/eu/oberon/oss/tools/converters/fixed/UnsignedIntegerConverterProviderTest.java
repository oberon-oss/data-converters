package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnsignedIntegerConverterProviderTest {

    private static final long MIN_VALUE = 0L;
    private static final long MAX_VALUE = UnsignedIntegerConverterProvider.MAX_UNSIGNED_INT_VALUE; // 0xFFFF_FFFFL

    private final UnsignedIntegerConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.UNSIGNED_INTEGER.name())
    );
    private final FixedToObjectConverter<Long> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<Long> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports UNSIGNED_INTEGER as target value type")
        void reportsCorrectValueType() {
            Assertions.assertEquals(ValueTypeNames.UNSIGNED_INTEGER.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 4-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(4, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("MAX_UNSIGNED_INT_VALUE equals 0xFFFF_FFFFL")
        void maxUnsignedIntConstant() {
            assertEquals(0xFFFF_FFFFL, MAX_VALUE);
        }
    }

    // ---------------------------------------------------------------------
    // Byte array size / null validation for the object converter
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Byte array size validation (bytes -> long)")
    class InputValidation {

        @Test
        @DisplayName("null byte array is rejected")
        void rejectsNullArray() {
            assertThrows(NullPointerException.class, () -> toObject.convert(null));
            assertThrows(NullPointerException.class, () -> toObject.convert(null, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("too-small array is rejected")
        void rejectsTooSmallArray() {
            byte[] threeBytes = {0x00, 0x00, 0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(threeBytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("too-large array is rejected")
        void rejectsTooLargeArray() {
            byte[] fiveBytes = {0x00, 0x00, 0x00, 0x00, 0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(fiveBytes, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Long (min / max / byte order)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> unsigned int (Long)")
    class BytesToObject {

        @Test
        @DisplayName("min value: all-zero bytes decode to 0 in both byte orders")
        void minValueBothOrders() {
            byte[] zeros = {0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value: all-0xFF bytes decode to 0xFFFF_FFFFL in both byte orders")
        void maxValueBothOrders() {
            byte[] ones = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x12,0x34,0x56,0x78} decodes to 0x12345678")
        void bigEndianOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            assertEquals(0x12345678L, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x12,0x34,0x56,0x78} decodes to 0x78563412")
        void littleEndianOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            assertEquals(0x78563412L, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x00, 0x00, 0x00, 0x01};
            long be = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            long le = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals(0x0000_0001L, be),
                    () -> assertEquals(0x0100_0000L, le)
            );
        }

        @Test
        @DisplayName("high bit only: {0x80,0x00,0x00,0x00} BE=0x80000000L, LE=0x00000080L")
        void highBitDoesNotSignExtend() {
            byte[] bytes = {(byte) 0x80, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertEquals(0x8000_0000L, toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0x0000_0080L, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x12, 0x34, 0x34, 0x12};
            long be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            long le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            long viaDefault = toObject.convert(bytes);
            long viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // Long -> bytes (min / max / range check / byte order)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("unsigned int (Long) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("min value 0 encodes to all-zero bytes in both byte orders")
        void encodesMinValue() {
            byte[] expected = {0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value 0xFFFF_FFFFL encodes to all-0xFF bytes in both byte orders")
        void encodesMaxValue() {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x12345678 encodes to {0x12,0x34,0x56,0x78}")
        void encodesBigEndian() {
            byte[] expected = {0x12, 0x34, 0x56, 0x78};
            assertArrayEquals(expected, toBytes.convert(0x12345678L, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x12345678 encodes to {0x78,0x56,0x34,0x12}")
        void encodesLittleEndian() {
            byte[] expected = {0x78, 0x56, 0x34, 0x12};
            assertArrayEquals(expected, toBytes.convert(0x12345678L, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("high-bit value 0x80000000L is encoded correctly in both byte orders")
        void encodesHighBitValue() {
            long value = 0x8000_0000L;
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80, 0x00, 0x00, 0x00},
                            toBytes.convert(value, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x00, 0x00, 0x00, (byte) 0x80},
                            toBytes.convert(value, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("output length is always the expected 4 bytes")
        void outputAlwaysFourBytes() {
            assertAll(
                    () -> assertEquals(4, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(4, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(4, toBytes.convert(0x1234_5678L).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            long value = 0x1234_5678L;
            assertArrayEquals(
                    toBytes.convert(value, ByteOrder.nativeOrder()),
                    toBytes.convert(value)
            );
        }

        @Test
        @DisplayName("value < 0 is rejected as out of unsigned range")
        void rejectsNegativeValue() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(-1L, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(Long.MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("value > 0xFFFF_FFFFL is rejected as out of unsigned range")
        void rejectsValueAboveMax() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(MAX_VALUE + 1, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(Long.MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value (both endiannesses)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip long -> bytes -> long")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(MIN_VALUE),
                    Arguments.of(1L),
                    Arguments.of(0x0000_00FFL),
                    Arguments.of(0x0000_FF00L),
                    Arguments.of(0x00FF_0000L),
                    Arguments.of(0xFF00_0000L),
                    Arguments.of(0x1234_5678L),
                    Arguments.of(0x8000_0000L), // sign-bit-boundary for signed int
                    Arguments.of(0x7FFF_FFFFL), // Integer.MAX_VALUE
                    Arguments.of(MAX_VALUE)
            );
        }

        @ParameterizedTest(name = "big-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void bigEndianRoundTrip(long value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "little-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void littleEndianRoundTrip(long value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("BE and LE encodings of the same value are exact byte-reverses of each other")
        void beAndLeAreByteReversed() {
            long value = 0x1234_5678L;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[3], le[2], le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }
}