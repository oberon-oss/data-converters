package eu.oberon.oss.tools.converters.varlen;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.varlen.text.AbstractTextConverterProvider;
import eu.oberon.oss.tools.converters.varlen.text.TextToByteConverter;
import eu.oberon.oss.tools.converters.varlen.text.TextToObjectConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class AbstractTextConverterProviderTest {

    private static final class TestTextProvider extends AbstractTextConverterProvider<StringBuilder> {

        private TestTextProvider() {
            super(ValueTypeNames.STRING);
        }

        @Override
        protected StringBuilder fromString(String value) {
            return new StringBuilder(value);
        }

        @Override
        protected String toStringValue(StringBuilder value) {
            return value.toString();
        }
    }

    private final TestTextProvider provider = new TestTextProvider();

    @Nested
    @DisplayName("Provider metadata")
    class Metadata {

        @Test
        @DisplayName("reports the configured value type name")
        void reportsConfiguredValueTypeName() {
            assertEquals(ValueTypeNames.STRING.name(), provider.getValueTypeName());
        }
    }

    @Nested
    @DisplayName("Converter getter bridge")
    class ConverterGetterBridge {

        @Test
        @DisplayName("getToObjectConverter() returns a TextToObjectConverter")
        void getToObjectConverterReturnsTextConverter() {
            TextToObjectConverter<StringBuilder> converter = provider.getToObjectConverter();

            assertNotNull(converter);
        }

        @Test
        @DisplayName("getToByteConverter() returns a TextToByteConverter")
        void getToByteConverterReturnsTextConverter() {
            TextToByteConverter<StringBuilder> converter = provider.getToByteConverter();

            assertNotNull(converter);
        }

        @Test
        @DisplayName("to-object converter is also usable as a var-len converter")
        void toObjectConverterIsAlsoVarLenConverter() {
            VarLenToObjectConverter<StringBuilder> converter = provider.getToObjectConverter();

            assertEquals("ABC", converter.convert(new byte[]{'A', 'B', 'C'}, ByteOrder.BIG_ENDIAN).toString());
        }

        @Test
        @DisplayName("to-byte converter is also usable as a var-len converter")
        void toByteConverterIsAlsoVarLenConverter() {
            VarLenToByteConverter<StringBuilder> converter = provider.getToByteConverter();

            assertArrayEquals(
                    "ABC".getBytes(StandardCharsets.UTF_8),
                    converter.convert(new StringBuilder("ABC"), ByteOrder.BIG_ENDIAN)
            );
        }

        @Test
        @DisplayName("typed text getter and var-len super getter return the same to-object instance")
        void textAndVarLenToObjectGetterReturnSameInstance() {
            TextToObjectConverter<StringBuilder> textConverter = provider.getToObjectConverter();
            VarLenToObjectConverter<StringBuilder> varLenConverter = provider.getToObjectConverter();

            assertSame(textConverter, varLenConverter);
        }

        @Test
        @DisplayName("typed text getter and var-len super getter return the same to-byte instance")
        void textAndVarLenToByteGetterReturnSameInstance() {
            TextToByteConverter<StringBuilder> textConverter = provider.getToByteConverter();
            VarLenToByteConverter<StringBuilder> varLenConverter = provider.getToByteConverter();

            assertSame(textConverter, varLenConverter);
        }

        @Test
        @DisplayName("to-object converter is cached")
        void toObjectConverterIsCached() {
            assertSame(provider.getToObjectConverter(), provider.getToObjectConverter());
        }

        @Test
        @DisplayName("to-byte converter is cached")
        void toByteConverterIsCached() {
            assertSame(provider.getToByteConverter(), provider.getToByteConverter());
        }
    }

    @Nested
    @DisplayName("Text conversion behavior")
    class TextConversionBehavior {

        @Test
        @DisplayName("bytes are decoded and passed through fromString")
        void bytesAreDecodedAndPassedThroughFromString() {
            StringBuilder result = provider.getToObjectConverter()
                    .convert("Hello".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals("Hello", result.toString())
            );
        }

        @Test
        @DisplayName("object is passed through toStringValue and encoded")
        void objectIsPassedThroughToStringValueAndEncoded() {
            byte[] result = provider.getToByteConverter()
                    .convert(new StringBuilder("Hello"), StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN);

            assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), result);
        }

        @Test
        @DisplayName("UTF-16 unqualified big-endian encoding does not emit BOM")
        void utf16UnqualifiedBigEndianEncodingDoesNotEmitBom() {
            byte[] result = provider.getToByteConverter()
                    .convert(new StringBuilder("A"), StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);

            assertArrayEquals(new byte[]{0x00, 0x41}, result);
        }

        @Test
        @DisplayName("UTF-16 unqualified little-endian encoding does not emit BOM")
        void utf16UnqualifiedLittleEndianEncodingDoesNotEmitBom() {
            byte[] result = provider.getToByteConverter()
                    .convert(new StringBuilder("A"), StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);

            assertArrayEquals(new byte[]{0x41, 0x00}, result);
        }

        @Test
        @DisplayName("UTF-16 unqualified big-endian bytes decode correctly")
        void utf16UnqualifiedBigEndianBytesDecodeCorrectly() {
            StringBuilder result = provider.getToObjectConverter()
                    .convert(new byte[]{0x00, 0x41}, StandardCharsets.UTF_16, ByteOrder.BIG_ENDIAN);

            assertEquals("A", result.toString());
        }

        @Test
        @DisplayName("UTF-16 unqualified little-endian bytes decode correctly")
        void utf16UnqualifiedLittleEndianBytesDecodeCorrectly() {
            StringBuilder result = provider.getToObjectConverter()
                    .convert(new byte[]{0x41, 0x00}, StandardCharsets.UTF_16, ByteOrder.LITTLE_ENDIAN);

            assertEquals("A", result.toString());
        }
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @Test
        @DisplayName("to-object converter rejects null byte array")
        void toObjectConverterRejectsNullByteArray() {
            TextToObjectConverter<StringBuilder> converter = provider.getToObjectConverter();

            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> converter.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(null, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("to-object converter rejects null charset")
        void toObjectConverterRejectsNullCharset() {
            TextToObjectConverter<StringBuilder> converter = provider.getToObjectConverter();
            byte[] input = "A".getBytes(StandardCharsets.UTF_8);

            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(input, (Charset) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(input, null, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("to-object converter rejects null byte order")
        void toObjectConverterRejectsNullByteOrder() {
            TextToObjectConverter<StringBuilder> converter = provider.getToObjectConverter();
            byte[] input = "A".getBytes(StandardCharsets.UTF_8);

            assertThrows(NullPointerException.class,
                    () -> converter.convert(input, StandardCharsets.UTF_8, null));
        }

        @Test
        @DisplayName("to-byte converter rejects null value")
        void toByteConverterRejectsNullValue() {
            TextToByteConverter<StringBuilder> converter = provider.getToByteConverter();

            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> converter.convert(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(null, StandardCharsets.UTF_8)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(null, StandardCharsets.UTF_8, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("to-byte converter rejects null charset")
        void toByteConverterRejectsNullCharset() {
            TextToByteConverter<StringBuilder> converter = provider.getToByteConverter();
            StringBuilder input = new StringBuilder("A");

            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(input, (Charset) null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> converter.convert(input, null, ByteOrder.BIG_ENDIAN))
            );
        }

        @Test
        @DisplayName("to-byte converter rejects null byte order")
        void toByteConverterRejectsNullByteOrder() {
            TextToByteConverter<StringBuilder> converter = provider.getToByteConverter();
            StringBuilder input = new StringBuilder("A");

            assertThrows(NullPointerException.class,
                    () -> converter.convert(input, StandardCharsets.UTF_8, null));
        }
    }
}