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

class SignedShortConverterProviderTest {

    private static final short MIN_VALUE = Short.MIN_VALUE; // -32768 == 0x8000
    private static final short MAX_VALUE = Short.MAX_VALUE; //  32767 == 0x7FFF

    private final SignedShortConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.SIGNED_SHORT.name())
    );
    private final FixedToObjectConverter<Short> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<Short> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports SIGNED_SHORT as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.SIGNED_SHORT.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 2-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(2, provider.getExpectedByteArraySize());
        }
    }

    // ---------------------------------------------------------------------
    // Byte array size / null validation for the object converter
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Byte array size validation (bytes -> short)")
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
    // Bytes -> Short (min / max / byte order / sign extension)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> signed short (Short)")
    class BytesToObject {

        @Test
        @DisplayName("all-zero bytes decode to 0 in both byte orders")
        void zeroBothOrders() {
            byte[] zeros = {0x00, 0x00};
            assertAll(
                    () -> assertEquals((short) 0, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals((short) 0, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("all-0xFF bytes decode to -1 in both byte orders")
        void allOnesDecodesToMinusOne() {
            byte[] ones = {(byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertEquals((short) -1, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals((short) -1, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x12,0x34} decodes to 0x1234")
        void bigEndianOrder() {
            byte[] bytes = {0x12, 0x34};
            assertEquals((short) 0x1234, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x12,0x34} decodes to 0x3412")
        void littleEndianOrder() {
            byte[] bytes = {0x12, 0x34};
            assertEquals((short) 0x3412, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x00, 0x01};
            short be = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            short le = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals((short) 0x0001, be),
                    () -> assertEquals((short) 0x0100, le)
            );
        }

        @Test
        @DisplayName("high bit set: {0x80,0x00} BE=Short.MIN_VALUE, LE=0x0080")
        void highBitSignExtendsCorrectly() {
            byte[] bytes = {(byte) 0x80, 0x00};
            assertAll(
                    () -> assertEquals(Short.MIN_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals((short) 0x0080, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Short.MAX_VALUE: {0x7F,0xFF} BE=32767, LE=(short)0xFF7F=-129")
        void decodesShortMaxBoundary() {
            byte[] bytes = {(byte) 0x7F, (byte) 0xFF};
            assertAll(
                    () -> assertEquals(Short.MAX_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals((short) 0xFF7F, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x42, 0x42};
            short be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            short le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {0x12, 0x34};
            short viaDefault = toObject.convert(bytes);
            short viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // Short -> bytes (min / max / byte order / negatives)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("signed short (Short) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("zero encodes to all-zero bytes in both byte orders")
        void encodesZero() {
            byte[] expected = {0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert((short) 0, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert((short) 0, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("-1 encodes to {0xFF,0xFF} in both byte orders")
        void encodesMinusOne() {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert((short) -1, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert((short) -1, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Short.MIN_VALUE encodes to {0x80,0x00} BE / {0x00,0x80} LE")
        void encodesMinValue() {
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80, 0x00},
                            toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x00, (byte) 0x80},
                            toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Short.MAX_VALUE encodes to {0x7F,0xFF} BE / {0xFF,0x7F} LE")
        void encodesMaxValue() {
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x7F, (byte) 0xFF},
                            toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0x7F},
                            toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x1234 encodes to {0x12,0x34}")
        void encodesBigEndian() {
            byte[] expected = {0x12, 0x34};
            assertArrayEquals(expected, toBytes.convert((short) 0x1234, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x1234 encodes to {0x34,0x12}")
        void encodesLittleEndian() {
            byte[] expected = {0x34, 0x12};
            assertArrayEquals(expected, toBytes.convert((short) 0x1234, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("output length is always the expected 2 bytes")
        void outputAlwaysTwoBytes() {
            assertAll(
                    () -> assertEquals(2, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(2, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(2, toBytes.convert((short) 0x1234).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            short value = 0x1234;
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
    @DisplayName("Round-trip short -> bytes -> short")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(MIN_VALUE),                       // -32768
                    Arguments.of((short) (MIN_VALUE + 1)),         // -32767
                    Arguments.of((short) -256),
                    Arguments.of((short) -1),
                    Arguments.of((short) 0),
                    Arguments.of((short) 1),
                    Arguments.of((short) 0x00FF),
                    Arguments.of((short) 0x0100),
                    Arguments.of((short) 0x1234),
                    Arguments.of((short) (MAX_VALUE - 1)),         //  32766
                    Arguments.of(MAX_VALUE)                        //  32767
            );
        }

        @ParameterizedTest(name = "big-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void bigEndianRoundTrip(short value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "little-endian round trip of {0}")
        @MethodSource("roundTripValues")
        void littleEndianRoundTrip(short value) {
            byte[] bytes = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("BE and LE encodings of the same value are exact byte-reverses of each other")
        void beAndLeAreByteReversed() {
            short value = 0x1234;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }

        @Test
        @DisplayName("exhaustive round-trip over the full signed short range in both byte orders")
        void exhaustiveRoundTripFullRange() {
            for (int v = Short.MIN_VALUE; v <= Short.MAX_VALUE; v++) {
                short value = (short) v;

                byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
                byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);

                assertEquals(value, toObject.convert(be, ByteOrder.BIG_ENDIAN),
                        "BE round-trip failed for value " + value);
                assertEquals(value, toObject.convert(le, ByteOrder.LITTLE_ENDIAN),
                        "LE round-trip failed for value " + value);
            }
        }
    }
}