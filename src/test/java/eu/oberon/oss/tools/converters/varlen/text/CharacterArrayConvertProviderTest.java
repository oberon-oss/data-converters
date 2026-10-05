package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CharacterArrayConverterProvider}.
 * <p>
 * Verifies:
 * <ul>
 *     <li>Provider metadata (value type name, converter wiring)</li>
 *     <li>{@link TextToObjectConverter} behavior for {@code byte[] -> Character[]}</li>
 *     <li>{@link TextToByteConverter} behavior for {@code Character[] -> byte[]}</li>
 *     <li>Charset / byte-order handling (single-byte, UTF-8, UTF-16 BE/LE, unqualified UTF-16)</li>
 *     <li>Round-trip fidelity via the sibling converter</li>
 *     <li>Null-parameter validation on every overload</li>
 * </ul>
 */
class CharacterArrayConvertProviderTest {

    private final CharacterArrayConverterProvider provider = new CharacterArrayConverterProvider();
    private final TextToObjectConverter<Character[]> toObject = provider.getToObjectConverter();
    private final TextToByteConverter<Character[]> toBytes = provider.getToByteConverter();

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static Character[] boxed(String s) {
        Character[] out = new Character[s.length()];
        for (int i = 0; i < s.length(); i++) {
            out[i] = s.charAt(i);
        }
        return out;
    }

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports CHARACTER_ARRAY as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.CHARACTER_ARRAY.name(), provider.getValueTypeName());
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
    // Input validation - to-object
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation (bytes -> Character[])")
    class ToObjectValidation {

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

        @Test
        @DisplayName("null charset is rejected")
        void rejectsNullCharset() {
            byte[] bytes = {'A'};
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(bytes, (Charset) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toObject.convert(bytes, null, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("null byte order is rejected")
        void rejectsNullByteOrder() {
            byte[] bytes = {'A'};
            assertThrows(NullPointerException.class,
                    () -> toObject.convert(bytes, StandardCharsets.UTF_8, null));
        }

        @Test
        @DisplayName("empty byte array decodes to an empty Character[]")
        void emptyBytesProduceEmptyArray() {
            Character[] result =
                    toObject.convert(new byte[0], StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(0, result.length)
            );
        }
    }

    // ---------------------------------------------------------------------
    // Input validation - to-bytes
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation (Character[] -> bytes)")
    class ToBytesValidation {

        @Test
        @DisplayName("null Character[] is rejected on every overload")
        void rejectsNullArray() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(null, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("null charset is rejected")
        void rejectsNullCharset() {
            Character[] input = boxed("A");
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(input, (Charset) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert(input, null, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("null byte order is rejected")
        void rejectsNullByteOrder() {
            Character[] input = boxed("A");
            assertThrows(NullPointerException.class,
                    () -> toBytes.convert(input, StandardCharsets.UTF_8, null));
        }

        @Test
        @DisplayName("null element inside Character[] triggers NPE during unboxing (documented behavior)")
        void rejectsNullElement() {
            Character[] input = {'A', null, 'C'};
            assertThrows(NullPointerException.class,
                    () -> toBytes.convert(input, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("empty Character[] encodes to an empty byte[]")
        void emptyArrayProducesEmptyBytes() {
            byte[] result = toBytes.convert(new Character[0],
                    StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(0, result.length)
            );
        }
    }

    // ---------------------------------------------------------------------
    // Single-byte charsets (byte order must be irrelevant)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Single-byte charsets (ASCII / Latin-1 / Windows-1252)")
    class SingleByteCharsets {

        @Test
        @DisplayName("US-ASCII: 'Hello' round-trips regardless of byte order")
        void asciiIndependentOfByteOrder() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.US_ASCII);
            Character[] expected = boxed("Hello");
            assertAll(
                    () -> assertArrayEquals(expected,
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected,
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(expected, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(expected, StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("ISO-8859-1: 'café' encodes/decodes to Latin-1 bytes")
        void latin1RoundTrip() {
            byte[] bytes = "café".getBytes(StandardCharsets.ISO_8859_1);
            Character[] expected = boxed("café");
            assertAll(
                    () -> assertArrayEquals(expected,
                            toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(expected, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("Windows-1252: '€' encodes to {0x80}")
        void windows1252EuroSign() {
            Charset cp1252 = Charset.forName("windows-1252");
            Character[] input = boxed("€");
            byte[] bytes = toBytes.convert(input, cp1252, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80}, bytes),
                    () -> assertArrayEquals(input,
                            toObject.convert(bytes, cp1252, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // UTF-8 (byte-order-independent)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-8 (variable-width, byte-order-independent)")
    class Utf8 {

        @Test
        @DisplayName("Mixed-width string round-trips through UTF-8")
        void mixedWidthRoundTrip() {
            String source = "A é € 中 あ";
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            Character[] expected = boxed(source);

            assertAll(
                    () -> assertArrayEquals(expected,
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(bytes,
                            toBytes.convert(expected, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("UTF-8 byte order parameter does not affect either direction")
        void byteOrderIrrelevant() {
            String source = "café";
            byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
            Character[] chars = boxed(source);

            assertAll(
                    () -> assertArrayEquals(
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN),
                            toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertArrayEquals(
                            toBytes.convert(chars, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN),
                            toBytes.convert(chars, StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // UTF-16BE / UTF-16LE - exact byte layout
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-16BE / UTF-16LE (endian-qualified)")
    class Utf16Qualified {

        @Test
        @DisplayName("UTF-16BE: 'AB' encodes to {0x00,0x41,0x00,0x42}")
        void utf16BeEncodesAB() {
            byte[] expected = {0x00, 0x41, 0x00, 0x42};
            assertArrayEquals(expected,
                    toBytes.convert(boxed("AB"), StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: 'AB' encodes to {0x41,0x00,0x42,0x00}")
        void utf16LeEncodesAB() {
            byte[] expected = {0x41, 0x00, 0x42, 0x00};
            assertArrayEquals(expected,
                    toBytes.convert(boxed("AB"), StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16BE: {0x00,0x41,0x00,0x42} decodes to ['A','B']")
        void utf16BeDecodesAB() {
            byte[] bytes = {0x00, 0x41, 0x00, 0x42};
            assertArrayEquals(boxed("AB"),
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: {0x41,0x00,0x42,0x00} decodes to ['A','B']")
        void utf16LeDecodesAB() {
            byte[] bytes = {0x41, 0x00, 0x42, 0x00};
            assertArrayEquals(boxed("AB"),
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Unqualified UTF-16 (byte-order aware in this provider)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Unqualified UTF-16 (byte-order aware)")
    class Utf16Unqualified {

        @Test
        @DisplayName("UTF-16 + LITTLE_ENDIAN encodes 'A' without BOM as {0x41,0x00}")
        void utf16LittleEndianEncodeNoBom() {
            byte[] bytes = toBytes.convert(boxed("A"),
                    StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(new byte[]{0x41, 0x00}, bytes);
        }

        @Test
        @DisplayName("UTF-16 + BIG_ENDIAN encodes 'A' without BOM as {0x00,0x41}")
        void utf16BigEndianEncodeNoBom() {
            byte[] bytes = toBytes.convert(boxed("A"),
                    StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(new byte[]{0x00, 0x41}, bytes);
        }

        @Test
        @DisplayName("UTF-16 + LITTLE_ENDIAN decodes {0x41,0x00} to 'A'")
        void utf16LittleEndianDecode() {
            byte[] bytes = {0x41, 0x00};
            assertArrayEquals(boxed("A"),
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16 + BIG_ENDIAN decodes {0x00,0x41} to 'A'")
        void utf16BigEndianDecode() {
            byte[] bytes = {0x00, 0x41};
            assertArrayEquals(boxed("A"),
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: Character[] -> bytes -> Character[]
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip Character[] -> bytes -> Character[]")
    class RoundTrip {

        static Stream<Arguments> samples() {
            return Stream.of(
                    Arguments.of("A"),
                    Arguments.of("Hello, World!"),
                    Arguments.of("café"),
                    Arguments.of("€中あ"),
                    Arguments.of("Mixed 123 é € 中 あ ~")
            );
        }

        @ParameterizedTest(name = "US-ASCII (ASCII-only sample) round-trip")
        @MethodSource("samples")
        void asciiOnlyRoundTrip(String source) {
            // Skip samples that contain non-ASCII characters for US-ASCII charset
            if (!source.chars().allMatch(c -> c < 0x80)) {
                return;
            }
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(input,
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-8 round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf8RoundTrip(String source) {
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(input,
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16BE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16BeRoundTrip(String source) {
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(input,
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16LE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16LeRoundTrip(String source) {
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(input,
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + BE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16UnqualifiedBeRoundTrip(String source) {
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);
            assertArrayEquals(input,
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + LE round-trip of \"{0}\"")
        @MethodSource("samples")
        void utf16UnqualifiedLeRoundTrip(String source) {
            Character[] input = boxed(source);
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(input,
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
        @DisplayName("convert(byte[]) uses the default charset (round-trip via convert(Character[]))")
        void toObjectSingleArgUsesDefaults() {
            Character[] input = boxed("Hello");
            byte[] bytes = toBytes.convert(input);
            assertArrayEquals(input, toObject.convert(bytes));
        }

        @Test
        @DisplayName("convert(byte[], charset) uses the native byte order")
        void toObjectTwoArgUsesNativeByteOrder() {
            Character[] input = boxed("Hi");
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_8);
            assertArrayEquals(input, toObject.convert(bytes, StandardCharsets.UTF_8));
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
            Character[] input = boxed("A");
            byte[] first = toBytes.convert(input, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            byte[] second = toBytes.convert(input, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertArrayEquals(first, second)
            );

            // Mutating the returned array must not affect later calls.
            first[0] = (byte) 0x42;
            assertArrayEquals(new byte[]{0x41},
                    toBytes.convert(input, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("returned Character[] is a fresh instance on every call")
        void returnsFreshCharArrayEachCall() {
            byte[] bytes = "AB".getBytes(StandardCharsets.US_ASCII);
            Character[] first =
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            Character[] second =
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertArrayEquals(first, second)
            );

            // Mutating the returned array must not affect later calls.
            first[0] = 'Z';
            assertArrayEquals(new Character[]{'A', 'B'},
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("length of decoded Character[] matches decoded string length (single-byte charset)")
        void decodedLengthMatchesSingleByte() {
            byte[] bytes = "Hello".getBytes(StandardCharsets.US_ASCII);
            Character[] result =
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertEquals(5, result.length);
        }

        @Test
        @DisplayName("length of encoded UTF-16BE byte[] equals 2 * input length for BMP chars")
        void encodedUtf16BeLength() {
            Character[] input = boxed("Hello");
            byte[] bytes = toBytes.convert(input, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            assertEquals(10, bytes.length);
        }
    }
}