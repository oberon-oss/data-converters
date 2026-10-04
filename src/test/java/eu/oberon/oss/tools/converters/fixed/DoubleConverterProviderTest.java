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

class DoubleConverterProviderTest {

    private final DoubleConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.DOUBLE.name())
    );

    private final FixedToObjectConverter<Double> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<Double> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports DOUBLE as target value type")
        void reportsCorrectValueType() {
            Assertions.assertEquals(ValueTypeNames.DOUBLE.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects an 8-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(8, provider.getExpectedByteArraySize());
        }
    }

    // ---------------------------------------------------------------------
    // Byte array size / null validation for the object converter
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Byte array size validation (bytes -> double)")
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
            byte[] sevenBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(sevenBytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("too-large array is rejected")
        void rejectsTooLargeArray() {
            byte[] nineBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(nineBytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("empty array is rejected")
        void rejectsEmptyArray() {
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(new byte[0], ByteOrder.BIG_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Double
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> double")
    class BytesToObject {

        @Test
        @DisplayName("zero: all-zero bytes decode to 0.0 in both byte orders")
        void zeroValueBothOrders() {
            byte[] zeros = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertEquals(0.0d, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0.0d, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: IEEE-754 bytes for 1.0 decode to 1.0")
        void bigEndianOneDotZero() {
            // 1.0d == 0x3FF0000000000000L
            byte[] bytes = {0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertEquals(1.0d, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: reversed IEEE-754 bytes for 1.0 decode to 1.0")
        void littleEndianOneDotZero() {
            byte[] bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xF0, 0x3F};
            assertEquals(1.0d, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("big-endian: IEEE-754 bytes for -1.0 decode to -1.0")
        void bigEndianMinusOneDotZero() {
            // -1.0d == 0xBFF0000000000000L
            byte[] bytes = {(byte) 0xBF, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertEquals(-1.0d, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("negative zero preserves its sign bit")
        void negativeZeroPreserved() {
            // -0.0d == 0x8000000000000000L
            byte[] beBytes = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            double result = toObject.convert(beBytes, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertEquals(-0.0d, result),
                    () -> assertEquals(Long.MIN_VALUE, Double.doubleToRawLongBits(result))
            );
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            double be = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            double le = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertNotEquals(Double.doubleToRawLongBits(be), Double.doubleToRawLongBits(le));
        }

        @Test
        @DisplayName("positive infinity decodes correctly")
        void positiveInfinity() {
            // +Inf == 0x7FF0000000000000L
            byte[] bytes = {0x7F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertEquals(Double.POSITIVE_INFINITY, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("negative infinity decodes correctly")
        void negativeInfinity() {
            // -Inf == 0xFFF0000000000000L
            byte[] bytes = {(byte) 0xFF, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertEquals(Double.NEGATIVE_INFINITY, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("NaN decodes to NaN")
        void notANumber() {
            // A canonical NaN == 0x7FF8000000000000L
            byte[] bytes = {0x7F, (byte) 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            double value = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            assertTrue(Double.isNaN(value));
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            double viaDefault = toObject.convert(bytes);
            double viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(Double.doubleToRawLongBits(viaNative),
                    Double.doubleToRawLongBits(viaDefault));
        }
    }

    // ---------------------------------------------------------------------
    // Double -> bytes
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("double -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("0.0 encodes to all-zero bytes in both byte orders")
        void encodesZero() {
            byte[] expected = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(0.0d, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(0.0d, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 1.0 encodes to IEEE-754 bytes")
        void encodesOneBigEndian() {
            byte[] expected = {0x3F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(1.0d, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 1.0 encodes to reversed IEEE-754 bytes")
        void encodesOneLittleEndian() {
            byte[] expected = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xF0, 0x3F};
            assertArrayEquals(expected, toBytes.convert(1.0d, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("big-endian: -1.0 encodes to IEEE-754 bytes")
        void encodesMinusOneBigEndian() {
            byte[] expected = {(byte) 0xBF, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(-1.0d, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("negative zero encodes with its sign bit set")
        void encodesNegativeZero() {
            byte[] expected = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(-0.0d, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("positive infinity encodes to the IEEE-754 +Inf bit pattern")
        void encodesPositiveInfinity() {
            byte[] expected = {0x7F, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(Double.POSITIVE_INFINITY, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("negative infinity encodes to the IEEE-754 -Inf bit pattern")
        void encodesNegativeInfinity() {
            byte[] expected = {(byte) 0xFF, (byte) 0xF0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(Double.NEGATIVE_INFINITY, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("NaN encodes to the canonical IEEE-754 NaN bit pattern")
        void encodesNaN() {
            // Double.doubleToLongBits collapses NaN to the canonical NaN 0x7FF8000000000000L
            byte[] expected = {0x7F, (byte) 0xF8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertArrayEquals(expected, toBytes.convert(Double.NaN, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("output length is always the expected 8 bytes")
        void outputAlwaysEightBytes() {
            assertAll(
                    () -> assertEquals(8, toBytes.convert(0.0d, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(Double.MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(Double.MIN_VALUE).length),
                    () -> assertEquals(8, toBytes.convert(-1.234e300d).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            double value = 1234.5678d;
            assertArrayEquals(
                    toBytes.convert(value, ByteOrder.nativeOrder()),
                    toBytes.convert(value)
            );
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value (both endiannesses)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip double -> bytes -> double")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(0.0d),
                    Arguments.of(-0.0d),
                    Arguments.of(1.0d),
                    Arguments.of(-1.0d),
                    Arguments.of(Math.PI),
                    Arguments.of(Math.E),
                    Arguments.of(1234.5678d),
                    Arguments.of(-9876.54321d),
                    Arguments.of(1.7976931348623157e308d), // near Double.MAX_VALUE
                    Arguments.of(Double.MIN_VALUE),
                    Arguments.of(Double.MAX_VALUE),
                    Arguments.of(-Double.MAX_VALUE),
                    Arguments.of(Double.MIN_NORMAL),
                    Arguments.of(Double.POSITIVE_INFINITY),
                    Arguments.of(Double.NEGATIVE_INFINITY)
            );
        }

        @ParameterizedTest(name = "big-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void bigEndianRoundTrip(double value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            assertNotNull(bytes);
            assertEquals(8, bytes.length);
            double decoded = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            // Compare via raw bits to correctly handle -0.0 vs. 0.0
            assertEquals(Double.doubleToRawLongBits(value),
                    Double.doubleToRawLongBits(decoded));
        }

        @ParameterizedTest(name = "little-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void littleEndianRoundTrip(double value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertNotNull(bytes);
            assertEquals(8, bytes.length);
            double decoded = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertEquals(Double.doubleToRawLongBits(value),
                    Double.doubleToRawLongBits(decoded));
        }

        @Test
        @DisplayName("NaN round-trips (both orders)")
        void nanRoundTrip() {
            byte[] be = toBytes.convert(Double.NaN, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(Double.NaN, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertTrue(Double.isNaN(toObject.convert(be, ByteOrder.BIG_ENDIAN))),
                    () -> assertTrue(Double.isNaN(toObject.convert(le, ByteOrder.LITTLE_ENDIAN)))
            );
        }

        @Test
        @DisplayName("BE and LE encodings of the same value are exact byte-reverses of each other")
        void beAndLeAreByteReversed() {
            double value = 1234.5678d;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[7], le[6], le[5], le[4], le[3], le[2], le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }
}