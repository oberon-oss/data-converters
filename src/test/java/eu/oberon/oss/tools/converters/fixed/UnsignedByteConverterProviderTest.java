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
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UnsignedByteConverterProviderTest {

    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 0xFF; // 255

    private final UnsignedByteConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.UNSIGNED_BYTE.name())
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
        @DisplayName("reports UNSIGNED_BYTE as target value type")
        void reportsCorrectValueType() {
            Assertions.assertEquals(ValueTypeNames.UNSIGNED_BYTE.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 1-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(1, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("getToObjectConverter() never returns null")
        void toObjectConverterIsProvided() {
            assertNotNull(provider.getToObjectConverter());
        }

        @Test
        @DisplayName("getToByteConverter() never returns null")
        void toByteConverterIsProvided() {
            assertNotNull(provider.getToByteConverter());
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Integer
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> unsigned byte (Integer)")
    class BytesToObject {

        @Test
        @DisplayName("0x00 decodes to 0")
        void decodesZero() {
            assertEquals(MIN_VALUE, toObject.convert(new byte[]{0x00}));
        }

        @Test
        @DisplayName("0xFF decodes to 255 (no sign extension)")
        void decodesMaxValue() {
            assertEquals(MAX_VALUE, toObject.convert(new byte[]{(byte) 0xFF}));
        }

        @Test
        @DisplayName("0x80 decodes to 128 (high bit does not sign-extend)")
        void decodesHighBitByte() {
            assertEquals(0x80, toObject.convert(new byte[]{(byte) 0x80}));
        }

        @Test
        @DisplayName("negative signed byte value -1 decodes to unsigned 255")
        void decodesNegativeSignedByteAsUnsigned() {
            assertEquals(255, toObject.convert(new byte[]{(byte) -1}));
        }

        @ParameterizedTest(name = "signed byte {0} decodes to unsigned {1}")
        @MethodSource("signedToUnsignedPairs")
        void decodesArbitraryBytes(byte signed, int expectedUnsigned) {
            assertEquals(expectedUnsigned, toObject.convert(new byte[]{signed}));
        }

        static Stream<Arguments> signedToUnsignedPairs() {
            return Stream.of(
                    Arguments.of((byte) 0x00, 0),
                    Arguments.of((byte) 0x01, 1),
                    Arguments.of((byte) 0x7F, 127),
                    Arguments.of((byte) 0x80, 128),
                    Arguments.of((byte) 0x81, 129),
                    Arguments.of((byte) 0xFE, 254),
                    Arguments.of((byte) 0xFF, 255)
            );
        }
    }

    // ---------------------------------------------------------------------
    // Integer -> bytes
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("unsigned byte (Integer) -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("0 encodes to a single 0x00 byte")
        void encodesMinValue() {
            assertArrayEquals(new byte[]{0x00}, toBytes.convert(MIN_VALUE));
        }

        @Test
        @DisplayName("255 encodes to a single 0xFF byte")
        void encodesMaxValue() {
            assertArrayEquals(new byte[]{(byte) 0xFF}, toBytes.convert(MAX_VALUE));
        }

        @Test
        @DisplayName("128 encodes to a single 0x80 byte")
        void encodesHighBitValue() {
            assertArrayEquals(new byte[]{(byte) 0x80}, toBytes.convert(128));
        }

        @ParameterizedTest(name = "value {0} produces a 1-byte array")
        @ValueSource(ints = {0, 1, 42, 127, 128, 200, 254, 255})
        void outputAlwaysOneByte(int value) {
            byte[] out1 = toBytes.convert(value);
            assertNotNull(out1);
            assertEquals(1, out1.length);

            // The methods using a user-specified byte order should produce the same result as the default method
            assertArrayEquals(out1, toBytes.convert(value, ByteOrder.BIG_ENDIAN));
            assertArrayEquals(out1, toBytes.convert(value, ByteOrder.LITTLE_ENDIAN));
            assertArrayEquals(out1, toBytes.convert(value, ByteOrder.nativeOrder()));
        }

        @ParameterizedTest(name = "value {0} is out of unsigned byte range and rejected")
        @ValueSource(ints = {-1, -128, Integer.MIN_VALUE, 256, 1000, Integer.MAX_VALUE})
        void rejectsOutOfRangeValues(int value) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> toBytes.convert(value));
            // Message should mention the offending value; not strictly required, but useful to lock in.
            assertNotNull(ex.getMessage());
        }

        @Test
        @DisplayName("null input is rejected with NullPointerException (auto-unboxing of Integer)")
        void rejectsNullValue() {
            assertThrows(NullPointerException.class, () -> toBytes.convert(null));
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip Integer -> bytes -> Integer")
    class RoundTrip {

        @ParameterizedTest(name = "round trip of unsigned byte value {0}")
        @ValueSource(ints = {0, 1, 2, 15, 16, 42, 127, 128, 129, 200, 254, 255})
        void roundTripValues(int value) {
            byte[] bytes = toBytes.convert(value);
            assertNotNull(bytes);
            assertEquals(1, bytes.length);
            assertEquals(value, toObject.convert(bytes));
        }

        @Test
        @DisplayName("full 0..255 round-trip: every unsigned byte value survives encode+decode")
        void fullRangeRoundTrip() {
            for (int value = MIN_VALUE; value <= MAX_VALUE; value++) {
                byte[] bytes = toBytes.convert(value);
                assertEquals(1, bytes.length, "unexpected length for value " + value);
                int decoded = toObject.convert(bytes);
                assertEquals(value, decoded, "round-trip mismatch for value " + value);
            }
        }
    }
}