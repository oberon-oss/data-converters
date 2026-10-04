package eu.oberon.oss.tools.converters.binary;

import eu.oberon.oss.tools.converters.AbstractConverter;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * An abstract base class that implements a {@link BinaryConverter}, enabling bidirectional conversions between a Java type {@code <T>} and {@code byte[]}.
 *
 * @param <T> the type of object to convert to and from {@code byte[]}
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractBinaryConverter<T> extends AbstractConverter<T, byte[]> implements BinaryConverter<T> {

    private final Class<T> typeClass;
    private final BiFunction<T, ByteOrder, byte[]> toBytesWithOrder;
    private final BiFunction<byte[], ByteOrder, T> fromBytesWithOrder;

    /**
     * Constructs an instance with the specified type class and conversion functions.
     *
     * @param typeClass          the class type
     * @param toBytesWithOrder   the function converting {@code T} to {@code byte[]} with a given {@link ByteOrder}
     * @param fromBytesWithOrder the function converting {@code byte[]} to {@code T} with a given {@link ByteOrder}
     */
    protected AbstractBinaryConverter(
            Class<T> typeClass,
            BiFunction<T, ByteOrder, byte[]> toBytesWithOrder,
            BiFunction<byte[], ByteOrder, T> fromBytesWithOrder) {
        super(typeClass, byte[].class,
                obj -> toBytesWithOrder.apply(obj, ByteOrder.nativeOrder()),
                bytes -> fromBytesWithOrder.apply(bytes, ByteOrder.nativeOrder()));
        this.typeClass = Objects.requireNonNull(typeClass, "Parameter: typeClass");
        this.toBytesWithOrder = Objects.requireNonNull(toBytesWithOrder, "Parameter: toBytesWithOrder");
        this.fromBytesWithOrder = Objects.requireNonNull(fromBytesWithOrder, "Parameter: fromBytesWithOrder");
    }

    @Override
    public Class<T> getTypeClass() {
        return typeClass;
    }

    @Override
    public byte[] toBytes(T object) {
        return toBytesWithOrder.apply(object, ByteOrder.nativeOrder());
    }

    @Override
    public T fromBytes(byte[] bytes) {
        return fromBytesWithOrder.apply(bytes, ByteOrder.nativeOrder());
    }

    @Override
    public byte[] toBytes(T object, ByteOrder byteOrder) {
        return toBytesWithOrder.apply(object, byteOrder);
    }

    @Override
    public T fromBytes(byte[] bytes, ByteOrder byteOrder) {
        return fromBytesWithOrder.apply(bytes, byteOrder);
    }
}
