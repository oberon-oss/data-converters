package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.UnsignedShortConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.UNSIGNED_SHORT;

/**
 * Retrieves an unsigned short value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class UnsignedShortRetriever extends AbstractFixedLengthValueRetriever<Integer> {

    /**
     * Constructs an instance of {@code UnsignedShortRetriever}.
     *
     * @since 1.0.0
     */
    public UnsignedShortRetriever() {
        UnsignedShortConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(UNSIGNED_SHORT.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), UNSIGNED_SHORT);
    }
}
