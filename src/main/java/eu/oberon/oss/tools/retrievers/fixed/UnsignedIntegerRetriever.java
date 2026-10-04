package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.UnsignedIntegerConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.UNSIGNED_INTEGER;

/**
 * Retrieves an unsigned integer value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class UnsignedIntegerRetriever extends AbstractFixedLengthValueRetriever<Long> {

    /**
     * Constructs an instance of {@code UnsignedIntegerRetriever}.
     *
     * @since 1.0.0
     */
    public UnsignedIntegerRetriever() {
        UnsignedIntegerConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(UNSIGNED_INTEGER.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), UNSIGNED_INTEGER);
    }
}
