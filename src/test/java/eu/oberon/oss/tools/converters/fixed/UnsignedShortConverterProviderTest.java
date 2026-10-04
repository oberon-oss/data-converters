package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
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

class UnsignedShortConverterProviderTest {

    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = UnsignedShortConverterProvider.MAX_UNSIGNED_SHORT_VALUE; // 0xFFFF

    private final UnsignedShortConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.UNSIGNED_SHORT.name())
    );
    private final FixedToObjectConverter<Integer> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<Integer> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports UNSIGNED_SHORT as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.UNSIGNED_SHORT.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 2-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(2, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("MAX_UNSIGNED_SHORT_VALUE equals 0xFFFF")
        void maxUnsignedShortConstant() {
            assertEquals(0xFFFF, MAX_VALUE);
        }
    }

    // ---------------------------------------------------------------------
    // Byte array size / null validation for the object converter
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Byte array size validation (bytes -> int)")
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
            byte[] oneByte = {0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(oneByte, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("too-large array is rejected")
        void rejectsTooLargeArray() {
            byte[] threeBytes = {0x00, 0x00, 0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(threeBytes, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Integer (min / max / byte order)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> unsigned short (Integer)")
    class BytesToObject {

        @Test
        @DisplayName("min value: all-zero bytes decode to 0 in both byte orders")
        void minValueBothOrders() {
            byte[] zeros = {0x00, 0x00};
            assertAll(
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value: all-0xFF bytes decode to 0xFFFF in both byte orders")
        void maxValueBothOrders() {
            byte[] ones = {(byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x12,0x34} decodes to 0x1234")
        void bigEndianOrder() {
            byte[] bytes = {0x12, 0x34};
            assertEquals(0x1234, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x12,0x34} decodes to 0x3412")
        void littleEndianOrder() {
            byte[] bytes = {0x12, 0x34};
            assertEquals(0x3412, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x00, 0x01};
            int be = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            int le = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals(0x0001, be),
                    () -> assertEquals(0x0100, le)
            );
        }

        @Test
        @DisplayName("high bit only: {0x80,0x00} BE=0x8000, LE=0x0080")
        void highBitDoesNotSignExtend() {
            byte[] bytes = {(byte) 0x80, 0x00};
            assertAll(
                    () -> assertEquals(0x8000, toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0x0080, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x42, 0x42};
            int be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            int le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x12, 0x34};
            int viaDefault = toObject.convert(bytes);
            int viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // Integer -> bytes (min / max / range check / byte order)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("unsigned short (Integer) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("min value 0 encodes to all-zero bytes in both byte orders")
        void encodesMinValue() {
            byte[] expected = {0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value 0xFFFF encodes to all-0xFF bytes in both byte orders")
        void encodesMaxValue() {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x1234 encodes to {0x12,0x34}")
        void encodesBigEndian() {
            byte[] expected = {0x12, 0x34};
            assertArrayEquals(expected, toBytes.convert(0x1234, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x1234 encodes to {0x34,0x12}")
        void encodesLittleEndian() {
            byte[] expected = {0x34, 0x12};
            assertArrayEquals(expected, toBytes.convert(0x1234, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("high-bit value 0x8000 is encoded correctly in both byte orders")
        void encodesHighBitValue() {
            int value = 0x8000;
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80, 0x00},
                            toBytes.convert(value, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x00, (byte) 0x80},
                            toBytes.convert(value, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("output length is always the expected 2 bytes")
        void outputAlwaysTwoBytes() {
            assertAll(
                    () -> assertEquals(2, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(2, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(2, toBytes.convert(0x1234).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            int value = 0x1234;
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
                            () -> toBytes.convert(-1, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(Integer.MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("value > 0xFFFF is rejected as out of unsigned range")
        void rejectsValueAboveMax() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(MAX_VALUE + 1, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(Integer.MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value (both endiannesses)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip int -> bytes -> int")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(MIN_VALUE),
                    Arguments.of(1),
                    Arguments.of(0x00FF),
                    Arguments.of(0x0100),
                    Arguments.of(0x1234),
                    Arguments.of(0x7FFF), // Short.MAX_VALUE
                    Arguments.of(0x8000), // sign-bit-boundary for signed short
                    Arguments.of(0xFF00),
                    Arguments.of(MAX_VALUE)
            );
        }

        @ParameterizedTest(name = "big-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void bigEndianRoundTrip(int value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "little-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void littleEndianRoundTrip(int value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("BE and LE encodings of the same value are exact byte-reverses of each other")
        void beAndLeAreByteReversed() {
            int value = 0x1234;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }
}