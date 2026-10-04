package eu.oberon.oss.tools;

/**
 * Enumerates the supported value types for binary data conversion, for which this library provides converters.
 *
 * @author TigerLilly64
 * @since 1.0.0
 */
public enum ValueTypeNames {
    /**
     * Represents an unsigned byte value in the range of 0x00 - 0xFF.
     *
     * @since 1.0.0
     */
    UNSIGNED_BYTE,
    /**
     * Represents A 16-bit signed short value in the range of 0x8000 - 0x7FFF
     *
     * @since 1.0.0
     */
    SIGNED_SHORT,
    /**
     * Represents A 16-bit unsigned short value in the range of 0x0000 - 0xFFFF
     *
     * @since 1.0.0
     */
    UNSIGNED_SHORT,
    /**
     * Represents A 32-bit signed integer value in the range of 0x8000_0000 - 0x7FFF_FFFF
     *
     * @since 1.0.0
     */
    SIGNED_INTEGER,
    /**
     * Represents A 32-bit unsigned integer value in the range of 0x0000_0000 - 0xFFFF_FFFF
     *
     * @since 1.0.0
     */
    UNSIGNED_INTEGER,
    /**
     * Represents A 64-bit signed long value in the range of 0x8000_0000_0000_0000 - 0x7FFF_FFFF_FFFF_FFFF
     *
     * @since 1.0.0
     */
    SIGNED_LONG,
    /**
     * Represents A 64-bit unsigned long value in the range of 0x0000_0000_0000_0000 - 0xFFFF_FFFF_FFFF_FFFF
     *
     * @since 1.0.0
     */
    UNSIGNED_LONG,
    /**
     * Represents a 32-bit floating-point value following the IEEE 754 standard.
     *
     * @since 1.0.0
     */
    FLOAT,
    /**
     * Represents a 64-bit floating-point value following the IEEE 754 standard.
     *
     * @since 1.0.0
     */
    DOUBLE,
    /**
     * Represents a boolean value, which can be either true (1) or false (0).
     *
     * @since 1.0.0
     */
    BOOLEAN,
    /**
     * Represents a standard 16-bit Unicode character.
     *
     * @since 1.0.0
     */
    CHARACTER,
    /**
     * Represents a standard 16-bit Unicode character array.
     *
     * @since 1.0.0
     */
    CHARACTER_ARRAY,
    /**
     * Represents a String object.
     *
     * @since 1.0.0
     */
    STRING,
}
