package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.SignedShortConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.SIGNED_SHORT;

/**
 * Retrieves a signed short value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class SignedShortRetriever extends AbstractFixedLengthValueRetriever<Short> {

    /**
     * Constructs an instance of {@code SignedShortRetriever}.
     *
     * @since 1.0.0
     */
    public SignedShortRetriever() {
        SignedShortConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(SIGNED_SHORT.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), SIGNED_SHORT);
    }
}
