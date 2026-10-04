package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of unsigned integer values to and from byte arrays.
 * <p>
 * This class extends the {@link AbstractFixedConverterProvider} using a value type of {@code Long}.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class UnsignedIntegerConverterProvider extends AbstractFixedConverterProvider<Long> {

    /**
     * The maximum value that can be represented by an unsigned integer (2³32-1).
     *
     * @since 1.0.0
     */
    public static final long MAX_UNSIGNED_INT_VALUE = 0xFFFF_FFFFL;

    private final FixedToObjectConverter<Long> toObjectConverter;
    private final FixedToByteConverter<Long> toByteConverter;

    /**
     * Constructs a new instance of the {@link UnsignedIntegerConverterProvider} class.
     *
     * @since 1.0.0
     */
    public UnsignedIntegerConverterProvider() {
        super(ValueTypeNames.UNSIGNED_INTEGER, Long.class, 4);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Long convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public Long convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    return ((bytes[0] & 0xFFL) << 24) |
                            ((bytes[1] & 0xFFL) << 16) |
                            ((bytes[2] & 0xFFL) << 8) |
                            (bytes[3] & 0xFFL);
                } else {
                    return (bytes[0] & 0xFFL) |
                            ((bytes[1] & 0xFFL) << 8) |
                            ((bytes[2] & 0xFFL) << 16) |
                            ((bytes[3] & 0xFFL) << 24);
                }
            }
        };

        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Long object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(Long longValue, ByteOrder byteOrder) {
                if (longValue < 0 || longValue > MAX_UNSIGNED_INT_VALUE) {
                    throw new IllegalArgumentException("Value exceeds unsigned integer range (0..4,294,967,295): " + longValue);
                }
                byte[] bytes = new byte[4];
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    bytes[0] = (byte) ((longValue >> 24) & 0xFF);
                    bytes[1] = (byte) ((longValue >> 16) & 0xFF);
                    bytes[2] = (byte) ((longValue >> 8) & 0xFF);
                    bytes[3] = (byte) (longValue & 0xFFL);
                } else {
                    bytes[0] = (byte) (longValue & 0xFFL);
                    bytes[1] = (byte) ((longValue >> 8) & 0xFF);
                    bytes[2] = (byte) ((longValue >> 16) & 0xFF);
                    bytes[3] = (byte) ((longValue >> 24) & 0xFF);
                }
                return bytes;
            }
        };
    }

    @Override
    public FixedToObjectConverter<Long> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Long> getToByteConverter() {
        return toByteConverter;
    }
}