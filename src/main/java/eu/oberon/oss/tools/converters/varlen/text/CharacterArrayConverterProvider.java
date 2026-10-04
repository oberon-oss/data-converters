package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;

/**
 * Provides conversion between {@link Character[]} and {@link String} for encoding and decoding.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class CharacterArrayConverterProvider extends AbstractTextConverterProvider<Character[]> {

    /**
     * Constructs a new instance of the {@link CharacterArrayConverterProvider} class.
     *
     * @since 1.0.0
     */
    public CharacterArrayConverterProvider() {
        super(ValueTypeNames.CHARACTER_ARRAY, Character[].class);
    }

    @Override
    protected Character[] fromString(String value) {
        Character[] out = new Character[value.length()];
        for (int i = 0; i < value.length(); i++) {
            out[i] = value.charAt(i);
        }
        return out;
    }

    @Override
    protected String toStringValue(Character[] value) {
        char[] chars = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            chars[i] = value[i]; // unbox; NPE if null element (documented behavior)
        }
        return new String(chars);
    }
}