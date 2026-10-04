package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class FloatConvertProviderTest {

    private FloatConvertProvider provider;
    private FixedToObjectConverter<Float> toObject;
    private FixedToByteConverter<Float> toByte;

    @BeforeEach
    void setUp() {
        provider = Objects.requireNonNull(
                AbstractConverterProvider.getConverterProvider(ValueTypeNames.FLOAT.name())
        );
        toObject = provider.getToObjectConverter();
        toByte = provider.getToByteConverter();
    }

    @Nested
    @DisplayName("Provider metadata")
    class ProviderMetadata {

        @Test
        @DisplayName("Value type name should be FLOAT")
        void valueTypeNameIsFloat() {
            assertEquals(ValueTypeNames.FLOAT.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("Expected byte array size should be 4")
        void expectedByteArraySizeIsFour() {
            assertEquals(4, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("Converters should not be null")
        void convertersAreNotNull() {
            assertNotNull(toObject);
            assertNotNull(toByte);
        }
    }

    @Nested
    @DisplayName("ToByteConverter tests")
    class ToByteConverterTests {

        @Test
        @DisplayName("Big-endian encoding should produce the expected bytes")
        void bigEndianEncoding() {
            // 1.0f -> 0x3F800000
            byte[] bytes = toByte.convert(1.0f, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(new byte[]{0x3F, (byte) 0x80, 0x00, 0x00}, bytes);
        }

        @Test
        @DisplayName("Little-endian encoding should produce reversed bytes")
        void littleEndianEncoding() {
            byte[] bytes = toByte.convert(1.0f, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(new byte[]{0x00, 0x00, (byte) 0x80, 0x3F}, bytes);
        }

        @Test
        @DisplayName("Default overload should use native byte order")
        void defaultUsesNativeByteOrder() {
            float value = 3.14f;
            byte[] defaultBytes = toByte.convert(value);
            byte[] nativeBytes = toByte.convert(value, ByteOrder.nativeOrder());
            assertArrayEquals(nativeBytes, defaultBytes);
        }

        @Test
        @DisplayName("Output byte array should always have length 4")
        void outputAlwaysFourBytes() {
            assertEquals(4, toByte.convert(0.0f, ByteOrder.BIG_ENDIAN).length);
            assertEquals(4, toByte.convert(Float.MAX_VALUE, ByteOrder.LITTLE_ENDIAN).length);
        }

        @Test
        @DisplayName("Encoding matches ByteBuffer output (big-endian)")
        void matchesByteBufferBigEndian() {
            float[] values = {0.0f, -0.0f, 1.5f, -12345.6789f, Float.MAX_VALUE, Float.MIN_VALUE};
            for (float v : values) {
                byte[] expected = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(v).array();
                assertArrayEquals(expected, toByte.convert(v, ByteOrder.BIG_ENDIAN),
                        "Mismatch for value: " + v);
            }
        }

        @Test
        @DisplayName("Encoding matches ByteBuffer output (little-endian)")
        void matchesByteBufferLittleEndian() {
            float[] values = {0.0f, -0.0f, 1.5f, -12345.6789f, Float.MAX_VALUE, Float.MIN_VALUE};
            for (float v : values) {
                byte[] expected = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(v).array();
                assertArrayEquals(expected, toByte.convert(v, ByteOrder.LITTLE_ENDIAN),
                        "Mismatch for value: " + v);
            }
        }

        @Test
        @DisplayName("NaN should encode using canonical NaN bit pattern")
        void nanEncoding() {
            byte[] bytes = toByte.convert(Float.NaN, ByteOrder.BIG_ENDIAN);
            int bits = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                    | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
            assertEquals(Float.floatToIntBits(Float.NaN), bits);
        }
    }

    @Nested
    @DisplayName("ToObjectConverter tests")
    class ToObjectConverterTests {

        @Test
        @DisplayName("Big-endian decoding produces expected float")
        void bigEndianDecoding() {
            // 0x3F800000 -> 1.0f
            byte[] bytes = new byte[]{0x3F, (byte) 0x80, 0x00, 0x00};
            assertEquals(1.0f, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("Little-endian decoding produces expected float")
        void littleEndianDecoding() {
            byte[] bytes = new byte[]{0x00, 0x00, (byte) 0x80, 0x3F};
            assertEquals(1.0f, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("Default overload should use native byte order")
        void defaultUsesNativeByteOrder() {
            byte[] bytes = toByte.convert(2.71828f, ByteOrder.nativeOrder());
            assertEquals(toObject.convert(bytes, ByteOrder.nativeOrder()),
                    toObject.convert(bytes));
        }

        @Test
        @DisplayName("Null byte array should throw NullPointerException")
        void nullByteArrayThrows() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> toObject.convert(null, ByteOrder.BIG_ENDIAN));
            assertTrue(ex.getMessage().contains("byteArray"));
        }

        @ParameterizedTest(name = "byte array of length {0} should be rejected")
        @ValueSource(ints = {0, 1, 2, 3, 5, 8})
        @DisplayName("Wrong-sized byte array should throw IllegalArgumentException")
        void wrongSizedByteArrayThrows(int length) {
            byte[] bytes = new byte[length];
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
            assertTrue(ex.getMessage().contains("wrong size"));
            assertTrue(ex.getMessage().contains("expected size was 4"));
        }

        @Test
        @DisplayName("Decoding matches ByteBuffer output (big-endian)")
        void matchesByteBufferBigEndian() {
            float[] values = {0.0f, -0.0f, 1.5f, -12345.6789f, Float.MAX_VALUE, Float.MIN_VALUE};
            for (float v : values) {
                byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(v).array();
                assertEquals(v, toObject.convert(bytes, ByteOrder.BIG_ENDIAN),
                        "Mismatch for value: " + v);
            }
        }

        @Test
        @DisplayName("Decoding matches ByteBuffer output (little-endian)")
        void matchesByteBufferLittleEndian() {
            float[] values = {0.0f, -0.0f, 1.5f, -12345.6789f, Float.MAX_VALUE, Float.MIN_VALUE};
            for (float v : values) {
                byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(v).array();
                assertEquals(v, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN),
                        "Mismatch for value: " + v);
            }
        }

        @Test
        @DisplayName("NaN round-trips with matching bit pattern")
        void decodesNaN() {
            byte[] bytes = toByte.convert(Float.NaN, ByteOrder.BIG_ENDIAN);
            Float result = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);
            assertEquals(Float.floatToIntBits(Float.NaN), Float.floatToIntBits(result));
        }

        @Test
        @DisplayName("Positive infinity should round-trip")
        void decodesPositiveInfinity() {
            byte[] bytes = toByte.convert(Float.POSITIVE_INFINITY, ByteOrder.BIG_ENDIAN);
            assertEquals(Float.POSITIVE_INFINITY, toObject.convert(bytes, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("Negative infinity should round-trip")
        void decodesNegativeInfinity() {
            byte[] bytes = toByte.convert(Float.NEGATIVE_INFINITY, ByteOrder.LITTLE_ENDIAN);
            assertEquals(Float.NEGATIVE_INFINITY, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN));
        }
    }

    @Nested
    @DisplayName("Round-trip tests")
    class RoundTripTests {

        @Test
        @DisplayName("Round-trip in big-endian preserves value")
        void roundTripBigEndian() {
            float[] values = {0.0f, 1.0f, -1.0f, 3.14159f, -2.71828f,
                    Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL};
            for (float v : values) {
                byte[] bytes = toByte.convert(v, ByteOrder.BIG_ENDIAN);
                assertEquals(v, toObject.convert(bytes, ByteOrder.BIG_ENDIAN),
                        "Round-trip failed for: " + v);
            }
        }

        @Test
        @DisplayName("Round-trip in little-endian preserves value")
        void roundTripLittleEndian() {
            float[] values = {0.0f, 1.0f, -1.0f, 3.14159f, -2.71828f,
                    Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_NORMAL};
            for (float v : values) {
                byte[] bytes = toByte.convert(v, ByteOrder.LITTLE_ENDIAN);
                assertEquals(v, toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN),
                        "Round-trip failed for: " + v);
            }
        }

        @Test
        @DisplayName("Round-trip with default (native) byte order preserves value")
        void roundTripNative() {
            float value = 42.42f;
            byte[] bytes = toByte.convert(value);
            assertEquals(value, toObject.convert(bytes));
        }

        @Test
        @DisplayName("Big-endian and little-endian encodings should be byte-reversed")
        void endiannessIsReversed() {
            float value = 123.456f;
            byte[] be = toByte.convert(value, ByteOrder.BIG_ENDIAN);
            byte[] le = toByte.convert(value, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(new byte[]{be[3], be[2], be[1], be[0]}, le);
        }
    }
}