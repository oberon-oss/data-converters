# Build status

[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=coverage)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=bugs)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=oberon-oss_data-converters&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=oberon-oss_data-converters)
---
[![Latest Release](https://img.shields.io/github/v/release/oberon-oss/data-converters?logo=github&style=flat-square)](https://github.com/oberon-oss/data-converters/releases/latest)
---

# Data Converters and Binary Data Reader

A unified Java library combining bidirectional type conversions (`converters`) and binary buffer reading / value retrieval (`binary-data-reader`) on top of a
common `BiDirectionalConverter` interface foundation.

---

### Key Concepts & Architecture

1. **Core Abstraction: `BiDirectionalConverter<S, T>`**
    - The foundational contract for 2-way type conversions between any types `<S>` and `<T>`.
    - `BiDirectionalConvertersRegistry` provides automatic discovery via Java SPI (`ServiceLoader`) and dynamic reverse converter synthesis.

2. **String Conversion: `Converter<S>`**
    - `Converter<S> extends BiDirectionalConverter<S, String>`
    - Provides bidirectional mapping between Java objects and `String` representations.
    - `ConvertersRegistry` acts as a specialized lookup facade and factory for composite types (e.g. `LastUsedItemList`).

3. **Binary Conversion: `BinaryConverter<T>`**
    - `BinaryConverter<T> extends BiDirectionalConverter<T, byte[]>`
    - Specialized interface for converting between Java types and `byte[]`.
    - Supports native and explicit `ByteOrder` configurations via `.withByteOrder(ByteOrder)`.
    - `AbstractBinaryConverter<T>`, `FixedConverterProvider<T>`, and `VarLenConverterProvider<T>` implement `BinaryConverter<T>`, providing full compatibility
      with `BiDirectionalConverter`.
    - `BinaryConvertersRegistry` allows type lookup by Java class type or by named value types (`ValueTypeNames`).

4. **Stream & Buffer Inspection: `BinaryDataReader` & `BinaryDataViewer`**
    - Direct integration with `BiDirectionalConverter` and `BinaryConverter` via helper methods:
        - `reader.read(BiDirectionalConverter<T, byte[]>, length)`
        - `viewer.peek(BiDirectionalConverter<T, byte[]>, offset, length)`
    - Cursor-based sequential reading (`BinaryDataReader`) and non-mutating buffer inspection (`BinaryDataViewer`).
    - Fully compatible with `ValueRetriever` implementations for high-level buffer reading.

---

### Example Usage

```java
import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataReaderImpl;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewerImpl;
import eu.oberon.oss.tools.converters.BiDirectionalConvertersRegistry;
import eu.oberon.oss.tools.converters.BinaryConvertersRegistry;
import eu.oberon.oss.tools.converters.ConvertersRegistry;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;
import eu.oberon.oss.tools.converters.string.Converter;

import java.nio.ByteOrder;

class example {
    void exampleMethod() {
        // 1. Unified registries
        BiDirectionalConvertersRegistry biRegistry = new BiDirectionalConvertersRegistry();
        BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry(biRegistry);
        ConvertersRegistry stringRegistry = new ConvertersRegistry();

        // 2. Convert object to binary
        BinaryConverter<Integer> intConverter = binaryRegistry.getConverterForClassType(Integer.class);

        assert intConverter != null;
        byte[] binaryData = intConverter.toBytes(123456789, ByteOrder.BIG_ENDIAN);

        // 3. Inspect / Read binary data
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(binaryData));
        Integer value = reader.read(intConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 4);

        // 4. Convert to String
        Converter<Integer> stringConverter = stringRegistry.getConverterForClassType(Integer.class);
        assert stringConverter != null;
        String str = stringConverter.convertToString().apply(value); // "123456789"
    }
}
```

---

### Creating Custom Converters

Custom converters allow you to define bidirectional transformations between Java objects and representations such as `byte[]` (binary
serialization/modification) or `String`.

#### 1. Binary Converter

##### Option A: Extending `AbstractBinaryConverter<T>` (Functional Approach)

Extend `AbstractBinaryConverter<T>` for concise bidirectional binary conversion with `ByteOrder` support:

```java
package com.example.converters;

import eu.oberon.oss.tools.converters.binary.AbstractBinaryConverter;

import java.nio.ByteBuffer;

// Custom data model
public record Point2D(short x, short y) {
}

// Binary Converter (4 bytes total: 2 bytes for x, 2 bytes for y)
public class Point2DBinaryConverter extends AbstractBinaryConverter<Point2D> {

    public Point2DBinaryConverter() {
        super(
                Point2D.class,
                (point, byteOrder) -> ByteBuffer.allocate(4)
                        .order(byteOrder)
                        .putShort(point.x())
                        .putShort(point.y())
                        .array(),
                (bytes, byteOrder) -> {
                    if (bytes.length != 4) {
                        throw new IllegalArgumentException("Expected 4 bytes, got " + bytes.length);
                    }
                    ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);
                    return new Point2D(buffer.getShort(), buffer.getShort());
                }
        );
    }
}
```

##### Option B: Implementing `FixedConverterProvider<T>` (Provider Interface Approach)

Implement `FixedConverterProvider<T>` for fixed-size structured conversions and SPI provider discovery:

```java
package com.example.converters;

import eu.oberon.oss.tools.converters.fixed.FixedConverterProvider;
import eu.oberon.oss.tools.converters.fixed.FixedToByteConverter;
import eu.oberon.oss.tools.converters.fixed.FixedToObjectConverter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Fixed-size Converter Provider (4 bytes total: 2 bytes for x, 2 bytes for y)
public class Point2DConverterProvider implements FixedConverterProvider<Point2D> {

    private final FixedToObjectConverter<Point2D> toObjectConverter = new FixedToObjectConverter<>() {
        @Override
        public Point2D convert(byte[] bytes) {
            return convert(bytes, ByteOrder.nativeOrder());
        }

        @Override
        public Point2D convert(byte[] bytes, ByteOrder byteOrder) {
            if (bytes.length != 4) {
                throw new IllegalArgumentException("Expected 4 bytes, got " + bytes.length);
            }
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);
            return new Point2D(buffer.getShort(), buffer.getShort());
        }
    };

    private final FixedToByteConverter<Point2D> toByteConverter = new FixedToByteConverter<>() {
        @Override
        public byte[] convert(Point2D point) {
            return convert(point, ByteOrder.nativeOrder());
        }

        @Override
        public byte[] convert(Point2D point, ByteOrder byteOrder) {
            return ByteBuffer.allocate(4)
                    .order(byteOrder)
                    .putShort(point.x())
                    .putShort(point.y())
                    .array();
        }
    };

    @Override
    public String getValueTypeName() {
        return "POINT_2D";
    }

    @Override
    public Class<Point2D> getTypeClass() {
        return Point2D.class;
    }

    @Override
    public int getExpectedByteArraySize() {
        return 4;
    }

    @Override
    public FixedToObjectConverter<Point2D> getToObjectConverter() {
        return toObjectConverter;
    }

    @Override
    public FixedToByteConverter<Point2D> getToByteConverter() {
        return toByteConverter;
    }
}
```

#### 2. String Converter

Extend `AbstractStringConverter<T>` for bidirectional String formatting and parsing:

```java
package com.example.converters;

import eu.oberon.oss.tools.converters.string.AbstractStringConverter;

public class Point2DStringConverter extends AbstractStringConverter<Point2D> {

    public Point2DStringConverter() {
        super(
                Point2D.class,
                point -> point.x() + "," + point.y(),            // to String
                str -> {                                          // from String
                    String[] parts = str.split(",");
                    return new Point2D(Short.parseShort(parts[0].trim()), Short.parseShort(parts[1].trim()));
                }
        );
    }
}
```

---

### Creating Custom Value Retrievers

Value retrievers read structured data directly from a `BinaryDataReader` (with cursor tracking) or `BinaryDataViewer` (without cursor mutation).

Implement `FixedLengthValueRetriever<T>` (for fixed-size data) or `VarLenValueRetriever<T>` (for variable-length data):

```java
package com.example.retrievers;

import com.example.converters.Point2D;
import com.example.converters.Point2DConverterProvider;
import eu.oberon.oss.tools.binaryreader.BinaryDataReader;
import eu.oberon.oss.tools.binaryreader.BinaryDataViewer;
import eu.oberon.oss.tools.retrievers.fixed.FixedLengthValueRetriever;

import java.nio.ByteOrder;
import java.util.Objects;

public final class Point2DRetriever implements FixedLengthValueRetriever<Point2D> {

    private final Point2DConverterProvider provider;

    public Point2DRetriever() {
        this(new Point2DConverterProvider());
    }

    public Point2DRetriever(Point2DConverterProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Parameter: provider");
    }

    @Override
    public String valueTypeName() {
        return provider.getValueTypeName();
    }

    @Override
    public Point2D getValue(BinaryDataViewer viewer, int offset) {
        return getValue(viewer, offset, ByteOrder.nativeOrder());
    }

    @Override
    public Point2D getValue(BinaryDataViewer viewer, int offset, ByteOrder byteOrder) {
        byte[] bytes = viewer.peekBytes(offset, provider.getExpectedByteArraySize());
        return provider.getToObjectConverter().convert(bytes, byteOrder);
    }

    @Override
    public Point2D getValue(BinaryDataReader reader) {
        return getValue(reader, ByteOrder.nativeOrder());
    }

    @Override
    public Point2D getValue(BinaryDataReader reader, ByteOrder byteOrder) {
        byte[] bytes = reader.readBytes(provider.getExpectedByteArraySize());
        return provider.getToObjectConverter().convert(bytes, byteOrder);
    }
}
```

---

### Registering and Loading Services (Java SPI)

The library leverages standard Java Service Provider Interface (`ServiceLoader`) for zero-configuration, pluggable discovery.

#### 1. Define Service Provider Files

Add service configuration files under your project's `src/main/resources/META-INF/services/` directory:

* **Converter Providers:** `src/main/resources/META-INF/services/eu.oberon.oss.tools.converters.ConverterProvider`
  ```text
  com.example.converters.Point2DConverterProvider
  ```

* **String Converters:** `src/main/resources/META-INF/services/eu.oberon.oss.tools.converters.string.Converter`
  ```text
  com.example.converters.Point2DStringConverter
  ```

* **Binary Converters:** `src/main/resources/META-INF/services/eu.oberon.oss.tools.converters.binary.BinaryConverter`
  ```text
  com.example.converters.Point2DBinaryConverter
  ```

* **Value Retrievers:** `src/main/resources/META-INF/services/eu.oberon.oss.tools.retrievers.ValueRetriever`
  ```text
  com.example.retrievers.Point2DRetriever
  ```

#### 2. Automatic Loading via Registries

When initializing registries, discovered service providers on the classpath are automatically registered:

```java
import com.example.converters.Point2D;
import com.example.retrievers.Point2DRetriever;
import eu.oberon.oss.tools.converters.BiDirectionalConvertersRegistry;
import eu.oberon.oss.tools.converters.BinaryConvertersRegistry;
import eu.oberon.oss.tools.converters.ConvertersRegistry;
import eu.oberon.oss.tools.converters.binary.BinaryConverter;
import eu.oberon.oss.tools.converters.string.Converter;
import eu.oberon.oss.tools.retrievers.AbstractValueRetriever;

class example {
    // Automatic discovery via ServiceLoader
    BiDirectionalConvertersRegistry biRegistry = new BiDirectionalConvertersRegistry();
    BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry(biRegistry);
    ConvertersRegistry stringRegistry = new ConvertersRegistry();

    // Look up custom binary converter discovered from META-INF/services
    BinaryConverter<Point2D> pointBinaryConverter = binaryRegistry.getConverterForClassType(Point2D.class);

    // Look up custom string converter discovered from META-INF/services
    Converter<Point2D> pointStringConverter = stringRegistry.getConverterForClassType(Point2D.class);

    // Look up value retriever discovered from META-INF/services
    Point2DRetriever pointRetriever = AbstractValueRetriever.getRetriever("POINT_2D");
}
```

#### 3. Programmatic Registration (Optional)

You can also register converters and retrievers manually at runtime without SPI:

```java
import com.example.converters.Point2DBinaryConverter;
import com.example.converters.Point2DConverterProvider;
import com.example.converters.Point2DStringConverter;
import com.example.retrievers.Point2DRetriever;
import eu.oberon.oss.tools.converters.BiDirectionalConvertersRegistry;
import eu.oberon.oss.tools.converters.BinaryConvertersRegistry;
import eu.oberon.oss.tools.converters.ConvertersRegistry;
import eu.oberon.oss.tools.retrievers.AbstractValueRetriever;

class example {
    // 1. Initialize registries
    BiDirectionalConvertersRegistry biRegistry = new BiDirectionalConvertersRegistry();
    BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry(biRegistry);
    ConvertersRegistry stringRegistry = new ConvertersRegistry();

    void exampleMethod() {
        // 2. Register binary converter / provider
        binaryRegistry.registerConverter(new Point2DBinaryConverter());
        // or: binaryRegistry.registerConverter(new Point2DConverterProvider());

        // 3. Register string converter
        stringRegistry.registerConverter(new Point2DStringConverter());

        // 4. Register retriever
        AbstractValueRetriever.registerRetriever(new Point2DRetriever());
    }
}
```
