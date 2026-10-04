package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of 16-bit signed short values to and from byte arrays. This class provides converters for handling
 * fixed-length binary representations of signed short values, ensuring compatibility with different byte orders.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class SignedShortConverterProvider extends AbstractFixedConverterProvider<Short> {
    private final FixedToObjectConverter<Short> toObjectConverter;
    private final FixedToByteConverter<Short> toByteConverter;

    /**
     * Default constructor.
     *
     * @since 1.0.0
     */
    public SignedShortConverterProvider() {
        super(ValueTypeNames.SIGNED_SHORT, Short.class, 2);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Short convert(byte[] bytes) {
                checkByteArraySize(bytes);
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public Short convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    return (short) (((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF));
                } else {
                    return (short) (((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF));
                }
            }
        };
        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Short object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Short object, ByteOrder byteOrder) {
                byte[] bytes = new byte[2];
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    bytes[0] = (byte) (object & 0xFF);
                    bytes[1] = (byte) ((object >> 8) & 0xFF);
                } else {
                    bytes[0] = (byte) ((object >> 8) & 0xFF);
                    bytes[1] = (byte) (object & 0xFF);
                }
                return bytes;
            }
        };
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public FixedToObjectConverter<Short> getToObjectConverter() {
        return toObjectConverter;

    }

    @Override
    public FixedToByteConverter<Short> getToByteConverter() {
        return toByteConverter;
    }
}