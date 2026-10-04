package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.SignedIntegerConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.SIGNED_INTEGER;

/**
 * Retrieves a signed integer value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class SignedIntegerRetriever extends AbstractFixedLengthValueRetriever<Integer> {

    /**
     * Constructs an instance of {@code SignedIntegerRetriever}.
     *
     * @since 1.0.0
     */
    public SignedIntegerRetriever() {
        SignedIntegerConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(SIGNED_INTEGER.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), SIGNED_INTEGER);
    }
}
