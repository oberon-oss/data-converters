package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of signed long values to and from byte arrays.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class SignedLongConverterProvider extends AbstractFixedConverterProvider<Long> {
    private final FixedToObjectConverter<Long> toObjectConverter;
    private final FixedToByteConverter<Long> toByteConverter;

    /**
     * Constructs a new instance of the {@link SignedLongConverterProvider} class.
     *
     * @since 1.0.0
     */
    public SignedLongConverterProvider() {
        super(ValueTypeNames.SIGNED_LONG, Long.class, 8);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Long convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public Long convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                long value;
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    value = ((long) bytes[0] << 56) |
                            ((long) (bytes[1] & 0xFF) << 48) |
                            ((long) (bytes[2] & 0xFF) << 40) |
                            ((long) (bytes[3] & 0xFF) << 32) |
                            ((long) (bytes[4] & 0xFF) << 24) |
                            ((long) (bytes[5] & 0xFF) << 16) |
                            ((long) (bytes[6] & 0xFF) << 8) |
                            (bytes[7] & 0xFF);
                } else {
                    value = ((long) bytes[7] << 56) |
                            ((long) (bytes[6] & 0xFF) << 48) |
                            ((long) (bytes[5] & 0xFF) << 40) |
                            ((long) (bytes[4] & 0xFF) << 32) |
                            ((long) (bytes[3] & 0xFF) << 24) |
                            ((long) (bytes[2] & 0xFF) << 16) |
                            ((long) (bytes[1] & 0xFF) << 8) |
                            (bytes[0] & 0xFF);
                }
                return value;
            }
        };

        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Long object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Long object, ByteOrder byteOrder) {
                byte[] result = new byte[8];
                long value = object;
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    result[0] = (byte) (value >>> 56);
                    result[1] = (byte) (value >>> 48);
                    result[2] = (byte) (value >>> 40);
                    result[3] = (byte) (value >>> 32);
                    result[4] = (byte) (value >>> 24);
                    result[5] = (byte) (value >>> 16);
                    result[6] = (byte) (value >>> 8);
                    result[7] = (byte) value;
                } else {
                    result[0] = (byte) value;
                    result[1] = (byte) (value >>> 8);
                    result[2] = (byte) (value >>> 16);
                    result[3] = (byte) (value >>> 24);
                    result[4] = (byte) (value >>> 32);
                    result[5] = (byte) (value >>> 40);
                    result[6] = (byte) (value >>> 48);
                    result[7] = (byte) (value >>> 56);
                }

                return result;
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
