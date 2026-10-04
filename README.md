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
    - `FixedConverterProvider<T>` and `VarLenConverterProvider<T>` implement `BinaryConverter<T>`, providing full compatibility with `BiDirectionalConverter`.
    - `BinaryConvertersRegistry` allows type lookup by Java class type or by named value types (`ValueTypeNames`).

4. **Stream & Buffer Inspection: `BinaryDataReader` & `BinaryDataViewer`**
    - Direct integration with `BiDirectionalConverter` and `BinaryConverter` via helper methods:
        - `reader.read(BiDirectionalConverter<T, byte[]>, length)`
        - `viewer.peek(BiDirectionalConverter<T, byte[]>, offset, length)`
    - Fully compatible with `ValueRetriever` implementations for high-level cursor-based buffer reading.

---

### Example Usage

```java
// 1. Unified registry
BiDirectionalConvertersRegistry biRegistry = new BiDirectionalConvertersRegistry();
BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry(biRegistry);
ConvertersRegistry stringRegistry = new ConvertersRegistry();

// 2. Convert object to binary
BinaryConverter<Integer> intConverter = binaryRegistry.getConverterForClassType(Integer.class);
byte[] binaryData = intConverter.toBytes(123456789, ByteOrder.BIG_ENDIAN);

// 3. Inspect / Read binary data
BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(binaryData));
Integer value = reader.read(intConverter.withByteOrder(ByteOrder.BIG_ENDIAN), 4);

// 4. Convert to String
Converter<Integer> stringConverter = stringRegistry.getConverterForClassType(Integer.class);
String str = stringConverter.convertToString().apply(value); // "123456789"
```

---

### Creating Custom Converters / Modifiers

Custom converters allow you to define bidirectional transformations between Java objects and representations such as `byte[]` (binary serialization/modification) or `String`.

#### 1. Binary Converter / Provider

Extend `AbstractFixedConverterProvider<T>` (for fixed-size data) or `AbstractBinaryConverter<T>`:

```java
package com.example.converters;

import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.converters.fixed.AbstractFixedConverterProvider;
import eu.oberon.oss.tools.converters.fixed.FixedToByteConverter;
import eu.oberon.oss.tools.converters.fixed.FixedToObjectConverter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// Custom data model
public record Point2D(short x, short y) {}

// Converter Provider (4 bytes total: 2 bytes for x, 2 bytes for y)
public class Point2DConverterProvider extends AbstractFixedConverterProvider<Point2D> {

    private final FixedToObjectConverter<Point2D> toObjectConverter = new FixedToObjectConverter<>() {
        @Override
        public Point2D convert(byte[] bytes, ByteOrder byteOrder) {
            checkByteArraySize(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);
            return new Point2D(buffer.getShort(), buffer.getShort());
        }
    };

    private final FixedToByteConverter<Point2D> toByteConverter = new FixedToByteConverter<>() {
        @Override
        public byte[] convert(Point2D point, ByteOrder byteOrder) {
            return ByteBuffer.allocate(4)
                    .order(byteOrder)
                    .putShort(point.x())
                    .putShort(point.y())
                    .array();
        }
    };

    public Point2DConverterProvider() {
        super(ValueTypeNames.CUSTOM, Point2D.class, 4);
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

Extend `AbstractFixedLengthValueRetriever<T>` or `AbstractVarLenValueRetriever<T>`:

```java
package com.example.retrievers;

import com.example.converters.Point2D;
import com.example.converters.Point2DConverterProvider;
import eu.oberon.oss.tools.ValueTypeNames;
import eu.oberon.oss.tools.retrievers.fixed.AbstractFixedLengthValueRetriever;

public final class Point2DRetriever extends AbstractFixedLengthValueRetriever<Point2D> {

    public Point2DRetriever() {
        this(new Point2DConverterProvider());
    }

    public Point2DRetriever(Point2DConverterProvider provider) {
        super(provider.getToObjectConverter(), provider.getExpectedByteArraySize(), ValueTypeNames.CUSTOM);
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
  com.example.converters.Point2DConverterProvider
  ```

* **Value Retrievers:** `src/main/resources/META-INF/services/eu.oberon.oss.tools.retrievers.ValueRetriever`
  ```text
  com.example.retrievers.Point2DRetriever
  ```

#### 2. Automatic Loading via Registries

When initializing registries, discovered service providers on the classpath are automatically registered:

```java
// Automatic discovery via ServiceLoader
BiDirectionalConvertersRegistry biRegistry = new BiDirectionalConvertersRegistry();
BinaryConvertersRegistry binaryRegistry = new BinaryConvertersRegistry(biRegistry);
ConvertersRegistry stringRegistry = new ConvertersRegistry();

// Look up custom binary converter discovered from META-INF/services
BinaryConverter<Point2D> pointBinaryConverter = binaryRegistry.getConverterForClassType(Point2D.class);

// Look up custom string converter discovered from META-INF/services
Converter<Point2D> pointStringConverter = stringRegistry.getConverterForClassType(Point2D.class);

// Look up value retriever discovered from META-INF/services
Point2DRetriever pointRetriever = AbstractValueRetriever.getRetriever("CUSTOM");
```

#### 3. Programmatic Registration (Optional)

You can also register converters and retrievers manually at runtime without SPI:

```java
// Register binary converter
binaryRegistry.registerConverter(new Point2DConverterProvider());

// Register string converter
stringRegistry.registerConverter(new Point2DStringConverter());

// Register retriever
AbstractValueRetriever.registerRetriever(new Point2DRetriever());
```
