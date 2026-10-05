package eu.oberon.oss.tools.retrievers;

import eu.oberon.oss.tools.ValueTypeNames;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for value retrievers.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public abstract class AbstractValueRetriever implements ValueRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValueRetriever.class);
    private final ValueTypeNames valueTypeNames;

    /**
     * Constructs an instance of {@code AbstractValueRetriever} with the specified value type names.
     *
     * @param valueTypeNames The value type names that define the types of values this retriever will support.
     *
     * @since 1.0.0
     */
    protected AbstractValueRetriever(ValueTypeNames valueTypeNames) {
        this.valueTypeNames = valueTypeNames;
    }

    @Override
    public String valueTypeName() {
        return valueTypeNames.name();
    }

    private static final ConcurrentHashMap<String, ValueRetriever> RETRIEVER_HASH_MAP = new ConcurrentHashMap<>();

    static {
        LOGGER.debug("Initializing retrievers via ServiceLoader");
        ServiceLoader<ValueRetriever> loader = ServiceLoader.load(
                ValueRetriever.class,
                AbstractValueRetriever.class.getClassLoader());

        for (ValueRetriever retriever : loader) {
            registerRetriever(retriever);
        }
        LOGGER.info("Registered {} retriever(s)", RETRIEVER_HASH_MAP.size());
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
    public static <T extends ValueRetriever> @Nullable T getRetriever(String valueTypeName) {
        Objects.requireNonNull(valueTypeName, "Parameter: valueTypeName");
        //noinspection unchecked
        return (T) RETRIEVER_HASH_MAP.get(valueTypeName);
    }

    /**
     * Registers a converter retriever for the given value type name.
     *
     * @param retriever The converter retriever to register. If a retriever is already registered for the given value type name, it will be replaced; A warning
     *                  message will be logged.
     *
     * @return true if an existing retriever was replaced, false otherwise
     *
     * @throws NullPointerException if the retriever is null
     * @since 1.0.0
     */
    public static boolean registerRetriever(ValueRetriever retriever) {
        Objects.requireNonNull(retriever, "Parameter: retriever");
        String name = retriever.valueTypeName();
        boolean replacedExisting = false;

        if (RETRIEVER_HASH_MAP.containsKey(name)) {
            LOGGER.warn("Retriever for value type {} already registered. Replacing {} of {}",
                    name, RETRIEVER_HASH_MAP.get(name).getClass().getName(), retriever.getClass().getName()
            );
            replacedExisting = true;
        }
        RETRIEVER_HASH_MAP.put(name, retriever);
        LOGGER.debug("Registered retriever: {} -> {}", name, retriever.getClass().getName());
        return replacedExisting;
    }
}
