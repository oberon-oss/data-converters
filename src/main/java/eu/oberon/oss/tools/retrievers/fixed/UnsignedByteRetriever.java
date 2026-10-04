package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.UnsignedByteConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.UNSIGNED_BYTE;

/**
 * Retrieves an unsigned byte value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class UnsignedByteRetriever extends AbstractFixedLengthValueRetriever<Integer> {

    /**
     * Constructs an instance of {@code UnsignedByteRetriever}.
     *
     * @since 1.0.0
     */
    public UnsignedByteRetriever() {
        UnsignedByteConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(UNSIGNED_BYTE.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), UNSIGNED_BYTE);
    }
}
