package eu.oberon.oss.tools.retrievers.varlen.text;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.varlen.text.CharConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.CHARACTER;

/**
 * Provides a retriever for string values.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class CharacterValueRetriever extends AbstractTextValueRetriever<Character> {
    /**
     * Constructs an instance of {@code CharacterValueRetriever}.
     *
     * @since 1.0.0
     */
    public CharacterValueRetriever() {
        CharConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(CHARACTER.name()));
        super(provider.getToObjectConverter(), CHARACTER);
    }
}
