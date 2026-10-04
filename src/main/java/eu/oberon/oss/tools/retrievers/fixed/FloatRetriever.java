package eu.oberon.oss.tools.retrievers.fixed;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.fixed.FloatConvertProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.FLOAT;

/**
 * Retrieves a {@link Float} value from a binary data reader or viewer.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public final class FloatRetriever extends AbstractFixedLengthValueRetriever<Float> {

    /**
     * Constructs an instance of {@code FloatRetriever}.
     *
     * @since 1.0.0
     */
    public FloatRetriever() {
        FloatConvertProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(FLOAT.name()));
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), FLOAT);
    }

}
