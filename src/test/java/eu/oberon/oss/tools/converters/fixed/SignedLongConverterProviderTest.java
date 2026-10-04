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

class SignedLongConverterProviderTest {

    private static final long MIN_VALUE = Long.MIN_VALUE;
    private static final long MAX_VALUE = Long.MAX_VALUE;


    private final SignedLongConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.SIGNED_LONG.name())
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
        @DisplayName("reports SIGNED_LONG as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.SIGNED_LONG.name(), provider.getValueTypeName());
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
    @DisplayName("Byte array size validation (bytes -> long)")
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
    // Bytes -> Long (min / max / byte order / sign-extension)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> signed long (Long)")
    class BytesToObject {

        @Test
        @DisplayName("all-zero bytes decode to 0 in both byte orders")
        void zeroBothOrders() {
            byte[] zeros = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertEquals(0L, toObject.convert(zeros, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0L, toObject.convert(zeros, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("all-0xFF bytes decode to -1 in both byte orders")
        void allOnesDecodeToMinusOne() {
            byte[] ones = {
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
            };
            assertAll(
                    () -> assertEquals(-1L, toObject.convert(ones, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(-1L, toObject.convert(ones, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: {0x12..0xF0} decodes to 0x123456789ABCDEF0L")
        void bigEndianOrder() {
            byte[] bytes = {
                    0x12, 0x34, 0x56, 0x78,
                    (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
            };
            assertEquals(0x123456789ABCDEF0L, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: {0x12..0xF0} decodes to 0xF0DEBC9A78563412L")
        void littleEndianOrder() {
            byte[] bytes = {
                    0x12, 0x34, 0x56, 0x78,
                    (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
            };
            assertEquals(0xF0DEBC9A78563412L, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("byte order matters: BE and LE produce different results for asymmetric input")
        void byteOrderProducesDifferentResults() {
            byte[] bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
            assertAll(
                    () -> assertEquals(0x0000_0000_0000_0001L,
                            toObject.convert(bytes, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(0x0100_0000_0000_0000L,
                            toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("high bit set is interpreted as Long.MIN_VALUE in BE")
        void highBitIsNegativeBigEndian() {
            byte[] bytes = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertEquals(Long.MIN_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("high bit set in the last LE byte is interpreted as Long.MIN_VALUE in LE")
        void highBitIsNegativeLittleEndian() {
            byte[] bytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80};
            assertEquals(Long.MIN_VALUE, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("Long.MAX_VALUE encoded as BE bytes decodes back to Long.MAX_VALUE")
        void maxLongBigEndian() {
            byte[] bytes = {
                    (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
            };
            assertEquals(MAX_VALUE, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("palindrome bytes decode to the same value regardless of byte order")
        void palindromeIndependentOfByteOrder() {
            byte[] palindrome = {0x12, 0x34, 0x56, 0x78, 0x78, 0x56, 0x34, 0x12};
            long be = toObject.convert(palindrome, ByteOrder.BIG_ENDIAN);
            long le = toObject.convert(palindrome, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            byte[] bytes = {
                    0x12, 0x34, 0x56, 0x78,
                    (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
            };
            long viaDefault = toObject.convert(bytes);
            long viaNative = toObject.convert(bytes, ByteOrder.nativeOrder());
            assertEquals(viaNative, viaDefault);
        }
    }

    // ---------------------------------------------------------------------
    // Long -> bytes (min / max / byte order / negative values)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("signed long (Long) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("0 encodes to all-zero bytes in both byte orders")
        void encodesZero() {
            byte[] expected = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(0L, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(0L, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("-1 encodes to all-0xFF bytes in both byte orders")
        void encodesMinusOne() {
            byte[] expected = {
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
            };
            assertAll(
                    () -> assertArrayEquals(expected, toBytes.convert(-1L, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected, toBytes.convert(-1L, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("big-endian: 0x123456789ABCDEF0L encodes to {0x12..0xF0}")
        void encodesBigEndian() {
            byte[] expected = {
                    0x12, 0x34, 0x56, 0x78,
                    (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
            };
            assertArrayEquals(expected, toBytes.convert(0x123456789ABCDEF0L, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("little-endian: 0x123456789ABCDEF0L encodes to {0xF0..0x12}")
        void encodesLittleEndian() {
            byte[] expected = {
                    (byte) 0xF0, (byte) 0xDE, (byte) 0xBC, (byte) 0x9A,
                    0x78, 0x56, 0x34, 0x12
            };
            assertArrayEquals(expected, toBytes.convert(0x123456789ABCDEF0L, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("Long.MIN_VALUE encodes to {0x80,0x00,..} (BE) / reversed (LE)")
        void encodesLongMinValue() {
            assertAll(
                    () -> assertArrayEquals(
                            new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
                            toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(
                            new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80},
                            toBytes.convert(MIN_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Long.MAX_VALUE encodes to {0x7F,0xFF,..} (BE) / reversed (LE)")
        void encodesLongMaxValue() {
            assertAll(
                    () -> assertArrayEquals(
                            new byte[]{
                                    (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
                            },
                            toBytes.convert(MAX_VALUE, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(
                            new byte[]{
                                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x7F
                            },
                            toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("output length is always the expected 8 bytes")
        void outputAlwaysEightBytes() {
            assertAll(
                    () -> assertEquals(8, toBytes.convert(MIN_VALUE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length),
                    () -> assertEquals(8, toBytes.convert(0L).length),
                    () -> assertEquals(8, toBytes.convert(0x1234_5678_9ABC_DEF0L).length)
            );
        }

        @Test
        @DisplayName("no-arg overload uses ByteOrder.nativeOrder()")
        void defaultsToNativeOrder() {
            long value = 0x1234_5678_9ABC_DEF0L;
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
    @DisplayName("Round-trip long -> bytes -> long")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(0L),
                    Arguments.of(1L),
                    Arguments.of(-1L),
                    Arguments.of(0x0000_0000_0000_00FFL),
                    Arguments.of(0x0000_0000_0000_FF00L),
                    Arguments.of(0x0000_0000_00FF_0000L),
                    Arguments.of(0x0000_0000_FF00_0000L),
                    Arguments.of(0x0000_00FF_0000_0000L),
                    Arguments.of(0x0000_FF00_0000_0000L),
                    Arguments.of(0x00FF_0000_0000_0000L),
                    Arguments.of(0xFF00_0000_0000_0000L), // negative in signed 64-bit
                    Arguments.of(0x1234_5678_9ABC_DEF0L),
                    Arguments.of(-0x1234_5678_9ABC_DEF0L),
                    Arguments.of((long) Integer.MIN_VALUE),
                    Arguments.of((long) Integer.MAX_VALUE),
                    Arguments.of(MIN_VALUE),
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
            long value = 0x1234_5678_9ABC_DEF0L;
            byte[] be = toBytes.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert(value, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[7], le[6], le[5], le[4], le[3], le[2], le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }
}