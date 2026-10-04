package eu.oberon.oss.tools.retrievers.varlen.text;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.varlen.text.StringConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.STRING;

/**
 * Provides a retriever for string values.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class StringValueRetriever extends AbstractTextValueRetriever<String> {

    /**
     * Constructs an instance of {@code StringValueRetriever}.
     *
     * @since 1.0.0
     */
    public StringValueRetriever() {
        StringConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(STRING.name()));
        super(provider.getToObjectConverter(), STRING);
    }
}
