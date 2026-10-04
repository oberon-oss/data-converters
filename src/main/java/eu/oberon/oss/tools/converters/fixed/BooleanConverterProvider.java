package eu.oberon.oss.tools.converters.fixed;

import eu.oberon.oss.tools.ValueTypeNames;

import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Converter provider for boolean values.
 * <p>
 * This class provides converters for reading and writing boolean values from/to byte arrays.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class BooleanConverterProvider extends AbstractFixedConverterProvider<Boolean> {
    private final FixedToObjectConverter<Boolean> toObjectConverter;
    private final FixedToByteConverter<Boolean> fixedToByteConverter;

    /**
     * Default constructor.
     *
     * @since 1.0.0
     */
    public BooleanConverterProvider() {
        super(ValueTypeNames.BOOLEAN, Boolean.class, 1);
        toObjectConverter = new FixedToObjectConverter<>() {
            @Override
            public Boolean convert(byte[] bytes) {
                checkByteArraySize(bytes);
                return bytes[0] != 0;
            }

            /**
             * {@inheritDoc}
             * This implementation ignores the byte order parameter as boolean conversion does not support it.
             * @since 1.0.0
             */
            @Override
            public Boolean convert(byte[] bytes, ByteOrder byteOrder) {
                return convert(bytes);
            }
        };

        fixedToByteConverter = new FixedToByteConverter<>() {
            @Override
            public byte[] convert(Boolean object) {
                Objects.requireNonNull(object, "Parameters: object");
                //noinspection PointlessBooleanExpression
                return Boolean.TRUE.equals(object) ? new byte[]{1} : new byte[]{0};
            }

            /**
             * {@inheritDoc}
             * This implementation ignores the byte order parameter as boolean conversion does not support it.
             *
             * @since 1.0.0
             */
            @Override
            public byte[] convert(Boolean object, ByteOrder byteOrder) {
                return convert(object);
            }
        };
    }

    @Override
    public FixedToObjectConverter<Boolean> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Boolean> getToByteConverter() {
        return fixedToByteConverter;
    }
}
