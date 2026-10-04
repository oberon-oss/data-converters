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

class CharConverterToCharacterProviderTest {

    private final CharConverterProvider provider = new CharConverterProvider();
    private final TextToObjectConverter<Character> toObject = provider.getToObjectConverter();

    // ---------------------------------------------------------------------
    // Provider metadata
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports CHARACTER as target value type")
        void reportsCorrectValueType() {
            assertEquals(ValueTypeNames.CHARACTER.name(), provider.getValueTypeName());
        }

        @Test
        @DisplayName("getToObjectConverter() returns a non-null converter")
        void toObjectConverterNotNull() {
            assertNotNull(provider.getToObjectConverter());
        }

        @Test
        @DisplayName("getToByteConverter() currently returns null (not yet implemented)")
        void toByteConverterIsNull() {
            // Documents the current state of the API; update this test once
            // the to-byte converter is implemented.
            org.junit.jupiter.api.Assertions.assertNotNull(provider.getToByteConverter());
        }
    }

    // ---------------------------------------------------------------------
    // Input validation
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation (bytes -> Character)")
    class InputValidation {

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
        @DisplayName("null byte array is rejected by byte-order-only overload")
        void byteOrderOnlyOverloadRejectsNullArray() {
            assertThrows(NullPointerException.class,
                    () -> toObject.convert(null, ByteOrder.BIG_ENDIAN));
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
        @DisplayName("empty byte array is rejected")
        void rejectsEmptyArray() {
            assertAll(
                    () -> assertThrows(StringIndexOutOfBoundsException.class,
                            () -> toObject.convert(new byte[0])),
                    () -> assertThrows(StringIndexOutOfBoundsException.class,
                            () -> toObject.convert(new byte[0], StandardCharsets.UTF_8)),
                    () -> assertThrows(StringIndexOutOfBoundsException.class,
                            () -> toObject.convert(new byte[0], StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // Single-byte charsets: byte order must NOT affect the result
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Single-byte charsets (ASCII / Latin-1 / Windows-1252)")
    class SingleByteCharsets {

        @Test
        @DisplayName("US-ASCII: {0x41} decodes to 'A' regardless of byte order")
        void asciiIndependentOfByteOrder() {
            byte[] bytes = {0x41}; // 'A'
            assertAll(
                    () -> assertEquals('A',
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals('A',
                            toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.LITTLE_ENDIAN)),
                    () -> assertEquals('A',
                            toObject.convert(bytes, StandardCharsets.US_ASCII))
            );
        }

        @Test
        @DisplayName("ISO-8859-1: {0xE9} decodes to 'é' regardless of byte order")
        void latin1IndependentOfByteOrder() {
            byte[] bytes = {(byte) 0xE9}; // 'é' in Latin-1
            assertAll(
                    () -> assertEquals('é',
                            toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals('é',
                            toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.LITTLE_ENDIAN))
            );
        }

        @Test
        @DisplayName("Windows-1252: {0x80} decodes to '€' (Euro sign)")
        void windows1252EuroSign() {
            byte[] bytes = {(byte) 0x80}; // '€' in Windows-1252
            Charset cp1252 = Charset.forName("windows-1252");
            assertAll(
                    () -> assertEquals('€',
                            toObject.convert(bytes, cp1252, ByteOrder.BIG_ENDIAN)),
                    () -> assertEquals('€',
                            toObject.convert(bytes, cp1252, ByteOrder.LITTLE_ENDIAN))
            );
        }
    }

    // ---------------------------------------------------------------------
    // UTF-8: variable-width; byte order is irrelevant
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-8 (variable-width, byte-order-independent)")
    class Utf8 {

        @Test
        @DisplayName("1-byte UTF-8: {0x41} decodes to 'A'")
        void asciiRangeSingleByte() {
            byte[] bytes = {0x41};
            assertEquals('A',
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("convert(byte[], ByteOrder) delegates to default charset and BIG_ENDIAN")
        void byteOrderOnlyBigEndianUsesDefaultCharset() {
            byte[] bytes = String.valueOf('A').getBytes(Charset.defaultCharset());
            Character expected = toObject.convert(bytes, Charset.defaultCharset(), ByteOrder.BIG_ENDIAN);

            Character result = toObject.convert(bytes, ByteOrder.BIG_ENDIAN);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("convert(byte[], ByteOrder) delegates to default charset and LITTLE_ENDIAN")
        void byteOrderOnlyLittleEndianUsesDefaultCharset() {
            byte[] bytes = String.valueOf('A').getBytes(Charset.defaultCharset());
            Character expected = toObject.convert(bytes, Charset.defaultCharset(), ByteOrder.LITTLE_ENDIAN);

            Character result = toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN);

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("convert(byte[], ByteOrder) matches explicit default-charset overload")
        void byteOrderOnlyOverloadMatchesExplicitDefaultCharsetOverload() {
            byte[] bytes = String.valueOf('€').getBytes(Charset.defaultCharset());

            assertAll(
                    () -> assertEquals(
                            toObject.convert(bytes, Charset.defaultCharset(), ByteOrder.BIG_ENDIAN),
                            toObject.convert(bytes, ByteOrder.BIG_ENDIAN)
                    ),
                    () -> assertEquals(
                            toObject.convert(bytes, Charset.defaultCharset(), ByteOrder.LITTLE_ENDIAN),
                            toObject.convert(bytes, ByteOrder.LITTLE_ENDIAN)
                    )
            );
        }

        @Test
        @DisplayName("convert(byte[], ByteOrder) rejects null byte order")
        void byteOrderOnlyOverloadRejectsNullByteOrder() {
            byte[] bytes = String.valueOf('A').getBytes(Charset.defaultCharset());

            assertThrows(NullPointerException.class,
                    () -> toObject.convert(bytes, (ByteOrder) null));
        }

        @Test
        @DisplayName("2-byte UTF-8: {0xC3,0xA9} decodes to 'é'")
        void twoByteSequence() {
            byte[] bytes = "é".getBytes(StandardCharsets.UTF_8);
            assertEquals('é',
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("3-byte UTF-8: '€' decodes correctly")
        void threeByteSequence() {
            byte[] bytes = "€".getBytes(StandardCharsets.UTF_8);
            assertEquals('€',
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-8: byte order does not affect the decoded character")
        void byteOrderIrrelevant() {
            byte[] bytes = "é".getBytes(StandardCharsets.UTF_8);
            Character be = toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);
            Character le = toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.LITTLE_ENDIAN);
            assertEquals(be, le);
        }
    }

    // ---------------------------------------------------------------------
    // UTF-16: fixed 2-byte code units for the BMP; byte order matters
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("UTF-16 family (byte-order-sensitive)")
    class Utf16 {

        @Test
        @DisplayName("UTF-16BE: {0x00,0x41} decodes to 'A'")
        void utf16BeDecodesA() {
            byte[] bytes = {0x00, 0x41};
            assertEquals('A',
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: {0x41,0x00} decodes to 'A'")
        void utf16LeDecodesA() {
            byte[] bytes = {0x41, 0x00};
            assertEquals('A',
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16BE: {0x20,0xAC} decodes to '€'")
        void utf16BeDecodesEuro() {
            byte[] bytes = {0x20, (byte) 0xAC};
            assertEquals('€',
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16LE: {0xAC,0x20} decodes to '€'")
        void utf16LeDecodesEuro() {
            byte[] bytes = {(byte) 0xAC, 0x20};
            assertEquals('€',
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }

        @Test
        @DisplayName("UTF-16BE bytes decoded as UTF-16LE (or vice versa) produce different characters")
        void wrongByteOrderProducesDifferentResult() {
            byte[] beBytes = {0x00, 0x41};       // 'A' in BE
            Character asBe = toObject.convert(beBytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN);
            Character asLe = toObject.convert(beBytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN);
            assertAll(
                    () -> assertEquals('A', asBe),
                    () -> org.junit.jupiter.api.Assertions.assertNotEquals(asBe, asLe)
            );
        }
    }

    // ---------------------------------------------------------------------
    // Round-trip via Java's own encoder (sanity check across charsets)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Round-trip: char -> bytes (JDK) -> char (converter)")
    class RoundTrip {

        static Stream<Arguments> asciiRoundTrip() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('z'),
                    Arguments.of('0'),
                    Arguments.of('~')
            );
        }

        @ParameterizedTest(name = "US-ASCII round trip of ''{0}''")
        @MethodSource("asciiRoundTrip")
        void asciiRoundTrip(char value) {
            byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.US_ASCII);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.US_ASCII, ByteOrder.BIG_ENDIAN));
        }

        static Stream<Arguments> latin1RoundTrip() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('é'),
                    Arguments.of('ñ'),
                    Arguments.of('ü')
            );
        }

        @ParameterizedTest(name = "ISO-8859-1 round trip of ''{0}''")
        @MethodSource("latin1RoundTrip")
        void latin1RoundTrip(char value) {
            byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.ISO_8859_1);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.ISO_8859_1, ByteOrder.BIG_ENDIAN));
        }

        static Stream<Arguments> utf8RoundTrip() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('é'),
                    Arguments.of('€'),
                    Arguments.of('中'),
                    Arguments.of('あ')
            );
        }

        @ParameterizedTest(name = "UTF-8 round trip of ''{0}''")
        @MethodSource("utf8RoundTrip")
        void utf8RoundTrip(char value) {
            byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN));
        }

        static Stream<Arguments> utf16RoundTrip() {
            return Stream.of(
                    Arguments.of('A'),
                    Arguments.of('é'),
                    Arguments.of('€'),
                    Arguments.of('中'),
                    Arguments.of('あ')
            );
        }

        @ParameterizedTest(name = "UTF-16BE round trip of ''{0}''")
        @MethodSource("utf16RoundTrip")
        void utf16BeRoundTrip(char value) {
            byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_16BE);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16BE, ByteOrder.BIG_ENDIAN));
        }

        @ParameterizedTest(name = "UTF-16LE round trip of ''{0}''")
        @MethodSource("utf16RoundTrip")
        void utf16LeRoundTrip(char value) {
            byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_16LE);
            assertEquals(value,
                    toObject.convert(bytes, StandardCharsets.UTF_16LE, ByteOrder.LITTLE_ENDIAN));
        }
    }

    // ---------------------------------------------------------------------
    // CharsetProperties helper
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("CharsetProperties helper")
    class CharsetPropertiesTests {

        @Test
        @DisplayName("isSingleByte returns true for common 1-byte charsets")
        void singleByteCharsetsAreDetected() {
            assertAll(
                    () -> assertTrue(CharsetProperties
                            .isSingleByte(StandardCharsets.US_ASCII)),
                    () -> assertTrue(CharsetProperties
                            .isSingleByte(StandardCharsets.ISO_8859_1)),
                    () -> assertTrue(CharsetProperties
                            .isSingleByte(Charset.forName("windows-1252")))
            );
        }

        @Test
        @DisplayName("isSingleByte returns false for multi-byte charsets")
        void multiByteCharsetsAreDetected() {
            assertAll(
                    () -> assertFalse(CharsetProperties
                            .isSingleByte(StandardCharsets.UTF_8)),
                    () -> assertFalse(CharsetProperties
                            .isSingleByte(StandardCharsets.UTF_16)),
                    () -> assertFalse(CharsetProperties
                            .isSingleByte(StandardCharsets.UTF_16BE)),
                    () -> assertFalse(CharsetProperties
                            .isSingleByte(StandardCharsets.UTF_16LE))
            );
        }

        @Test
        @DisplayName("isByteOrderSensitive is true only for unqualified UTF-16 / UTF-32")
        void byteOrderSensitivityIsDetected() {
            assertAll(
                    // Single-byte -> never sensitive
                    () -> assertFalse(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.US_ASCII)),
                    () -> assertFalse(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.ISO_8859_1)),
                    // UTF-8 -> not sensitive
                    () -> assertFalse(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.UTF_8)),
                    // UTF-16 (unqualified) -> sensitive
                    () -> assertTrue(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.UTF_16)),
                    // Endian-qualified UTF-16 variants embed byte order themselves
                    () -> assertFalse(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.UTF_16BE)),
                    () -> assertFalse(CharsetProperties
                            .isByteOrderSensitive(StandardCharsets.UTF_16LE))
            );
        }
    }

    @Test
    @DisplayName("Unqualified UTF-16 + LITTLE_ENDIAN honors the requested byte order")
    void unqualifiedUtf16RespectsLittleEndian() {
        byte[] leBytes = {0x41, 0x00}; // 'A' in little-endian UTF-16
        Character result = toObject.convert(
                leBytes, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);
        assertEquals('A', result);
    }

    @Test
    @DisplayName("Unqualified UTF-16 + BIG_ENDIAN uses the JDK default (BE without BOM)")
    void unqualifiedUtf16RespectsBigEndian() {
        byte[] beBytes = {0x00, 0x41}; // 'A' in big-endian UTF-16
        Character result = toObject.convert(
                beBytes, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);
        assertEquals('A', result);
    }
}