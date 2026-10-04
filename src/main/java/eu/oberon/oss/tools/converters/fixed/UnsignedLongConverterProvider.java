package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * A converter provider for managing the conversion of unsigned long values to and from byte arrays.
 * <p>
 * This class extends the {@link AbstractFixedConverterProvider} using a value type of {@code BigInteger}.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class UnsignedLongConverterProvider extends AbstractFixedConverterProvider<BigInteger> {
    /**
     * The maximum value of an unsigned long (2^64 - 1).
     *
     * @since 1.0.0
     */
    public static final BigInteger MAX_UNSIGNED_BigInteger_VALUE = new BigInteger("18446744073709551615");

    private final FixedToObjectConverter<BigInteger> toObjectConverter;
    private final FixedToByteConverter<BigInteger> toByteConverter;

    /**
     * Constructs a new instance of the {@link UnsignedLongConverterProvider}.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("java:S3776") // There is no point in reducing the complexity from 16 to 15.
    public UnsignedLongConverterProvider() {
        super(ValueTypeNames.UNSIGNED_LONG, BigInteger.class, 8);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public BigInteger convert(byte[] bytes) {
                return convert(bytes, ByteOrder.nativeOrder());
            }

            @Override
            public BigInteger convert(byte[] bytes, ByteOrder byteOrder) {
                checkByteArraySize(bytes);
                byte[] orderedBytes = new byte[8];
                if (byteOrder == ByteOrder.BIG_ENDIAN) {
                    System.arraycopy(bytes, 0, orderedBytes, 0, 8);
                } else {
                    for (int i = 0; i < 8; i++) {
                        orderedBytes[i] = bytes[7 - i];
                    }
                }

                return new BigInteger(1, orderedBytes);
            }
        };

        toByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(BigInteger object) {
                return convert(object, ByteOrder.nativeOrder());
            }

            @Override
            public byte[] convert(BigInteger object, ByteOrder byteOrder) {
                if (object.signum() < 0 || object.compareTo(MAX_UNSIGNED_BigInteger_VALUE) > 0) {
                    throw new IllegalArgumentException(
                            "BigInteger value out of unsigned long range (0..18446744073709551615): " + object);
                }

                byte[] bigIntegerBytes = object.toByteArray();
                byte[] result = new byte[8];

                // Handle sign byte if present and pad with zeros
                int sourcePos = bigIntegerBytes.length > 8 ? bigIntegerBytes.length - 8 : 0;
                int destPos = Math.max(0, 8 - bigIntegerBytes.length);
                int length = Math.min(bigIntegerBytes.length, 8);

                System.arraycopy(bigIntegerBytes, sourcePos, result, destPos, length);

                // Reverse if little endian
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    byte[] reversed = new byte[8];
                    for (int i = 0; i < 8; i++) {
                        reversed[i] = result[7 - i];
                    }
                    return reversed;
                }

                return result;
            }
        };
    }

    @Override
    public FixedToObjectConverter<BigInteger> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<BigInteger> getToByteConverter() {
        return toByteConverter;
    }
}
