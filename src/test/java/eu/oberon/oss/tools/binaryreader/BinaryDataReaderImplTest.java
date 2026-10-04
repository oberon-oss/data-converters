package eu.oberon.oss.tools.binaryreader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BinaryDataReaderImplTest {

    @Test
    void remainingReturnsTotalSizeBeforeReading() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        int remaining = reader.remaining();

        assertEquals(4, remaining);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 2",
            "4, 0"
    })
    void remainingReturnsUnreadByteCountAfterReadingBytes(int bytesToRead, int expectedRemaining) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        reader.readBytes(bytesToRead);

        assertEquals(expectedRemaining, reader.remaining());
    }

    @ParameterizedTest
    @CsvSource({
            "3, true",
            "4, false"
    })
    void hasRemainingReturnsExpectedResult(int bytesRead, boolean expected) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        reader.readBytes(bytesRead);

        assertEquals(expected, reader.hasRemaining());
    }

    @Test
    void hasRemainingReturnsFalseForEmptyReader() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[0]).getReader();

        assertFalse(reader.hasRemaining());
        assertEquals(0, reader.remaining());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 3, true",
            "2, 2, true",
            "2, 3, false",
            "4, 0, true"
    })
    void hasRemainingLengthReturnsExpectedResult(int bytesRead, int length, boolean expected) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        reader.readBytes(bytesRead);

        assertEquals(expected, reader.hasRemaining(length));
    }

    @Test
    void hasRemainingLengthThrowsIllegalArgumentExceptionForNegativeLength() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reader.hasRemaining(-1)
        );

        assertEquals("length must not be negative", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("matchesPatterns")
    void matchesReturnsExpectedResultForPatternAtCurrentOffset(byte[] pattern, boolean expected) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertEquals(expected, reader.matches(pattern));
    }

    private static Stream<Arguments> matchesPatterns() {
        return Stream.of(
                Arguments.of(new byte[]{1, 2}, true),
                Arguments.of(new byte[]{1, 3}, false)
        );
    }

    @Test
    void matchesUsesCurrentReaderOffset() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        reader.readByte();

        assertTrue(reader.matches(new byte[]{2, 3}));
    }

    @Test
    void matchesDoesNotAdvanceReaderOffset() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertTrue(reader.matches(new byte[]{1, 2}));
        assertEquals(4, reader.remaining());
        assertEquals(1, reader.readByte());
    }

    @Test
    void matchesReturnsFalseWhenExpectedPatternIsLongerThanRemainingBytes() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        reader.readBytes(3);

        assertFalse(reader.matches(new byte[]{4, 5}));
        assertEquals(1, reader.remaining());
    }

    @Test
    void matchesReturnsTrueForEmptyExpectedPatternAtCurrentOffset() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertTrue(reader.matches(new byte[0]));
        assertEquals(4, reader.remaining());
    }

    @Test
    void matchesThrowsNullPointerExceptionWhenExpectedPatternIsNull() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reader.matches(null)
        );

        assertEquals("Parameter: expected", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("matchesOffsetPatterns")
    void matchesOffsetReturnsExpectedResult(int offset, byte[] pattern, boolean expected) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertEquals(expected, reader.matches(offset, pattern));
    }

    private static Stream<Arguments> matchesOffsetPatterns() {
        return Stream.of(
                Arguments.of(1, new byte[]{2, 3}, true),
                Arguments.of(1, new byte[]{2, 4}, false)
        );
    }

    @Test
    void matchesOffsetDoesNotAdvanceReaderOffset() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertTrue(reader.matches(2, new byte[]{3, 4}));
        assertEquals(4, reader.remaining());
        assertEquals(1, reader.readByte());
    }

    @Test
    void matchesOffsetReturnsFalseWhenExpectedPatternExtendsPastEndOfData() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertFalse(reader.matches(3, new byte[]{4, 5}));
        assertEquals(4, reader.remaining());
    }

    @Test
    void matchesOffsetReturnsTrueForEmptyExpectedPatternAtEndOffset() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertTrue(reader.matches(4, new byte[0]));
        assertEquals(4, reader.remaining());
    }

    @Test
    void matchesOffsetThrowsNullPointerExceptionWhenExpectedPatternIsNull() {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> reader.matches(0, null)
        );

        assertEquals("Parameter: expected", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidMatchesOffsetParameters")
    void matchesOffsetThrowsIndexOutOfBoundsExceptionWhenOffsetIsInvalid(int offset, byte[] pattern) {
        BinaryDataReader reader = new BinaryDataViewerImpl(new byte[]{1, 2, 3, 4}).getReader();

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> reader.matches(offset, pattern)
        );
    }

    private static Stream<Arguments> invalidMatchesOffsetParameters() {
        return Stream.of(
                Arguments.of(-1, new byte[]{1}),
                Arguments.of(5, new byte[0])
        );
    }

    @Test
    void testSkipForward() {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));

        reader.skip(3);

        assertEquals(3, reader.offset());
        assertEquals(2, reader.remaining());
    }

    @Test
    void testSkipBackward() {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));

        reader.skip(4);
        reader.skip(-2);

        assertEquals(2, reader.offset());
        assertEquals(3, reader.remaining());
    }

    @Test
    void testSkipZeroDoesNotChangeOffset() {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));

        reader.skip(2);
        reader.skip(0);

        assertEquals(2, reader.offset());
        assertEquals(3, reader.remaining());
    }

    @Test
    void testSkipToEnd() {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));

        reader.skip(data.length);

        assertEquals(data.length, reader.offset());
        assertEquals(0, reader.remaining());
    }

    @ParameterizedTest
    @ValueSource(ints = {4, -3})
    void testSkipBeyondBoundsThrowsExceptionAndKeepsOffset(int skipAmount) {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryDataReader reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(data));

        reader.skip(2);

        assertThrows(IndexOutOfBoundsException.class, () -> reader.skip(skipAmount));
        assertEquals(2, reader.offset());
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("rejects null viewer")
        void rejectsNullViewer() {
            assertThrows(NullPointerException.class, () -> new BinaryDataReaderImpl(null));
        }

        @Test
        @DisplayName("starts at offset zero")
        void startsAtOffsetZero() {
            BinaryDataReaderImpl reader = new BinaryDataReaderImpl(new BinaryDataViewerImpl(new byte[]{0x01}));

            assertEquals(0, reader.offset());
        }
    }

    @Nested
    @DisplayName("offset")
    class Offset {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03})
        );

        @ParameterizedTest
        @ValueSource(ints = {2, 3})
        @DisplayName("sets offset to valid position")
        void setsOffsetToValidPosition(int validOffset) {
            reader.offset(validOffset);

            assertEquals(validOffset, reader.offset());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 4})
        @DisplayName("rejects invalid offset")
        void rejectsInvalidOffset(int invalidOffset) {
            assertThrows(IndexOutOfBoundsException.class, () -> reader.offset(invalidOffset));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("does not change current offset when rejected")
        void doesNotChangeCurrentOffsetWhenRejected() {
            reader.offset(1);

            assertThrows(IndexOutOfBoundsException.class, () -> reader.offset(4));

            assertEquals(1, reader.offset());
        }
    }

    @Nested
    @DisplayName("readByte()")
    class ReadByteAtCurrentOffset {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03})
        );

        @Test
        @DisplayName("reads byte at current offset")
        void readsByteAtCurrentOffset() {
            assertEquals(0x01, reader.readByte());
        }

        @Test
        @DisplayName("advances offset by one")
        void advancesOffsetByOne() {
            reader.readByte();

            assertEquals(1, reader.offset());
        }

        @Test
        @DisplayName("reads sequential bytes")
        void readsSequentialBytes() {
            assertAll(
                    () -> assertEquals(0x01, reader.readByte()),
                    () -> assertEquals(0x02, reader.readByte()),
                    () -> assertEquals(0x03, reader.readByte()),
                    () -> assertEquals(3, reader.offset())
            );
        }

        @Test
        @DisplayName("rejects read at end offset")
        void rejectsReadAtEndOffset() {
            reader.offset(3);

            assertThrows(IndexOutOfBoundsException.class, reader::readByte);
            assertEquals(3, reader.offset());
        }
    }

    @Nested
    @DisplayName("readByte(offset)")
    class ReadByteAtAbsoluteOffset {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03})
        );

        @Test
        @DisplayName("reads byte at absolute offset")
        void readsByteAtAbsoluteOffset() {
            assertEquals(0x02, reader.readByte(1));
        }

        @Test
        @DisplayName("sets offset to next byte after absolute read")
        void setsOffsetToNextByteAfterAbsoluteRead() {
            reader.readByte(1);

            assertEquals(2, reader.offset());
        }

        @Test
        @DisplayName("absolute read does not depend on current offset")
        void absoluteReadDoesNotDependOnCurrentOffset() {
            reader.offset(2);

            byte value = reader.readByte(0);

            assertAll(
                    () -> assertEquals(0x01, value),
                    () -> assertEquals(1, reader.offset())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 3})
        @DisplayName("rejects invalid offset")
        void rejectsInvalidOffset(int invalidOffset) {
            assertThrows(IndexOutOfBoundsException.class, () -> reader.readByte(invalidOffset));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("does not change current offset when rejected")
        void doesNotChangeCurrentOffsetWhenRejected() {
            reader.offset(1);

            assertThrows(IndexOutOfBoundsException.class, () -> reader.readByte(3));

            assertEquals(1, reader.offset());
        }
    }

    @Nested
    @DisplayName("readBytes(length)")
    class ReadBytesAtCurrentOffset {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03, 0x04})
        );

        @Test
        @DisplayName("reads requested bytes at current offset")
        void readsRequestedBytesAtCurrentOffset() {
            reader.offset(1);

            byte[] bytes = reader.readBytes(2);

            assertArrayEquals(new byte[]{0x02, 0x03}, bytes);
        }

        @Test
        @DisplayName("advances offset by requested length")
        void advancesOffsetByRequestedLength() {
            reader.readBytes(3);

            assertEquals(3, reader.offset());
        }

        @Test
        @DisplayName("supports zero length")
        void supportsZeroLength() {
            reader.offset(2);

            byte[] bytes = reader.readBytes(0);

            assertAll(
                    () -> assertArrayEquals(new byte[]{}, bytes),
                    () -> assertEquals(2, reader.offset())
            );
        }

        @Test
        @DisplayName("returns a defensive copy")
        void returnsDefensiveCopy() {
            byte[] bytes = reader.readBytes(2);
            bytes[0] = 0x7F;

            reader.offset(0);

            assertArrayEquals(new byte[]{0x01, 0x02}, reader.readBytes(2));
        }

        @Test
        @DisplayName("rejects negative length")
        void rejectsNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> reader.readBytes(-1));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("rejects range that exceeds size")
        void rejectsRangeThatExceedsSize() {
            reader.offset(3);

            assertThrows(IndexOutOfBoundsException.class, () -> reader.readBytes(2));
            assertEquals(3, reader.offset());
        }
    }

    @Nested
    @DisplayName("readBytes(offset, length)")
    class ReadBytesAtAbsoluteOffset {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03, 0x04})
        );

        @Test
        @DisplayName("reads requested bytes at absolute offset")
        void readsRequestedBytesAtAbsoluteOffset() {
            byte[] bytes = reader.readBytes(1, 2);

            assertArrayEquals(new byte[]{0x02, 0x03}, bytes);
        }

        @Test
        @DisplayName("sets offset to end of absolute read range")
        void setsOffsetToEndOfAbsoluteReadRange() {
            reader.readBytes(1, 2);

            assertEquals(3, reader.offset());
        }

        @Test
        @DisplayName("absolute read does not depend on current offset")
        void absoluteReadDoesNotDependOnCurrentOffset() {
            reader.offset(3);

            byte[] bytes = reader.readBytes(0, 2);

            assertAll(
                    () -> assertArrayEquals(new byte[]{0x01, 0x02}, bytes),
                    () -> assertEquals(2, reader.offset())
            );
        }

        @Test
        @DisplayName("supports zero length at end")
        void supportsZeroLengthAtEnd() {
            byte[] bytes = reader.readBytes(4, 0);

            assertAll(
                    () -> assertArrayEquals(new byte[]{}, bytes),
                    () -> assertEquals(4, reader.offset())
            );
        }

        @Test
        @DisplayName("rejects negative length")
        void rejectsNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> reader.readBytes(0, -1));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            assertThrows(IndexOutOfBoundsException.class, () -> reader.readBytes(-1, 1));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("rejects range that exceeds size")
        void rejectsRangeThatExceedsSize() {
            reader.offset(1);

            assertThrows(IndexOutOfBoundsException.class, () -> reader.readBytes(3, 2));

            assertEquals(1, reader.offset());
        }
    }

    @Nested
    @DisplayName("readBytes(target, offset)")
    class ReadBytesIntoTarget {

        private final BinaryDataReaderImpl reader = new BinaryDataReaderImpl(
                new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03, 0x04})
        );

        @Test
        @DisplayName("copies requested bytes into target")
        void copiesRequestedBytesIntoTarget() {
            byte[] target = new byte[2];

            reader.readBytes(target, 1);

            assertArrayEquals(new byte[]{0x02, 0x03}, target);
        }

        @Test
        @DisplayName("sets offset to end of copied range")
        void setsOffsetToEndOfCopiedRange() {
            byte[] target = new byte[2];

            reader.readBytes(target, 1);

            assertEquals(3, reader.offset());
        }

        @Test
        @DisplayName("supports empty target at end")
        void supportsEmptyTargetAtEnd() {
            byte[] target = new byte[]{};

            reader.readBytes(target, 4);

            assertAll(
                    () -> assertArrayEquals(new byte[]{}, target),
                    () -> assertEquals(4, reader.offset())
            );
        }

        @Test
        @DisplayName("rejects null target")
        void rejectsNullTarget() {
            assertThrows(NullPointerException.class, () -> reader.readBytes(null, 0));
            assertEquals(0, reader.offset());
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 3})
        @DisplayName("rejects invalid offset")
        void rejectsInvalidOffset(int invalidOffset) {
            byte[] target = new byte[2];

            assertThrows(IndexOutOfBoundsException.class, () -> reader.readBytes(target, invalidOffset));
            assertEquals(0, reader.offset());
        }

        @Test
        @DisplayName("leaves target and offset unchanged when range is invalid")
        void leavesTargetAndOffsetUnchangedWhenRangeIsInvalid() {
            byte[] target = {0x55, 0x66};
            reader.offset(1);

            assertThrows(IndexOutOfBoundsException.class, () -> reader.readBytes(target, 3));

            assertAll(
                    () -> assertArrayEquals(new byte[]{0x55, 0x66}, target),
                    () -> assertEquals(1, reader.offset())
            );
        }
    }

    @Nested
    @DisplayName("getViewer()")
    class GetViewer {

        @Test
        @DisplayName("returns the underlying viewer")
        void returnsUnderlyingViewer() {
            BinaryDataViewer viewer = new BinaryDataViewerImpl(new byte[]{1, 2, 3});
            BinaryDataReader reader = new BinaryDataReaderImpl(viewer);

            assertSame(viewer, reader.getViewer());
        }
    }
}