package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of unsigned byte values to and from byte arrays.
 * <p>
 * This class extends the {@link AbstractFixedConverterProvider} using a value type of {@code Integer}.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class UnsignedByteConverterProvider extends AbstractFixedConverterProvider<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnsignedByteConverterProvider.class);

    /**
     * The maximum value for an unsigned byte.
     *
     * @since 1.0.0
     */
    public static final int MAX_UNSIGNED_BYTE_VALUE = 0xFF;

    private final FixedToObjectConverter<Integer> toObjectConverter;
    private final FixedToByteConverter<Integer> toByteConverter;

    /**
     * Constructs a new instance of the {@link UnsignedByteConverterProvider} class.
     *
     * @since 1.0.0
     */
    public UnsignedByteConverterProvider() {
        LOGGER.debug("Any byte order parameter will be ignored, and methods will behave the same as the ones with a byte order parameter");
        super(ValueTypeNames.UNSIGNED_BYTE, Integer.class, 1);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Integer convert(byte[] bytes) {
                return bytes[0] & 0xFF;
            }

            @Override
            public Integer convert(byte[] bytes, ByteOrder byteOrder) {
                return convert(bytes);
            }
        };

        toByteConverter = new FixedToByteConverter<>() {

            @Override
            public byte[] convert(Integer object) {
                if (object < 0 || object > MAX_UNSIGNED_BYTE_VALUE) {
                    throw new IllegalArgumentException("Value out of range (0..255) for unsigned byte: " + object);
                }
                return new byte[]{object.byteValue()};
            }

            @Override
            public byte[] convert(Integer object, ByteOrder byteOrder) {
                return convert(object);
            }
        };
    }

    @Override
    public FixedToObjectConverter<Integer> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Integer> getToByteConverter() {
        return toByteConverter;
    }
}
