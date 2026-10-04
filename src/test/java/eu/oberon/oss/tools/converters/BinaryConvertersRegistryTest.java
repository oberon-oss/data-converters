package eu.oberon.oss.tools.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BinaryConvertersRegistry Tests")
class BinaryConvertersRegistryTest {

    private BinaryConvertersRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BinaryConvertersRegistry();
    }

    @Test
    @DisplayName("Constructor with custom BiDirectionalConvertersRegistry stores and returns underlying registry")
    void testConstructorWithCustomRegistry() {
        BiDirectionalConvertersRegistry customBiRegistry = new BiDirectionalConvertersRegistry();
        BinaryConvertersRegistry customRegistry = new BinaryConvertersRegistry(customBiRegistry);

        assertSame(customBiRegistry, customRegistry.getBiDirectionalConvertersRegistry());
    }

    @Test
    @DisplayName("Constructor rejects null BiDirectionalConvertersRegistry")
    void testConstructorRejectsNullRegistry() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new BinaryConvertersRegistry(null));
        assertEquals("Parameter: biDirectionalConvertersRegistry", ex.getMessage());
    }

    @Test
    @DisplayName("Lookup by class type returns matching BinaryConverter")
    void testGetConverterForClassType() {
        BinaryConverter<Integer> intConverter = registry.getConverterForClassType(Integer.class);
        assertNotNull(intConverter);
        assertEquals(Integer.class, intConverter.getTypeClass());

        byte[] bytes = intConverter.toBytes(42);
        assertNotNull(bytes);
        assertEquals(42, intConverter.fromBytes(bytes));
    }

    @Test
    @DisplayName("getConverterForClassType rejects null classType")
    void testGetConverterForClassTypeRejectsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> registry.getConverterForClassType(null));
        assertEquals("Parameter: classType", ex.getMessage());
    }

    @Test
    @DisplayName("getConverterForClassType returns null when no converter is registered")
    void testGetConverterForClassTypeReturnsNullForUnknownType() {
        assertNull(registry.getConverterForClassType(Void.class));
    }

    @Test
    @DisplayName("getConverterForClassType wraps standard BiDirectionalConverter into BinaryConverter when not already BinaryConverter")
    void testGetConverterForClassTypeWrapsNonBinaryConverter() {
        record CustomPayload(String text) {}

        BiDirectionalConverter<CustomPayload, byte[]> nonBinaryConverter = AbstractConverter.of(
                CustomPayload.class,
                byte[].class,
                payload -> payload.text().getBytes(StandardCharsets.UTF_8),
                bytes -> new CustomPayload(new String(bytes, StandardCharsets.UTF_8))
        );

        registry.getBiDirectionalConvertersRegistry().registerConverter(nonBinaryConverter);

        BinaryConverter<CustomPayload> wrappedConverter = registry.getConverterForClassType(CustomPayload.class);
        assertNotNull(wrappedConverter);
        assertEquals(CustomPayload.class, wrappedConverter.getTypeClass());
        assertEquals(CustomPayload.class, wrappedConverter.getSourceType());
        assertEquals(byte[].class, wrappedConverter.getTargetType());

        byte[] encoded = wrappedConverter.toBytes(new CustomPayload("hello-world"));
        assertArrayEquals("hello-world".getBytes(StandardCharsets.UTF_8), encoded);

        CustomPayload decoded = wrappedConverter.fromBytes(encoded);
        assertEquals(new CustomPayload("hello-world"), decoded);

        // Verify BiDirectional functional interfaces on the wrapper
        assertEquals(new CustomPayload("hello-world"), wrappedConverter.getToSourceFunction().apply(encoded));
        assertArrayEquals(encoded, wrappedConverter.getToTargetFunction().apply(new CustomPayload("hello-world")));

        // Verify withByteOrder delegation
        BiDirectionalConverter<CustomPayload, byte[]> orderedConverter = wrappedConverter.withByteOrder(ByteOrder.LITTLE_ENDIAN);
        assertNotNull(orderedConverter);
        assertArrayEquals("hello-world".getBytes(StandardCharsets.UTF_8), orderedConverter.getToTargetFunction().apply(new CustomPayload("hello-world")));
    }

    @Test
    @DisplayName("Lookup by ValueTypeNames returns matching BinaryConverter")
    void testGetConverterForValueType() {
        BinaryConverter<Integer> signedIntConverter = registry.getConverterForValueType(ValueTypeNames.SIGNED_INTEGER);
        assertNotNull(signedIntConverter);

        byte[] beBytes = signedIntConverter.toBytes(0x12345678, ByteOrder.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, beBytes);

        BinaryConverter<Long> unsignedIntConverter = registry.getConverterForValueType(ValueTypeNames.UNSIGNED_INTEGER);
        assertNotNull(unsignedIntConverter);
        long unsignedVal = 0x80000000L;
        byte[] uintBytes = unsignedIntConverter.toBytes(unsignedVal, ByteOrder.BIG_ENDIAN);
        assertEquals(unsignedVal, unsignedIntConverter.fromBytes(uintBytes, ByteOrder.BIG_ENDIAN));
    }

    @Test
    @DisplayName("getConverterForValueType rejects null string and enum parameters")
    void testGetConverterForValueTypeRejectsNull() {
        NullPointerException exString = assertThrows(NullPointerException.class, () -> registry.getConverterForValueType((String) null));
        assertEquals("Parameter: valueTypeName", exString.getMessage());

        NullPointerException exEnum = assertThrows(NullPointerException.class, () -> registry.getConverterForValueType((ValueTypeNames) null));
        assertEquals("Parameter: valueTypeName", exEnum.getMessage());
    }

    @Test
    @DisplayName("getConverterForValueType returns null when unknown value type name")
    void testGetConverterForValueTypeUnknownReturnsNull() {
        assertNull(registry.getConverterForValueType("UNKNOWN_NONEXISTENT_TYPE"));
    }

    @Test
    @DisplayName("Lookup with ByteOrder returns configured BiDirectionalConverter")
    void testGetConverterWithByteOrder() {
        BiDirectionalConverter<Integer, byte[]> leConverter = registry.getConverterForClassType(Integer.class, ByteOrder.LITTLE_ENDIAN);
        assertNotNull(leConverter);

        byte[] bytes = leConverter.getToTargetFunction().apply(0x01020304);
        assertArrayEquals(new byte[]{0x04, 0x03, 0x02, 0x01}, bytes);
        assertEquals(0x01020304, leConverter.getToSourceFunction().apply(bytes));
    }

    @Test
    @DisplayName("getConverterForClassType with ByteOrder returns null when converter not found")
    void testGetConverterWithByteOrderReturnsNullForUnknownClass() {
        assertNull(registry.getConverterForClassType(Void.class, ByteOrder.LITTLE_ENDIAN));
    }

    @Test
    @DisplayName("getConverterForClassType with null ByteOrder throws NullPointerException")
    void testGetConverterWithByteOrderRejectsNullByteOrder() {
        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                registry.getConverterForClassType(Integer.class, null));
        assertEquals("Parameter: byteOrder", ex.getMessage());
    }

    @Test
    @DisplayName("registerConverter with BinaryConverter registers into underlying bidirectional registry")
    void testRegisterBinaryConverter() {
        record DummyType(int code) {}

        BinaryConverter<DummyType> dummyConverter = new BinaryConverter<>() {
            @Override
            public Class<DummyType> getTypeClass() {
                return DummyType.class;
            }

            @Override
            public byte[] toBytes(DummyType object) {
                return new byte[]{(byte) object.code()};
            }

            @Override
            public DummyType fromBytes(byte[] bytes) {
                return new DummyType(bytes[0]);
            }
        };

        registry.registerConverter(dummyConverter);

        BinaryConverter<DummyType> retrieved = registry.getConverterForClassType(DummyType.class);
        assertSame(dummyConverter, retrieved);
        assertArrayEquals(new byte[]{7}, retrieved.toBytes(new DummyType(7)));
        assertEquals(new DummyType(7), retrieved.fromBytes(new byte[]{7}));
    }

    @Test
    @DisplayName("registerConverter rejects null converter")
    void testRegisterBinaryConverterRejectsNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> registry.registerConverter((BinaryConverter<?>) null));
        assertEquals("Parameter: converter", ex.getMessage());
    }

    @Test
    @DisplayName("registerConverter with named mapping registers and retrieves named converter")
    void testRegisterNamedBinaryConverter() {
        record CustomHeader(short tag) {}

        BinaryConverter<CustomHeader> headerConverter = new BinaryConverter<>() {
            @Override
            public Class<CustomHeader> getTypeClass() {
                return CustomHeader.class;
            }

            @Override
            public byte[] toBytes(CustomHeader object) {
                return new byte[]{(byte) (object.tag() >> 8), (byte) object.tag()};
            }

            @Override
            public CustomHeader fromBytes(byte[] bytes) {
                return new CustomHeader((short) (((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF)));
            }
        };

        registry.registerConverter("CUSTOM_HEADER_TYPE", headerConverter);

        BinaryConverter<CustomHeader> retrieved = registry.getConverterForValueType("CUSTOM_HEADER_TYPE");
        assertSame(headerConverter, retrieved);
        assertArrayEquals(new byte[]{0x12, 0x34}, retrieved.toBytes(new CustomHeader((short) 0x1234)));
        assertEquals(new CustomHeader((short) 0x1234), retrieved.fromBytes(new byte[]{0x12, 0x34}));
    }

    @Test
    @DisplayName("registerConverter with named mapping rejects null parameters")
    void testRegisterNamedBinaryConverterRejectsNulls() {
        record Dummy(int x) {}
        BinaryConverter<Dummy> dummyConverter = new BinaryConverter<>() {
            @Override
            public Class<Dummy> getTypeClass() {
                return Dummy.class;
            }
            @Override
            public byte[] toBytes(Dummy object) { return new byte[0]; }
            @Override
            public Dummy fromBytes(byte[] bytes) { return new Dummy(0); }
        };

        NullPointerException exName = assertThrows(NullPointerException.class, () ->
                registry.registerConverter((String) null, dummyConverter));
        assertEquals("Parameter: name", exName.getMessage());

        NullPointerException exConverter = assertThrows(NullPointerException.class, () ->
                registry.registerConverter("DUMMY", null));
        assertEquals("Parameter: converter", exConverter.getMessage());
    }
}
