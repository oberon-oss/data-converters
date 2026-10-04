package eu.oberon.oss.tools.converters;

/**
 * The parent of all converter providers.
 *
 * @since 1.0.0
 */
public interface ConverterProvider {

    /**
     * Returns the assigned value type name, stating the type of value that is converted.
     *
     * @return The assigned value type name.
     *
     * @since 1.0.0
     */
    String getValueTypeName();


}
