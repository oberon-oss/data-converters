package eu.oberon.oss.tools.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for implementing {@link ConverterProvider} instances. Provides common functionality and infrastructure for managing and locating
 * converter providers.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractConverterProvider implements ConverterProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverterProvider.class);

    private final ValueTypeNames valueType;

    /**
     * Constructs a new {@link AbstractConverterProvider} instance with the specified {@link ValueTypeNames}.
     *
     * @param valueType The {@link ValueTypeNames} associated with this converter provider.
     *
     * @since 1.0.0
     */
    protected AbstractConverterProvider(ValueTypeNames valueType) {
        this.valueType = valueType;
    }

    @Override
    public String getValueTypeName() {
        return valueType.name();
    }

    private static final ConcurrentHashMap<String, ConverterProvider> CONVERTERS_HASHMAP = new ConcurrentHashMap<>();


    static {
        LOGGER.debug("Initializing converter providers via ServiceLoader");
        ServiceLoader<ConverterProvider> loader = ServiceLoader.load(
                ConverterProvider.class,
                AbstractConverterProvider.class.getClassLoader());

        for (ConverterProvider provider : loader) {
            registerConverterProvider(provider);
        }
        LOGGER.info("Registered {} converter provider(s)", CONVERTERS_HASHMAP.size());
    }

    /**
     * Attempts to locate and return a converter provider for the given value type name.
     *
     * @param valueTypeName The name of the converter provider to locate
     * @param <T>           The actual type of the converter provider. Callers need to ensure that the returned provider is compatible with their expectations.
     *
     * @return The converter provider, or null if no provider is found for the given value type name.
     *
     * @since 1.0.0
     */
    public static <T extends ConverterProvider> @Nullable T getConverterProvider(String valueTypeName) {
        Objects.requireNonNull(valueTypeName, "Parameter: valueTypeName");
        //noinspection unchecked
        return (T) CONVERTERS_HASHMAP.get(valueTypeName);
    }

    /**
     * Registers a converter provider for the given value type name.
     *
     * @param provider The converter provider to register. If a provider is already registered for the given value type name, it will be replaced; A warning
     *                 message will be logged.
     *
     * @return true if an existing provider was replaced, false otherwise
     *
     * @throws NullPointerException if the provider is null
     * @since 1.0.0
     */
    public static boolean registerConverterProvider(ConverterProvider provider) {
        Objects.requireNonNull(provider, "Parameter: provider");
        String name = provider.getValueTypeName();
        boolean replacedExisting = false;

        if (CONVERTERS_HASHMAP.containsKey(name)) {
            LOGGER.warn("Converter provider for value type {} already registered. Replacing {} of {}", name, CONVERTERS_HASHMAP.get(name).getClass().getName(), provider.getClass().getName());
            replacedExisting = true;
        }
        CONVERTERS_HASHMAP.put(name, provider);
        LOGGER.debug("Registered converter provider: {} -> {}", name, provider.getClass().getName());
        return replacedExisting;
    }
}
