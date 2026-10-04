package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;

/**
 * Provides conversions between byte arrays and signed integers. Enabling it to convert signed integer values to and from their byte array representations. It
 * specifically expects byte arrays of size 4, conforming to the size of a 32-bit signed integer.
 * <p>
 * Conversions are sensitive to byte order, supporting both {@code ByteOrder.BIG_ENDIAN} and {@code ByteOrder.LITTLE_ENDIAN}, with the default being the native
 * byte order.
 * <p>
 * The expected behavior for byte arrays is guaranteed through validation using the {@code checkByteArraySize(byte[])} method inherited from
 * {@code AbstractConverterProvider}.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class SignedIntegerConverterProvider extends AbstractFixedConverterProvider<Integer> {
    private final FixedToObjectConverter<Integer> toObjectConverter;
    private final FixedToByteConverter<Integer> toByteConverter;

    /**
     * Constructs a new instance of the {@link SignedIntegerConverterProvider} class.
     *
     * @since 1.0.0
     */
    public SignedIntegerConverterProvider() {
        super(ValueTypeNames.SIGNED_INTEGER, Integer.class, 4);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Integer convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public Integer convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    return ((bytes[0] & 0xFF) << 24) |
                            ((bytes[1] & 0xFF) << 16) |
                            ((bytes[2] & 0xFF) << 8) |
                            (bytes[3] & 0xFF);
                } else {
                    return (bytes[0] & 0xFF) |
                            ((bytes[1] & 0xFF) << 8) |
                            ((bytes[2] & 0xFF) << 16) |
                            ((bytes[3] & 0xFF) << 24);
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

                byte[] bytes = new byte[4];
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    bytes[0] = (byte) ((object >> 24) & 0xFF);
                    bytes[1] = (byte) ((object >> 16) & 0xFF);
                    bytes[2] = (byte) ((object >> 8) & 0xFF);
                    bytes[3] = (byte) (object & 0xFF);
                } else {
                    bytes[0] = (byte) (object & 0xFF);
                    bytes[1] = (byte) ((object >> 8) & 0xFF);
                    bytes[2] = (byte) ((object >> 16) & 0xFF);
                    bytes[3] = (byte) ((object >> 24) & 0xFF);
                }
                return bytes;
            }
        };
    }

    /**
     * Provides a convert that converts byte arrays to Integer objects.
     *
     * @return A converter that converts {@code byte[] ---> Integer }
     *
     * @since 1.0.0
     */
    @Override
    public FixedToObjectConverter<Integer> getToObjectConverter() {
        return toObjectConverter;
    }

    /**
     * Provides a convert that converts Integer objects to byte arrays.
     *
     * @return A converter that converts {@code Integer ---> byte[]}
     *
     * @since 1.0.0
     */
    @Override
    public FixedToByteConverter<Integer> getToByteConverter() {
        return toByteConverter;
    }
}