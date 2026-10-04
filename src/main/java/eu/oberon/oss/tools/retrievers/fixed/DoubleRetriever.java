package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.DoubleConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.DOUBLE;


/**
 * Retrieves a {@link Double} value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class DoubleRetriever extends AbstractFixedLengthValueRetriever<Double> {

    /**
     * Constructs an instance of {@code DoubleRetriever}.
     *
     * @since 1.0.0
     */
    public DoubleRetriever() {
        DoubleConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(DOUBLE.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), DOUBLE);
    }
}
