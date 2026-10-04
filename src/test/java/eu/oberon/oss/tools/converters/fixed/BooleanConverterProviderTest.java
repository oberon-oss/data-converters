package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BooleanConverterProviderTest {

    // Assure that a convert provider returns an actual object and not null.

    private final BooleanConverterProvider provider = Objects.requireNonNull(
            AbstractConverterProvider.getConverterProvider(ValueTypeNames.BOOLEAN.name())
    );
    private final FixedToObjectConverter<Boolean> toObject = provider.getToObjectConverter();
    private final FixedToByteConverter<Boolean> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports BOOLEAN as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.BOOLEAN.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("expects a 1-byte input/output array")
        void reportsCorrectExpectedByteArraySize() {
            assertEquals(1, provider.getExpectedByteArraySize());
        }

        @Test
        @DisplayName("getToObjectConverter() returns a non-null converter")
        void toObjectConverterNotNull() {
            assertNotNull(provider.getToObjectConverter());
        }

        @Test
        @DisplayName("getToByteConverter() returns a non-null converter")
        void toByteConverterNotNull() {
            assertNotNull(provider.getToByteConverter());
        }
    }

    // ---------------------------------------------------------------------
    // Bytes -> Boolean
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("bytes -> Boolean")
    class BytesToObject {

        @Test
        @DisplayName("byte 0x00 decodes to false")
        void zeroByteDecodesToFalse() {
            assertFalse(toObject.convert(new byte[]{0x00}));
        }

        @Test
        @DisplayName("byte 0x01 decodes to true")
        void oneByteDecodesToTrue() {
            assertTrue(toObject.convert(new byte[]{0x01}));
        }

        @ParameterizedTest(name = "byte value {0} decodes to true")
        @ValueSource(ints = {0x01, 0x02, 0x7F, 0x80, 0xFF})
        @DisplayName("any non-zero byte decodes to true")
        void anyNonZeroByteDecodesToTrue(int byteValue) {
            assertTrue(toObject.convert(new byte[]{(byte) byteValue}));
        }

        @Test
        @DisplayName("empty byte array causes an ArrayIndexOutOfBoundsException")
        void emptyArrayFails() {
            assertThrows(IllegalArgumentException.class,
                    () -> toObject.convert(new byte[]{}));
        }

        @Test
        @DisplayName("null byte array causes a NullPointerException")
        void nullArrayFails() {
            assertThrows(NullPointerException.class,
                    () -> toObject.convert(null));
        }
    }

    // ---------------------------------------------------------------------
    // Boolean -> bytes
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Boolean -> bytes")
    class ObjectToBytes {

        @Test
        @DisplayName("true encodes to {0x01}")
        void trueEncodesToOne() {
            assertArrayEquals(new byte[]{0x01}, toBytes.convert(true));
        }

        @Test
        @DisplayName("false encodes to {0x00}")
        void falseEncodesToZero() {
            assertArrayEquals(new byte[]{0x00}, toBytes.convert(false));
        }

        @Test
        @DisplayName("output length is always the expected 1 byte")
        void outputAlwaysOneByte() {
            assertAll(
                    () -> assertEquals(1, toBytes.convert(true).length),
                    () -> assertEquals(1, toBytes.convert(false).length)
            );
        }

        @Test
        @DisplayName("null Boolean causes a NullPointerException (auto-unbox of null)")
        void nullValueFails() {
            assertThrows(NullPointerException.class, () -> toBytes.convert(null));
        }

        @Test
        @DisplayName("returned array is a fresh instance on every call (no shared state)")
        void returnsFreshArrayEachCall() {
            byte[] first = toBytes.convert(true);
            byte[] second = toBytes.convert(true);
            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertArrayEquals(first, second)
            );

            // Mutate the returned array; a subsequent call must be unaffected
            first[0] = (byte) 0x42;
            assertArrayEquals(new byte[]{0x01}, toBytes.convert(true));
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: value -> bytes -> value
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip Boolean -> bytes -> Boolean")
    class RoundTrip {

        static Stream<Arguments> roundTripValues() {
            return Stream.of(
                    Arguments.of(true),
                    Arguments.of(false)
            );
        }

        @ParameterizedTest(name = "round trip of {0}")
        @MethodSource("roundTripValues")
        void roundTrip(boolean value) {
            byte[] bytes = toBytes.convert(value);
            assertNotNull(bytes);
            assertEquals(value, toObject.convert(bytes));
        }
    }
}