package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.UnsignedLongConverterProvider;

import java.math.BigInteger;
import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.UNSIGNED_LONG;

/**
 * Retrieves an unsigned long value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class UnsignedLongRetriever extends AbstractFixedLengthValueRetriever<BigInteger> {

    /**
     * Constructs an instance of {@code UnsignedLongRetriever}.
     *
     * @since 1.0.0
     */
    public UnsignedLongRetriever() {
        UnsignedLongConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(UNSIGNED_LONG.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), UNSIGNED_LONG);
    }
}
