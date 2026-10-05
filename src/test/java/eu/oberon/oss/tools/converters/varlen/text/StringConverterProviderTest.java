package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link StringConverterProvider}.
 * <p>
 * The protected methods {@link StringConverterProvider#fromString(String)} and {@link StringConverterProvider#toStringValue(String)} are exercised in two
 * ways:
 * <ul>
 *     <li>
 *         <b>Directly</b>, through a same-package subclass ({@code Exposed})
 *         that widens their visibility, verifying their identity contract.
 *     </li>
 *     <li>
 *         <b>Indirectly</b>, through the {@link TextToByteConverter} and
 *         {@link TextToObjectConverter} returned by the provider, which is how
 *         they are actually used in production.
 *     </li>
 * </ul>
 */
class StringConverterProviderTest {

    private final StringConverterProvider provider = new StringConverterProvider();
    private final TextToObjectConverter<String> toObject = provider.getToObjectConverter();
    private final TextToByteConverter<String> toBytes = provider.getToByteConverter();

    /**
     * Same-package subclass used to widen the visibility of the protected hook methods so they can be asserted directly.
     */
    private static final class Exposed extends StringConverterProvider {
        String callFromString(String value) {
            return fromString(value);
        }

        String callToStringValue(String value) {
            return toStringValue(value);
        }
    }

    private final Exposed exposed = new Exposed();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports STRING as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.STRING.name(), provider.getValueTypeName());
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
    // Direct tests of the protected hook methods (identity contract)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("fromString(String) - identity contract")
    class FromStringDirect {

        @ParameterizedTest(name = "returns the same reference for \"{0}\"")
        @ValueSource(strings = {
                "",
                "A",
                "Hello, World!",
                "café",
                "€中あ",
                "  leading and trailing  ",
                "line1\nline2\tend"
        })
        void returnsSameReference(String value) {
            assertSame(value, exposed.callFromString(value));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null input yields null output")
        void nullInputYieldsNull(String value) {
            assertNull(exposed.callFromString(value));
        }
    }

    @Nested
    @DisplayName("toStringValue(String) - identity contract")
    class ToStringValueDirect {

        @ParameterizedTest(name = "returns the same reference for \"{0}\"")
        @ValueSource(strings = {
                "",
                "A",
                "Hello, World!",
                "café",
                "€中あ",
                "  leading and trailing  ",
                "line1\nline2\tend"
        })
        void returnsSameReference(String value) {
            assertSame(value, exposed.callToStringValue(value));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null input yields null output")
        void nullInputYieldsNull(String value) {
            assertNull(exposed.callToStringValue(value));
        }
    }

    @Test
    @DisplayName("fromString and toStringValue are inverse (both being identity)")
    void fromStringAndToStringValueAreInverse() {
        String value = "round-trip é € 中 あ";
        assertAll(
                () -> assertSame(value, exposed.callFromString(exposed.callToStringValue(value))),
                () -> assertSame(value, exposed.callToStringValue(exposed.callFromString(value)))
        );
    }

    // ---------------------------------------------------------------------
    // Indirect tests via the converters exposed by the provider
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Indirect: bytes -> String (uses fromString internally)")
    class ToObjectIndirect {

        @Test
        @DisplayName("US-ASCII decoding returns the exact decoded String")
        void asciiDecoding() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.US_ASCII);
            assertEquals("Hello",
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("empty byte array decodes to an empty String (not null)")
        void emptyBytesProduceEmptyString() {
            String result = toObject.convert(new byte[0],
                    StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals("", result)
            );
        }

        @Test
        @DisplayName("null byte array is rejected on every overload")
        void rejectsNullArray() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(null, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }
    }

    @Nested
    @DisplayName("Indirect: String -> bytes (uses toStringValue internally)")
    class ToBytesIndirect {

        @Test
        @DisplayName("US-ASCII: 'Hello' encodes to the expected ASCII bytes")
        void asciiEncoding() {
            assertArrayEquals("Hello".getBytes(StandardCharsets.US_ASCII),
                    toBytes.convert("Hello", StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("empty String encodes to an empty byte[]")
        void emptyStringProducesEmptyBytes() {
            byte[] result = toBytes.convert("", StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(0, result.length)
            );
        }

        @Test
        @DisplayName("null String is rejected on every overload")
        void rejectsNullString() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // Charset / byte-order specific behavior
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Single-byte charsets (ASCII / Latin-1 / Windows-1252)")
    class SingleByteCharsets {

        @Test
        @DisplayName("US-ASCII round trip is byte-order-independent")
        void asciiRoundTripIndependentOfByteOrder() {
            String source = "Hello, World!";
            byte[] bytes = source.getBytes(StandardCharsets.US_ASCII);
            assertAll(
                    () -> assertEquals(source,
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals(source,
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(source, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(source, StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("ISO-8859-1: 'café' round-trips exactly")
        void latin1RoundTrip() {
            String source = "café";
            byte[] bytes = source.getBytes(StandardCharsets.ISO_8859_1);
            assertAll(
                    () -> assertEquals(source,
                            toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(source, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("Windows-1252: '€' encodes to {0x80}")
        void windows1252EuroSign() {
            Charset cp1252 = Charset.forName("windows-1252");
            byte[] bytes = toBytes.convert("€", cp1252, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80}, bytes),
                    () -> assertEquals("€", toObject.convert(bytes, cp1252, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    @Nested
    @DisplayName("UTF-8 (variable-width, byte-order-independent)")
    class Utf8 {

        @Test
        @DisplayName("Mixed-width string round-trips through UTF-8")
        void mixedWidthRoundTrip() {
            String source = "A é € 中 あ";
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertEquals(source,
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(source, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("UTF-8 byte order parameter does not affect either direction")
        void byteOrderIrrelevant() {
            String source = "café";
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            assertAll(
                    () -> assertEquals(
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN),
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertArrayEquals(
                            toBytes.convert(source, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN),
                            toBytes.convert(source, StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    @Nested
    @DisplayName("UTF-16BE / UTF-16LE (endian-qualified)")
    class Utf16Qualified {

        @Test
        @DisplayName("UTF-16BE: 'AB' encodes to {0x00,0x41,0x00,0x42}")
        void utf16BeEncodesAB() {
            assertArrayEquals(new byte[]{0x00, 0x41, 0x00, 0x42},
                    toBytes.convert("AB", StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: 'AB' encodes to {0x41,0x00,0x42,0x00}")
        void utf16LeEncodesAB() {
            assertArrayEquals(new byte[]{0x41, 0x00, 0x42, 0x00},
                    toBytes.convert("AB", StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16BE: {0x00,0x41,0x00,0x42} decodes to 'AB'")
        void utf16BeDecodesAB() {
            assertEquals("AB", toObject.convert(new byte[]{0x00, 0x41, 0x00, 0x42},
                    StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: {0x41,0x00,0x42,0x00} decodes to 'AB'")
        void utf16LeDecodesAB() {
            assertEquals("AB", toObject.convert(new byte[]{0x41, 0x00, 0x42, 0x00},
                    StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }
    }

    @Nested
    @DisplayName("Unqualified UTF-16 (byte-order aware)")
    class Utf16Unqualified {

        @Test
        @DisplayName("UTF-16 + LITTLE_ENDIAN encodes 'A' without BOM as {0x41,0x00}")
        void utf16LittleEndianEncodeNoBom() {
            assertArrayEquals(new byte[]{0x41, 0x00},
                    toBytes.convert("A", StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16 + BIG_ENDIAN encodes 'A' without BOM as {0x00,0x41}")
        void utf16BigEndianEncodeNoBom() {
            assertArrayEquals(new byte[]{0x00, 0x41},
                    toBytes.convert("A", StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16 + LITTLE_ENDIAN decodes {0x41,0x00} to 'A'")
        void utf16LittleEndianDecode() {
            assertEquals("A", toObject.convert(new byte[]{0x41, 0x00},
                    StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16 + BIG_ENDIAN decodes {0x00,0x41} to 'A'")
        void utf16BigEndianDecode() {
            assertEquals("A", toObject.convert(new byte[]{0x00, 0x41},
                    StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: String -> bytes -> String
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip String -> bytes -> String")
    class RoundTrip {

        static Stream<Arguments> samples() {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("A"),
                    Arguments.of("Hello, World!"),
                    Arguments.of("café"),
                    Arguments.of("€中あ"),
                    Arguments.of("Mixed 123 é € 中 あ ~")
            );
        }

        @ParameterizedTest(name = "US-ASCII (ASCII-only sample) round-trip: \"{0}\"")
        @MethodSource("samples")
        void asciiOnlyRoundTrip(String source) {
            if (!source.chars().allMatch(c -> c < 0x80)) {
                return; // skip non-ASCII samples for US-ASCII
            }
            byte[] bytes = toBytes.convert(source, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-8 round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf8RoundTrip(String source) {
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16BE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16BeRoundTrip(String source) {
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16LE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16LeRoundTrip(String source) {
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + BE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16UnqualifiedBeRoundTrip(String source) {
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + LE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16UnqualifiedLeRoundTrip(String source) {
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);
            assertEquals(source,
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Default-overload behavior
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Default overload behavior")
    class Defaults {

        @Test
        @DisplayName("convert(byte[]) uses the default charset (round-trip via convert(String))")
        void toObjectSingleArgUsesDefaults() {
            String source = "Hello";
            byte[] bytes = toBytes.convert(source);
            assertEquals(source, toObject.convert(bytes));
        }

        @Test
        @DisplayName("convert(byte[], charset) uses the native byte order")
        void toObjectTwoArgUsesNativeByteOrder() {
            String source = "Hi";
            byte[] bytes = toBytes.convert(source, StandardCharsets.UTF_8);
            assertEquals(source, toObject.convert(bytes, StandardCharsets.UTF_8));
        }
    }

    // ---------------------------------------------------------------------
    // Output invariants
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Output invariants")
    class OutputInvariants {

        @Test
        @DisplayName("returned byte[] is a fresh instance on every call")
        void returnsFreshByteArrayEachCall() {
            byte[] first = toBytes.convert("A", StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            byte[] second = toBytes.convert("A", StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertArrayEquals(first, second)
            );

            // Mutating the returned array must not affect further calls.
            first[0] = (byte) 0x42;
            assertArrayEquals(new byte[]{0x41},
                    toBytes.convert("A", StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("length of encoded UTF-16BE byte[] equals 2 * String length for BMP chars")
        void encodedUtf16BeLength() {
            byte[] bytes = toBytes.convert("Hello", StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            assertEquals(10, bytes.length);
        }
    }
}