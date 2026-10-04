package eu.oberon.oss.tools.retrievers.varlen.text;

import eu.oberon.oss.tools.converters.AbstractConverterProvider;
import eu.oberon.oss.tools.converters.varlen.text.CharacterArrayConverterProvider;

import java.util.Objects;

import static eu.oberon.oss.tools.ValueTypeNames.CHARACTER_ARRAY;

/**
 * Provides a retriever for string values.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class CharacterArrayValueRetriever extends AbstractTextValueRetriever<Character[]> {
    /**
     * Constructs an instance of {@code CharacterArrayValueRetriever}.
     *
     * @since 1.0.0
     */
    public CharacterArrayValueRetriever() {
        CharacterArrayConverterProvider provider = Objects.requireNonNull(AbstractConverterProvider.getConverterProvider(CHARACTER_ARRAY.name()));
        super(provider.getToObjectConverter(), CHARACTER_ARRAY);
    }
}
