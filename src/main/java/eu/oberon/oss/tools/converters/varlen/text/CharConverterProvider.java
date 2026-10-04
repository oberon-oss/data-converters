package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.AbstractConverterProvider;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Converter provider for the {@code Char} value type.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class CharConverterProvider extends AbstractConverterProvider implements TextConverterProvider<Character> {

    /**
     * Constructs a new instance of {@code CharConverterProvider}.
     *
     * @since 1.0.0
     */
    public CharConverterProvider() {
        super(ValueTypeNames.CHARACTER);
    }

    @Override
    public Class<Character> getTypeClass() {
        return Character.class;
    }

    @Override
    public TextToObjectConverter<Character> getToObjectConverter() {
        return new TextToObjectConverter<>() {
            @Override
            public Character convert(byte[] input) {
                return convert(input, Charset.defaultCharset());
            }

            @Override
            public Character convert(byte[] input, Charset charset) {
                return convert(input, charset, ByteOrder.nativeOrder());
            }

            @Override
            public Character convert(byte[] input, ByteOrder byteOrder) {
                return convert(input, Charset.defaultCharset(), byteOrder);
            }

            @Override
            public Character convert(byte[] input, Charset charset, ByteOrder byteOrder) {
                CharsetProperties.testParameters(input, charset, byteOrder);
                boolean isEndianSensitive = CharsetProperties.isByteOrderSensitive(charset);
                byte[] effective = input;

                if (isEndianSensitive && byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    // Prepend a LE BOM so the charset decoder interprets bytes correctly.
                    effective = new byte[input.length + 2];
                    effective[0] = (byte) 0xFF;
                    effective[1] = (byte) 0xFE;
                    System.arraycopy(input, 0, effective, 2, input.length);
                }

                String str = new String(effective, charset);
                return str.charAt(0);
            }
        };

    }

    @Override
    public TextToByteConverter<Character> getToByteConverter() {
        return new TextToByteConverter<>() {
            @Override
            public byte[] convert(Character input) {
                return convert(input, Charset.defaultCharset(), ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(Character input, Charset charset) {
                return convert(input, charset, ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(Character input, ByteOrder byteOrder) {
                return convert(input, Charset.defaultCharset(), byteOrder);
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Character input, Charset charset, ByteOrder byteOrder) {
                CharsetProperties.testParameters(input, charset, byteOrder);
                Charset effective = charset;
                if (CharsetProperties.isByteOrderSensitive(charset)) {
                    // Replace unqualified UTF-16 / UTF-32 with an endian-qualified variant
                    // so the JDK encoder emits bytes in the requested order WITHOUT a BOM.
                    String base = charset.name().equalsIgnoreCase("UTF-16") ? "UTF-16" : "UTF-32";
                    effective = Charset.forName(base + (byteOrder == ByteOrder.LITTLE_ENDIAN ? "LE" : "BE"));
                }
                return String.valueOf(input).getBytes(effective);
            }
        };
    }
}
