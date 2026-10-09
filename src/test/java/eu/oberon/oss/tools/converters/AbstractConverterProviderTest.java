package eu.oberon.oss.tools.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.fixed.BooleanConverterProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AbstractConverterProviderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverterProviderTest.class);

    private static final String SERVICE_NAME = "src/main/resources/META-INF/services/eu.oberon.oss.tools.converters.ConverterProvider";

    @Test
    void testIfServiceLoadsCorrectly() {
        File path = new File(SERVICE_NAME);
        assertNotNull(path);
        assertTrue(path.exists());
        Set<ValueTypeNames> loadedTypes = new HashSet<>(List.of(ValueTypeNames.values()));
        assertDoesNotThrow(() -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug("Loading provider, line= {}", line);
                //noinspection unchecked
                Class<ConverterProvider> provider = (Class<ConverterProvider>) classLoader.loadClass(line);
                ConverterProvider loadedProvider = provider.getDeclaredConstructor().newInstance();
                assertNotNull(loadedProvider);
                if (!loadedTypes.remove(ValueTypeNames.valueOf(loadedProvider.getValueTypeName()))) {
                    fail("Loaded provider " + loadedProvider.getValueTypeName() + " is not expected");
                }
            }
            if (!loadedTypes.isEmpty()) {
                fail("Loaded all expected providers; entries left are: " + loadedTypes);
            }
            // Test if an existing entry gets replaced correctly
            assertTrue(AbstractConverterProvider.registerConverterProvider(new TestClass()));

            // Restore the original provider - or other tests may fail
            assertTrue(AbstractConverterProvider.registerConverterProvider(new BooleanConverterProvider()));
        }, "Failed to read service file " + SERVICE_NAME);
    }

    private static class TestClass extends AbstractConverterProvider {
        TestClass() {
            super(ValueTypeNames.BOOLEAN);
        }
    }
}