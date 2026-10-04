package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * A specialized converter provider for handling unsigned 16-bit integer (unsigned short) conversions between byte arrays and their corresponding
 * {@link Integer} representations. This provider enforces a fixed byte array size of 2.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class UnsignedShortConverterProvider extends AbstractFixedConverterProvider<Integer> {

    /**
     * The maximum value of an unsigned short (2^16 - 1).
     *
     * @since 1.0.0
     */
    public static final Integer MAX_UNSIGNED_SHORT_VALUE = 0xFFFF;

    private final FixedToObjectConverter<Integer> toObjectConverter;
    private final FixedToByteConverter<Integer> toByteConverter;

    /**
     * Constructs a new instance of the {@link UnsignedShortConverterProvider}.
     *
     * @since 1.0.0
     */
    public UnsignedShortConverterProvider() {
        super(ValueTypeNames.UNSIGNED_SHORT, Integer.class, 2);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Integer convert(byte[] bytes) {
                checkByteArraySize(bytes);
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public Integer convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    return ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
                } else {
                    return ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
                }
            }
        };

        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Integer object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Integer object, ByteOrder byteOrder) {
                if (object < 0 || object > MAX_UNSIGNED_SHORT_VALUE) {
                    throw new IllegalArgumentException("Value exceeds unsigned short range (0..65536): " + object);
                }
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
    public FixedToObjectConverter<Integer> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Integer> getToByteConverter() {
        return toByteConverter;
    }
}