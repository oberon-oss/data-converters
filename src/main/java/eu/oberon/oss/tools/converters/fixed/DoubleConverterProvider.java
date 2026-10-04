package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * Provides converters for double values.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class DoubleConverterProvider extends AbstractFixedConverterProvider<Double> {
    private final FixedToObjectConverter<Double> toObjectConverter;
    private final FixedToByteConverter<Double> fixedToByteConverter;

    /**
     * Constructs a new DoubleConvertProvider.
     *
     * @since 1.0.0
     */
    public DoubleConverterProvider() {
        super(ValueTypeNames.DOUBLE, Double.class, 8);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Double convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public Double convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                long longBits;
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    longBits = ((long) (bytes[0] & 0xFF) << 56) |
                            ((long) (bytes[1] & 0xFF) << 48) |
                            ((long) (bytes[2] & 0xFF) << 40) |
                            ((long) (bytes[3] & 0xFF) << 32) |
                            ((long) (bytes[4] & 0xFF) << 24) |
                            ((long) (bytes[5] & 0xFF) << 16) |
                            ((long) (bytes[6] & 0xFF) << 8) |
                            (bytes[7] & 0xFF);
                } else {
                    longBits = ((long) (bytes[7] & 0xFF) << 56) |
                            ((long) (bytes[6] & 0xFF) << 48) |
                            ((long) (bytes[5] & 0xFF) << 40) |
                            ((long) (bytes[4] & 0xFF) << 32) |
                            ((long) (bytes[3] & 0xFF) << 24) |
                            ((long) (bytes[2] & 0xFF) << 16) |
                            ((long) (bytes[1] & 0xFF) << 8) |
                            (bytes[0] & 0xFF);
                }
                return Double.longBitsToDouble(longBits);
            }
        };
        fixedToByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Double object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @SuppressWarnings("DuplicatedCode")
            @Override
            public byte[] convert(Double object, ByteOrder byteOrder) {
                byte[] bytes = new byte[8];
                long longBits = Double.doubleToLongBits(object);
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    bytes[0] = (byte) (longBits >>> 56);
                    bytes[1] = (byte) (longBits >>> 48);
                    bytes[2] = (byte) (longBits >>> 40);
                    bytes[3] = (byte) (longBits >>> 32);
                    bytes[4] = (byte) (longBits >>> 24);
                    bytes[5] = (byte) (longBits >>> 16);
                    bytes[6] = (byte) (longBits >>> 8);
                    bytes[7] = (byte) longBits;
                } else {
                    bytes[7] = (byte) (longBits >>> 56);
                    bytes[6] = (byte) (longBits >>> 48);
                    bytes[5] = (byte) (longBits >>> 40);
                    bytes[4] = (byte) (longBits >>> 32);
                    bytes[3] = (byte) (longBits >>> 24);
                    bytes[2] = (byte) (longBits >>> 16);
                    bytes[1] = (byte) (longBits >>> 8);
                    bytes[0] = (byte) longBits;
                }
                return bytes;
            }
        };
    }

    @Override
    public FixedToObjectConverter<Double> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Double> getToByteConverter() {
        return fixedToByteConverter;
    }
}
