package eu.oberon.oss.tools.retrievers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AbstractValueRetrieverTest {

    @Test
    void getRetrieverReturnsNullWhenNoRetrieverIsRegisteredForValueTypeName() {
        String valueTypeName = uniqueValueTypeName();

        ValueRetriever retriever = AbstractValueRetriever.getRetriever(valueTypeName);

        assertNull(retriever);
    }

    @Test
    void getRetrieverThrowsNullPointerExceptionWhenValueTypeNameIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AbstractValueRetriever.getRetriever(null)
        );

        assertEquals("Parameter: valueTypeName", exception.getMessage());
    }

    @Test
    void registerRetrieverRegistersRetrieverAndReturnsFalseWhenNoExistingRetrieverWasReplaced() {
        String valueTypeName = uniqueValueTypeName();
        ValueRetriever retriever = new TestValueRetriever(valueTypeName);

        boolean replacedExisting = AbstractValueRetriever.registerRetriever(retriever);

        assertFalse(replacedExisting);
        assertSame(retriever, AbstractValueRetriever.getRetriever(valueTypeName));
    }

    @Test
    void registerRetrieverReplacesExistingRetrieverAndReturnsTrue() {
        String valueTypeName = uniqueValueTypeName();
        ValueRetriever originalRetriever = new TestValueRetriever(valueTypeName);
        ValueRetriever replacementRetriever = new TestValueRetriever(valueTypeName);

        boolean firstRegistrationReplacedExisting = AbstractValueRetriever.registerRetriever(originalRetriever);
        boolean secondRegistrationReplacedExisting = AbstractValueRetriever.registerRetriever(replacementRetriever);

        assertFalse(firstRegistrationReplacedExisting);
        assertTrue(secondRegistrationReplacedExisting);
        assertSame(replacementRetriever, AbstractValueRetriever.getRetriever(valueTypeName));
    }

    @Test
    void registerRetrieverThrowsNullPointerExceptionWhenRetrieverIsNull() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> AbstractValueRetriever.registerRetriever(null)
        );

        assertEquals("Parameter: retriever", exception.getMessage());
    }

    private static String uniqueValueTypeName() {
        return "TEST_VALUE_TYPE_" + UUID.randomUUID();
    }

    private static final class TestValueRetriever implements ValueRetriever {

        private final String valueTypeName;

        private TestValueRetriever(String valueTypeName) {
            this.valueTypeName = valueTypeName;
        }

        @Override
        public String getValueTypeName() {
            return valueTypeName;
        }
    }
}