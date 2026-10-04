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

class SignedIntegerConverterProviderTest {

    private static final int MIN_VALUE = Integer.MIN_VALUE;
    private static final int MAX_VALUE = Integer.MAX_VALUE;

    private final SignedIntegerConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.SIGNED_INTEGER.name())
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
        @DisplayName("reports a non-null value type name")
        void reportsValueType() {
            assertNotNull(provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 4-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(4, provider.getExpectedByteArraySize());
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
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> toObject.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(null, ByteOrder.BIG_ENDIAN))
            );
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

        @Test
        @DisplayName("empty array is rejected")
        void rejectsEmptyArray() {
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(new byte[0], ByteOrder.BIG_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Integer (min / max / byte order / sign-extension)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> signed int (Integer)")
    class BytesToObject {

        @Test
        @DisplayName("all-zero bytes decode to 0 in both byte orders")
        void zeroBothOrders() {
            byte[] zeros = {0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertEquals(0, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("all-0xFF bytes decode to -1 in both byte orders")
        void allOnesDecodeToMinusOne() {
            byte[] ones = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertEquals(-1, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(-1, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x12,0x34,0x56,0x78} decodes to 0x12345678")
        void bigEndianOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            assertEquals(0x12345678, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x12,0x34,0x56,0x78} decodes to 0x78563412")
        void littleEndianOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            assertEquals(0x78563412, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x00, 0x00, 0x00, 0x01};
            assertAll(
                    () -> assertEquals(0x0000_0001, toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0x0100_0000, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("high bit set is interpreted as a negative value (Integer.MIN_VALUE) in BE")
        void highBitIsNegativeBigEndian() {
            byte[] bytes = {(byte) 0x80, 0x00, 0x00, 0x00};
            assertEquals(Integer.MIN_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("high bit set in the last LE byte is interpreted as Integer.MIN_VALUE in LE")
        void highBitIsNegativeLittleEndian() {
            byte[] bytes = {0x00, 0x00, 0x00, (byte) 0x80};
            assertEquals(Integer.MIN_VALUE, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("Integer.MAX_VALUE encoded as BE bytes decodes back to Integer.MAX_VALUE")
        void maxIntBigEndian() {
            byte[] bytes = {(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            assertEquals(MAX_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x12, 0x34, 0x34, 0x12};
            int be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            int le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            int viaDefault = toObject.convert(bytes);
            int viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // Integer -> bytes (min / max / byte order / negative values)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("signed int (Integer) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("0 encodes to all-zero bytes in both byte orders")
        void encodesZero() {
            byte[] expected = {0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(0, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(0, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("-1 encodes to all-0xFF bytes in both byte orders")
        void encodesMinusOne() {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(-1, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(-1, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x12345678 encodes to {0x12,0x34,0x56,0x78}")
        void encodesBigEndian() {
            byte[] expected = {0x12, 0x34, 0x56, 0x78};
            assertArrayEquals(expected, toBytes.convert(0x12345678, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x12345678 encodes to {0x78,0x56,0x34,0x12}")
        void encodesLittleEndian() {
            byte[] expected = {0x78, 0x56, 0x34, 0x12};
            assertArrayEquals(expected, toBytes.convert(0x12345678, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("Integer.MIN_VALUE encodes to {0x80,0x00,0x00,0x00} (BE) / reversed (LE)")
        void encodesIntegerMinValue() {
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80, 0x00, 0x00, 0x00},
                            toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x00, 0x00, 0x00, (byte) 0x80},
                            toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Integer.MAX_VALUE encodes to {0x7F,0xFF,0xFF,0xFF} (BE) / reversed (LE)")
        void encodesIntegerMaxValue() {
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
                            toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F},
                            toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("output length is always the expected 4 bytes")
        void outputAlwaysFourBytes() {
            assertAll(
                    () -> assertEquals(4, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(4, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(4, toBytes.convert(0).length),
                    () -> assertEquals(4, toBytes.convert(0x1234_5678).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            int value = 0x1234_5678;
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
    @DisplayName("Round-trip int -> bytes -> int")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(0),
                    Arguments.of(1),
                    Arguments.of(-1),
                    Arguments.of(0x0000_00FF),
                    Arguments.of(0x0000_FF00),
                    Arguments.of(0x00FF_0000),
                    Arguments.of(0xFF00_0000), // negative in signed 32-bit
                    Arguments.of(0x1234_5678),
                    Arguments.of(-0x1234_5678),
                    Arguments.of(MIN_VALUE),
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
            int value = 0x1234_5678;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[3], le[2], le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }
}