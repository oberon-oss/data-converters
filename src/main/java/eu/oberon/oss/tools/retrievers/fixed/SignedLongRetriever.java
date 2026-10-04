package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.SignedLongConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.SIGNED_LONG;

/**
 * Retrieves a signed long value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class SignedLongRetriever extends AbstractFixedLengthValueRetriever<Long> {

    /**
     * Constructs an instance of {@code SignedLongRetriever}.
     *
     * @since 1.0.0
     */
    public SignedLongRetriever() {
        SignedLongConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(SIGNED_LONG.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), SIGNED_LONG);
    }
}
