package eu.oberon.oss.tools.converters.varlen.text;

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
 * Tests for the {@link TextToByteConverter} returned by {@link CharConverterProvider#getToByteConverter()}.
 */
class CharConverterProviderToByteTest {

    private final CharConverterProvider provider = new CharConverterProvider();
    private final TextToByteConverter<Character> toBytes = provider.getToByteConverter();
    private final TextToObjectConverter<Character> toObject = provider.getToObjectConverter();

    // ---------------------------------------------------------------------
    // Basic wiring
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Basic wiring")
    class Wiring {

        @Test
        @DisplayName("getToByteConverter() returns a non-null converter")
        void notNull() {
            assertNotNull(toBytes);
        }
    }

    @Nested
    @DisplayName("convert(Character, ByteOrder)")
    class ConvertCharacterWithByteOrder {

        @Test
        @DisplayName("BIG_ENDIAN delegates to default charset and BIG_ENDIAN")
        void bigEndianUsesDefaultCharset() {
            byte[] expected = toBytes.convert('A', Charset.defaultCharset(), ByteOrder.BIG_ENDIAN);

            byte[] result = toBytes.convert('A', ByteOrder.BIG_ENDIAN);

            assertArrayEquals(expected, result);
        }

        @Test
        @DisplayName("LITTLE_ENDIAN delegates to default charset and LITTLE_ENDIAN")
        void littleEndianUsesDefaultCharset() {
            byte[] expected = toBytes.convert('A', Charset.defaultCharset(), ByteOrder.LITTLE_ENDIAN);

            byte[] result = toBytes.convert('A', ByteOrder.LITTLE_ENDIAN);

            assertArrayEquals(expected, result);
        }

        @Test
        @DisplayName("different byte orders match the explicit default-charset overload")
        void byteOrderOnlyOverloadMatchesExplicitDefaultCharsetOverload() {
            assertAll(
                    () -> assertArrayEquals(
                            toBytes.convert('€', Charset.defaultCharset(), ByteOrder.BIG_ENDIAN),
                            toBytes.convert('€', ByteOrder.BIG_ENDIAN)
                    ),
                    () -> assertArrayEquals(
                            toBytes.convert('€', Charset.defaultCharset(), ByteOrder.LITTLE_ENDIAN),
                            toBytes.convert('€', ByteOrder.LITTLE_ENDIAN)
                    )
            );
        }

        @Test
        @DisplayName("null character is rejected")
        void rejectsNullCharacter() {
            assertThrows(NullPointerException.class,
                    () -> toBytes.convert(null, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("null byte order is rejected")
        void rejectsNullByteOrder() {
            assertThrows(NullPointerException.class,
                    () -> toBytes.convert('A', (ByteOrder) null));
        }
    }
    // ---------------------------------------------------------------------
    // Input validation
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation (Character -> bytes)")
    class InputValidation {

        @Test
        @DisplayName("null character is rejected on every overload")
        void rejectsNullCharacter() {
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
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert('A', (Charset) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> toBytes.convert('A', null, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("null byte order is rejected")
        void rejectsNullByteOrder() {
            assertThrows(NullPointerException.class,
                    () -> toBytes.convert('A', StandardCharsets.UTF_8, null));
        }
    }

    // ---------------------------------------------------------------------
    // Single-byte charsets: exact byte-level assertions
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Single-byte charsets (ASCII / Latin-1 / Windows-1252)")
    class SingleByteCharsets {

        @Test
        @DisplayName("US-ASCII: 'A' encodes to {0x41} regardless of byte order")
        void asciiIndependentOfByteOrder() {
            assertAll(
                    () -> assertArrayEquals(new byte[]{0x41},
                            toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x41},
                            toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{0x41},
                            toBytes.convert('A', StandardCharsets.US_ASCII))
            );
        }

        @Test
        @DisplayName("ISO-8859-1: 'é' encodes to {0xE9} regardless of byte order")
        void latin1IndependentOfByteOrder() {
            byte[] expected = {(byte) 0xE9};
            assertAll(
                    () -> assertArrayEquals(expected,
                            toBytes.convert('é', StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(expected,
                            toBytes.convert('é', StandardCharsets.ISO_8859_1, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Windows-1252: '€' encodes to {0x80}")
        void windows1252EuroSign() {
            Charset cp1252 = Charset.forName("windows-1252");
            assertAll(
                    () -> assertArrayEquals(new byte[]{(byte) 0x80},
                            toBytes.convert('€', cp1252, ByteOrder.BIG_ENDIAN)),
                    () -> assertArrayEquals(new byte[]{(byte) 0x80},
                            toBytes.convert('€', cp1252, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // UTF-8: variable-width, byte-order-independent
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-8 (variable-width, byte-order-independent)")
    class Utf8 {

        @Test
        @DisplayName("1-byte UTF-8: 'A' encodes to {0x41}")
        void asciiRangeSingleByte() {
            assertArrayEquals(new byte[]{0x41},
                    toBytes.convert('A', StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("2-byte UTF-8: 'é' encodes to {0xC3, 0xA9}")
        void twoByteSequence() {
            assertArrayEquals(new byte[]{(byte) 0xC3, (byte) 0xA9},
                    toBytes.convert('é', StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("3-byte UTF-8: '€' encodes to {0xE2, 0x82, 0xAC}")
        void threeByteSequence() {
            assertArrayEquals(new byte[]{(byte) 0xE2, (byte) 0x82, (byte) 0xAC},
                    toBytes.convert('€', StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-8: byte order parameter does not affect the encoded bytes")
        void byteOrderIrrelevant() {
            byte[] be = toBytes.convert('é', StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert('é', StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN);
            assertArrayEquals(be, le);
        }
    }

    // ---------------------------------------------------------------------
    // Endian-qualified UTF-16 (BE / LE): exact byte layout is fixed by the charset
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-16BE / UTF-16LE (byte order embedded in the charset)")
    class Utf16Qualified {

        @Test
        @DisplayName("UTF-16BE: 'A' encodes to {0x00, 0x41}")
        void utf16BeEncodesA() {
            assertArrayEquals(new byte[]{0x00, 0x41},
                    toBytes.convert('A', StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: 'A' encodes to {0x41, 0x00}")
        void utf16LeEncodesA() {
            assertArrayEquals(new byte[]{0x41, 0x00},
                    toBytes.convert('A', StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16BE: '€' encodes to {0x20, 0xAC}")
        void utf16BeEncodesEuro() {
            assertArrayEquals(new byte[]{0x20, (byte) 0xAC},
                    toBytes.convert('€', StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: '€' encodes to {0xAC, 0x20}")
        void utf16LeEncodesEuro() {
            assertArrayEquals(new byte[]{(byte) 0xAC, 0x20},
                    toBytes.convert('€', StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("BE and LE encodings of the same character are exact byte-reverses")
        void beAndLeAreByteReversed() {
            byte[] be = toBytes.convert('€', StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            byte[] le = toBytes.convert('€', StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN);
            byte[] reversedLe = {le[1], le[0]};
            assertArrayEquals(be, reversedLe);
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip: Character -> bytes -> Character (via the sibling to-object converter)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip Character -> bytes -> Character")
    class RoundTrip {

        static Stream<Arguments> asciiChars() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('z'),
                    Arguments.of('0'),
                    Arguments.of('~')
            );
        }

        @ParameterizedTest(name = "US-ASCII round trip of ''{0}''")
        @MethodSource("asciiChars")
        void asciiRoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        static Stream<Arguments> latin1Chars() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('é'),
                    Arguments.of('ñ'),
                    Arguments.of('ü')
            );
        }

        @ParameterizedTest(name = "ISO-8859-1 round trip of ''{0}''")
        @MethodSource("latin1Chars")
        void latin1RoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN));
        }

        static Stream<Arguments> multiByteChars() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('é'),
                    Arguments.of('€'),
                    Arguments.of('中'),
                    Arguments.of('あ')
            );
        }

        @ParameterizedTest(name = "UTF-8 round trip of ''{0}''")
        @MethodSource("multiByteChars")
        void utf8RoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16BE round trip of ''{0}''")
        @MethodSource("multiByteChars")
        void utf16BeRoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16LE round trip of ''{0}''")
        @MethodSource("multiByteChars")
        void utf16LeRoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + BIG_ENDIAN round trip of ''{0}''")
        @MethodSource("multiByteChars")
        void utf16UnqualifiedBeRoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16 (unqualified) + LITTLE_ENDIAN round trip of ''{0}''")
        @MethodSource("multiByteChars")
        void utf16UnqualifiedLeRoundTrip(char value) {
            byte[] bytes = toBytes.convert(value, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // Output invariants
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Output invariants")
    class OutputInvariants {

        @Test
        @DisplayName("returned array is a fresh instance on every call (no shared state)")
        void returnsFreshArrayEachCall() {
            byte[] first = toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            byte[] second = toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN);
            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertArrayEquals(first, second)
            );

            // Mutating the returned array must not affect calls made after this call.
            first[0] = (byte) 0x42;
            assertArrayEquals(new byte[]{0x41},
                    toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("single-byte charset always produces exactly 1 byte for a representable char")
        void singleByteLengthIsOne() {
            assertAll(
                    () -> assertEquals(1,
                            toBytes.convert('A', StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(1,
                            toBytes.convert('é', StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN).length)
            );
        }

        @Test
        @DisplayName("UTF-16BE / UTF-16LE always produce exactly 2 bytes for a BMP char")
        void utf16QualifiedLengthIsTwo() {
            assertAll(
                    () -> assertEquals(2,
                            toBytes.convert('A', StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN).length),
                    () -> assertEquals(2,
                            toBytes.convert('€', StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN).length)
            );
        }
    }


}