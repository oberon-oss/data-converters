package eu.oberon.oss.tools.converters.varlen.text;

import eu.oberon.oss.tools.ValueTypeNames;

/**
 * A converter provider for managing the conversion of string values to and from byte arrays.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public class StringConverterProvider extends AbstractTextConverterProvider<String> {

    /**
     * Constructs a new instance of the StringConverterProvider.
     *
     * @since 1.0.0
     */
    public StringConverterProvider() {
        super(ValueTypeNames.STRING, String.class);
    }

    @Override
    protected String fromString(String value) {
        return value;
    }

    @Override
    protected String toStringValue(String value) {
        return value;
    }
}