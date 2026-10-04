package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.varlen.AbstractVarLenConverterProvider;
import eu.oberon.oss.tools.converters.varlen.VarLenToByteConverter;
import eu.oberon.oss.tools.converters.varlen.VarLenToObjectConverter;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Base class for text converter providers that convert between {@code byte[]} and some text-like Java type {@code T} (e.g. {@link String},
 * {@code Character[]}).
 * <p>
 * Character set and/or byte-order handling is done here. Subclasses only need to describe how their type maps to and from a plain {@link String} via
 * {@link #fromString(String)} and {@link #toStringValue(Object)}.
 *
 * @param <T> The target type of the converter.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractTextConverterProvider<T> extends AbstractVarLenConverterProvider<T> implements TextConverterProvider<T> {

    /**
     * Constructor.
     *
     * @param valueType The value name type as passed from the extending classes.
     *
     * @since 1.0.0
     */
    protected AbstractTextConverterProvider(ValueTypeNames valueType) {
        super(valueType);
    }

    /**
     * Constructor with type class.
     *
     * @param valueType The value name type as passed from the extending classes.
     * @param typeClass The class type of the target object.
     *
     * @since 1.0.0
     */
    protected AbstractTextConverterProvider(ValueTypeNames valueType, Class<T> typeClass) {
        super(valueType, typeClass);
    }

    /**
     * Convert the decoded {@link String} to the provider's target type.
     *
     * @param value The value to be converted.
     *
     * @return The resulting value.
     *
     * @since 1.0.0
     */
    protected abstract T fromString(String value);

    /**
     * Convert the provider's target type to a {@link String} for encoding.
     *
     * @param value The value to be converted.
     *
     * @return The resulting {@link String}.
     *
     * @since 1.0.0
     */
    protected abstract String toStringValue(T value);

    @Override
    protected VarLenToObjectConverter<T> createToObjectConverter() {
        return new TextToObjectConverter<>() {
            @Override
            public T convert(byte[] input) {
                return convert(input, Charset.defaultCharset(), ByteOrder.nativeOrder());
            }

            @Override
            public T convert(byte[] input, Charset charset) {
                return convert(input, charset, ByteOrder.nativeOrder());
            }

            @Override
            public T convert(byte[] input, ByteOrder byteOrder) {
                return convert(input, Charset.defaultCharset(), byteOrder);
            }

            @Override
            public T convert(byte[] input, Charset charset, ByteOrder byteOrder) {
                CharsetProperties.testParameters(input, charset, byteOrder);

                byte[] effective = input;
                if (CharsetProperties.isByteOrderSensitive(charset) && byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    // Prepend a LE BOM so the charset decoder interprets bytes correctly.
                    effective = new byte[input.length + 2];
                    effective[0] = (byte) 0xFF;
                    effective[1] = (byte) 0xFE;
                    System.arraycopy(input, 0, effective, 2, input.length);
                }
                return fromString(new String(effective, charset));
            }
        };
    }

    @Override
    protected VarLenToByteConverter<T> createToByteConverter() {
        return new TextToByteConverter<>() {
            @Override
            public byte[] convert(T input) {
                return convert(input, Charset.defaultCharset(), ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(T input, Charset charset) {
                return convert(input, charset, ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(T input, ByteOrder byteOrder) {
                return convert(input, Charset.defaultCharset(), byteOrder);
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(T input, Charset charset, ByteOrder byteOrder) {
                CharsetProperties.testParameters(input, charset, byteOrder);
                Charset effective = charset;
                if (CharsetProperties.isByteOrderSensitive(charset)) {
                    // Replace unqualified UTF-16 / UTF-32 with an endian-qualified variant
                    // so the JDK encoder emits bytes in the requested order WITHOUT a BOM.
                    String base = charset.name().equalsIgnoreCase("UTF-16") ? "UTF-16" : "UTF-32";
                    effective = Charset.forName(base + (byteOrder == ByteOrder.LITTLE_ENDIAN ? "LE" : "BE"));
                }
                return toStringValue(input).getBytes(effective);
            }
        };
    }

    @Override
    public final TextToObjectConverter<T> getToObjectConverter() {
        return (TextToObjectConverter<T>) super.getToObjectConverter();
    }

    @Override
    public final TextToByteConverter<T> getToByteConverter() {
        return (TextToByteConverter<T>) super.getToByteConverter();
    }
}