package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.BooleanConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.BOOLEAN;

/**
 * Retrieves an unsigned byte value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class BooleanRetriever extends AbstractFixedLengthValueRetriever<Boolean> {

    /**
     * Constructs an instance of {@code BooleanRetriever}.
     *
     * @since 1.0.0
     */
    public BooleanRetriever() {
        BooleanConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(BOOLEAN.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), BOOLEAN);
    }
}
