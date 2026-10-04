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

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnsignedLongConverterProviderTest {

    private static final BigInteger MIN_VALUE = BigInteger.ZERO;
    /* 2^64 - 1 = 18_446_744_073_709_551_615 */

    private static final BigInteger MAX_VALUE = new BigInteger("18446744073709551615");
    /* 2^63 = 9_223_372_036_854_775_808 — the first value whose top bit is set */
    private static final BigInteger HIGH_BIT = BigInteger.ONE.shiftLeft(63);

    /* 2^64 — the first value that must be rejected as too large */
    private static final BigInteger OVERFLOW = BigInteger.ONE.shiftLeft(64);

    // init
    private final UnsignedLongConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.UNSIGNED_LONG.name())
    );
    private final FixedToObjectConverter<BigInteger> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<BigInteger> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports UNSIGNED_LONG as target value type")
        void reportsCorrectValueType() {
            Assertions.assertEquals(ValueTypeNames.UNSIGNED_LONG.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects an 8-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(8, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("MAX_VALUE equals 2^64 - 1")
        void maxUnsignedLongConstant() {
            assertEquals(MAX_VALUE, BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE));
        }
    }

    // ---------------------------------------------------------------------
    // Byte array size / null validation for the object converter
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Byte array size validation (bytes -> BigInteger)")
    class InputValidation {

        @Test
        @DisplayName("null byte array is rejected")
        void rejectsNullArray() {
            assertThrows(NullPointerException.class,
                    () -> toObject.convert(null, ByteOrder.BIG_ENDIAN));
            // The no-arg overload delegates to the two-arg overload, which validates the array.
            assertThrows(NullPointerException.class,
                    () -> toObject.convert(null));
        }

        @Test
        @DisplayName("too-small array is rejected")
        void rejectsTooSmallArray() {
            byte[] sevenBytes = {0, 0, 0, 0, 0, 0, 0};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(sevenBytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("too-large array is rejected")
        void rejectsTooLargeArray() {
            byte[] nineBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(nineBytes, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> BigInteger (min / max / byte order / no sign extension)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> unsigned long (BigInteger)")
    class BytesToObject {

        @Test
        @DisplayName("min value: all-zero bytes decode to 0 in both byte orders")
        void minValueBothOrders() {
            byte[] zeros = new byte[8];
            assertAll(
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MIN_VALUE, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value: all-0xFF bytes decode to 2^64 - 1 in both byte orders")
        void maxValueBothOrders() {
            byte[] ones = new byte[8];
            for (int i = 0; i < 8; i++) {
                ones[i] = (byte) 0xFF;
            }
            assertAll(
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(MAX_VALUE, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x01..0x08} decodes to 0x0102030405060708")
        void bigEndianOrder() {
            byte[] bytes = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            assertEquals(new BigInteger("0102030405060708", 16),
                    toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x01..0x08} decodes to 0x0807060504030201")
        void littleEndianOrder() {
            byte[] bytes = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            assertEquals(new BigInteger("0807060504030201", 16),
                    toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 0x01};
            BigInteger be = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            BigInteger le = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals(BigInteger.ONE, be),
                    () -> assertEquals(BigInteger.ONE.shiftLeft(56), le)
            );
        }

        @Test
        @DisplayName("high bit only: {0x80,0,0,0,0,0,0,0} BE = 2^63, LE = 2^7 (no sign extension)")
        void highBitDoesNotSignExtend() {
            byte[] bytes = {(byte) 0x80, 0, 0, 0, 0, 0, 0, 0};
            assertAll(
                    () -> assertEquals(BigInteger.ONE.shiftLeft(63),
                            toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(BigInteger.valueOf(0x80),
                            toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("all-0xFF bytes decode as positive (unsigned), not as -1")
        void decodesAsUnsigned() {
            byte[] ones = new byte[8];
            for (int i = 0; i < 8; i++) {
                ones[i] = (byte) 0xFF;
            }
            BigInteger decoded = toObject.convert(ones, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertEquals(1, decoded.signum(),
                            "decoded value must be strictly positive"),
                    () -> assertEquals(MAX_VALUE, decoded)
            );
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12};
            BigInteger be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            BigInteger le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            BigInteger viaDefault = toObject.convert(bytes);
            BigInteger viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // BigInteger -> bytes (min / max / range check / byte order)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("unsigned long (BigInteger) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("min value 0 encodes to all-zero bytes in both byte orders")
        void encodesMinValue() {
            byte[] expected = new byte[8];
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("max value 2^64 - 1 encodes to all-0xFF bytes in both byte orders")
        void encodesMaxValue() {
            byte[] expected = new byte[8];
            for (int i = 0; i < 8; i++) {
                expected[i] = (byte) 0xFF;
            }
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x0102030405060708 encodes to {0x01..0x08}")
        void encodesBigEndian() {
            byte[] expected = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
            assertArrayEquals(expected,
                    toBytes.convert(new BigInteger("0102030405060708", 16), ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x0102030405060708 encodes to {0x08..0x01}")
        void encodesLittleEndian() {
            byte[] expected = {0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01};
            assertArrayEquals(expected,
                    toBytes.convert(new BigInteger("0102030405060708", 16), ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("high-bit value 2^63 is encoded correctly in both byte orders")
        void encodesHighBitValue() {
            assertAll(
                    () -> assertArrayEquals(
                            new byte[]{(byte) 0x80, 0, 0, 0, 0, 0, 0, 0},
                            toBytes.convert(HIGH_BIT, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(
                            new byte[]{0, 0, 0, 0, 0, 0, 0, (byte) 0x80},
                            toBytes.convert(HIGH_BIT, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("small values are left-padded with zeros (no leading garbage)")
        void encodesSmallValueWithPadding() {
            assertAll(
                    () -> assertArrayEquals(
                            new byte[]{0, 0, 0, 0, 0, 0, 0, 0x01},
                            toBytes.convert(BigInteger.ONE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(
                            new byte[]{0x01, 0, 0, 0, 0, 0, 0, 0},
                            toBytes.convert(BigInteger.ONE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("output length is always the expected 8 bytes")
        void outputAlwaysEightBytes() {
            assertAll(
                    () -> assertEquals(8, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(HIGH_BIT).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            BigInteger value = new BigInteger("0102030405060708", 16);
            assertArrayEquals(
                    toBytes.convert(value, ByteOrder.nativeOrder()),
                    toBytes.convert(value)
            );
        }

        @Test
        @DisplayName("value < 0 is rejected as out of unsigned range")
        void rejectsNegativeValue() {
            BigInteger negativeOne = BigInteger.ONE.negate();
            BigInteger negativeMax = MAX_VALUE.negate();
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(negativeOne, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(negativeMax, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("value > 2^64 - 1 is rejected as out of unsigned range")
        void rejectsValueAboveMax() {
            BigInteger aboveMax = MAX_VALUE.add(BigInteger.ONE);
            BigInteger overflowShifted = OVERFLOW.shiftLeft(64);
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(aboveMax, ByteOrder.BIG_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(OVERFLOW, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> toBytes.convert(overflowShifted, ByteOrder.BIG_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value (both endiannesses)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip BigInteger -> bytes -> BigInteger")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(MIN_VALUE),
                    Arguments.of(BigInteger.ONE),
                    Arguments.of(BigInteger.valueOf(0xFFL)),
                    Arguments.of(BigInteger.valueOf(0xFF00L)),
                    Arguments.of(BigInteger.valueOf(0xFF_0000L)),
                    Arguments.of(BigInteger.valueOf(0xFF00_0000L)),
                    Arguments.of(new BigInteger("00000000FF000000", 16)),
                    Arguments.of(new BigInteger("0102030405060708", 16)),
                    Arguments.of(BigInteger.valueOf(Long.MAX_VALUE)),          // 2^63 - 1
                    Arguments.of(HIGH_BIT),                                    // 2^63 (top bit set)
                    Arguments.of(HIGH_BIT.add(BigInteger.ONE)),                // 2^63 + 1
                    Arguments.of(MAX_VALUE.subtract(BigInteger.ONE)),
                    Arguments.of(MAX_VALUE)
            );
        }

        @ParameterizedTest(name = "big-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void bigEndianRoundTrip(BigInteger value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            assertNotNull(bytes);
            assertEquals(8, bytes.length);
            assertEquals(value, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "little-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void littleEndianRoundTrip(BigInteger value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertNotNull(bytes);
            assertEquals(8, bytes.length);
            assertEquals(value, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("BE and LE encodings of the same value are exact byte-reverses of each other")
        void beAndLeAreByteReversed() {
            BigInteger value = new BigInteger("0102030405060708", 16);
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = new byte[8];
            for (int i = 0; i < 8; i++) {
                reversedLe[i] = le[7 - i];
            }
            assertArrayEquals(be, reversedLe);
        }

        @Test
        @DisplayName("values above 2^63 round-trip correctly (validates 'signum=1' path)")
        void unsignedTopBitRoundTrip() {
            BigInteger value = MAX_VALUE.subtract(BigInteger.valueOf(42));
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals(value, toObject.convert(be, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(value, toObject.convert(le, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertEquals(1, toObject.convert(be, ByteOrder.BIG_ENDIAN).signum())
            );
        }
    }
}