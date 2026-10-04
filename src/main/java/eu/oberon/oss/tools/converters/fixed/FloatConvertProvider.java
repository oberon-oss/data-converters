package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of float values to and from byte arrays.
 * <p>
 * This class extends the {@link AbstractFixedConverterProvider} using a value type of {@code Float}.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class FloatConvertProvider extends AbstractFixedConverterProvider<Float> {
    private final FixedToObjectConverter<Float> toObjectConverter;
    private final FixedToByteConverter<Float> toByteConverter;

    /**
     * Constructs a new instance of the {@link FloatConvertProvider} class.
     *
     * @since 1.0.0
     */
    public FloatConvertProvider() {
        super(ValueTypeNames.FLOAT, Float.class, 4);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Float convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public Float convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                int intBits;
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    intBits = ((bytes[0] & 0xFF) << 24) |
                            ((bytes[1] & 0xFF) << 16) |
                            ((bytes[2] & 0xFF) << 8) |
                            (bytes[3] & 0xFF);
                } else {
                    intBits = ((bytes[3] & 0xFF) << 24) |
                            ((bytes[2] & 0xFF) << 16) |
                            ((bytes[1] & 0xFF) << 8) |
                            (bytes[0] & 0xFF);
                }
                return Float.intBitsToFloat(intBits);
            }
        };
        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Float object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Float object, ByteOrder byteOrder) {
                int intBits = Float.floatToIntBits(object);
                byte[] bytes = new byte[4];
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    bytes[0] = (byte) ((intBits >> 24) & 0xFF);
                    bytes[1] = (byte) ((intBits >> 16) & 0xFF);
                    bytes[2] = (byte) ((intBits >> 8) & 0xFF);
                    bytes[3] = (byte) (intBits & 0xFF);
                } else {
                    bytes[0] = (byte) (intBits & 0xFF);
                    bytes[1] = (byte) ((intBits >> 8) & 0xFF);
                    bytes[2] = (byte) ((intBits >> 16) & 0xFF);
                    bytes[3] = (byte) ((intBits >> 24) & 0xFF);
                }
                return bytes;
            }
        };
    }

    @Override
    public FixedToObjectConverter<Float> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Float> getToByteConverter() {
        return toByteConverter;
    }
}
