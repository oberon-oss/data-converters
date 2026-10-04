package eu.oberon.oss.tools.binaryreader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BinaryDataViewerImplTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("rejects null data")
        void rejectsNullData() {
            assertThrows(NullPointerException.class, () -> new BinaryDataViewerImpl(null));
        }

        @Test
        @DisplayName("defensively copies constructor data")
        void defensivelyCopiesConstructorData() {
            byte[] source = {0x01, 0x02, 0x03};

            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(source);
            source[0] = 0x7F;

            assertEquals(0x01, viewer.peekByte(0));
        }

        @Test
        @DisplayName("accepts empty data")
        void acceptsEmptyData() {
            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{});

            assertEquals(0, viewer.size());
        }
    }

    @Nested
    @DisplayName("Size")
    class Size {

        @Test
        @DisplayName("returns the number of bytes in the viewer")
        void returnsDataSize() {
            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03});

            assertEquals(3, viewer.size());
        }
    }

    @Nested
    @DisplayName("ensureByteAvailable")
    class EnsureByteAvailable {

        private final BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03});

        @Test
        @DisplayName("accepts first valid offset")
        void acceptsFirstValidOffset() {
            assertDoesNotThrow(() -> viewer.ensureByteAvailable(0));
        }

        @Test
        @DisplayName("accepts last valid offset")
        void acceptsLastValidOffset() {
            assertDoesNotThrow(() -> viewer.ensureByteAvailable(2));
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureByteAvailable(-1));
        }

        @Test
        @DisplayName("rejects offset equal to size")
        void rejectsOffsetEqualToSize() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureByteAvailable(3));
        }

        @Test
        @DisplayName("rejects offset greater than size")
        void rejectsOffsetGreaterThanSize() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureByteAvailable(4));
        }
    }

    @Nested
    @DisplayName("ensureBytesAvailable")
    class EnsureBytesAvailable {

        private final BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03});

        @Test
        @DisplayName("accepts full available range")
        void acceptsFullAvailableRange() {
            assertDoesNotThrow(() -> viewer.ensureBytesAvailable(0, 3));
        }

        @Test
        @DisplayName("accepts sub range")
        void acceptsSubRange() {
            assertDoesNotThrow(() -> viewer.ensureBytesAvailable(1, 2));
        }

        @Test
        @DisplayName("accepts zero length at start")
        void acceptsZeroLengthAtStart() {
            assertDoesNotThrow(() -> viewer.ensureBytesAvailable(0, 0));
        }

        @Test
        @DisplayName("accepts zero length at end")
        void acceptsZeroLengthAtEnd() {
            assertDoesNotThrow(() -> viewer.ensureBytesAvailable(3, 0));
        }

        @Test
        @DisplayName("rejects negative length")
        void rejectsNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> viewer.ensureBytesAvailable(0, -1));
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureBytesAvailable(-1, 1));
        }

        @Test
        @DisplayName("rejects range that exceeds size")
        void rejectsRangeThatExceedsSize() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureBytesAvailable(1, 3));
        }

        @Test
        @DisplayName("rejects offset greater than size even for zero length")
        void rejectsOffsetGreaterThanSizeEvenForZeroLength() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.ensureBytesAvailable(4, 0));
        }
    }

    @Nested
    @DisplayName("peekByte")
    class PeekByte {

        private final BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03});

        @Test
        @DisplayName("returns byte at offset")
        void returnsByteAtOffset() {
            assertAll(
                    () -> assertEquals(0x01, viewer.peekByte(0)),
                    () -> assertEquals(0x02, viewer.peekByte(1)),
                    () -> assertEquals(0x03, viewer.peekByte(2))
            );
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekByte(-1));
        }

        @Test
        @DisplayName("rejects offset equal to size")
        void rejectsOffsetEqualToSize() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekByte(3));
        }
    }

    @Nested
    @DisplayName("peekBytes(offset, length)")
    class PeekBytesByLength {

        private final BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03, 0x04});

        @Test
        @DisplayName("returns requested bytes")
        void returnsRequestedBytes() {
            assertArrayEquals(new byte[]{0x02, 0x03}, viewer.peekBytes(1, 2));
        }

        @Test
        @DisplayName("returns full data")
        void returnsFullData() {
            assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, viewer.peekBytes(0, 4));
        }

        @Test
        @DisplayName("returns empty array for zero length")
        void returnsEmptyArrayForZeroLength() {
            assertArrayEquals(new byte[]{}, viewer.peekBytes(2, 0));
        }

        @Test
        @DisplayName("returns a defensive copy")
        void returnsDefensiveCopy() {
            byte[] bytes = viewer.peekBytes(0, 2);
            bytes[0] = 0x7F;

            assertArrayEquals(new byte[]{0x01, 0x02}, viewer.peekBytes(0, 2));
        }

        @Test
        @DisplayName("rejects negative length")
        void rejectsNegativeLength() {
            assertThrows(IllegalArgumentException.class, () -> viewer.peekBytes(0, -1));
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekBytes(-1, 1));
        }

        @Test
        @DisplayName("rejects range that exceeds size")
        void rejectsRangeThatExceedsSize() {
            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekBytes(3, 2));
        }
    }

    @Nested
    @DisplayName("peekBytes(target, offset)")
    class PeekBytesIntoTarget {

        private final BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03, 0x04});

        @Test
        @DisplayName("copies requested bytes into target")
        void copiesRequestedBytesIntoTarget() {
            byte[] target = new byte[2];

            viewer.peekBytes(target, 1);

            assertArrayEquals(new byte[]{0x02, 0x03}, target);
        }

        @Test
        @DisplayName("copies full data into target")
        void copiesFullDataIntoTarget() {
            byte[] target = new byte[4];

            viewer.peekBytes(target, 0);

            assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, target);
        }

        @Test
        @DisplayName("supports empty target at end")
        void supportsEmptyTargetAtEnd() {
            byte[] target = new byte[]{};

            assertDoesNotThrow(() -> viewer.peekBytes(target, 4));
            assertArrayEquals(new byte[]{}, target);
        }

        @Test
        @DisplayName("rejects null target")
        void rejectsNullTarget() {
            assertThrows(NullPointerException.class, () -> viewer.peekBytes(null, 0));
        }

        @Test
        @DisplayName("rejects negative offset")
        void rejectsNegativeOffset() {
            byte[] target = new byte[1];

            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekBytes(target, -1));
        }

        @Test
        @DisplayName("rejects range that exceeds size")
        void rejectsRangeThatExceedsSize() {
            byte[] target = new byte[2];

            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekBytes(target, 3));
        }

        @Test
        @DisplayName("leaves target unchanged when range is invalid")
        void leavesTargetUnchangedWhenRangeIsInvalid() {
            byte[] target = {0x55, 0x66};

            assertThrows(IndexOutOfBoundsException.class, () -> viewer.peekBytes(target, 3));
            assertArrayEquals(new byte[]{0x55, 0x66}, target);
        }
    }

    @Nested
    @DisplayName("getReader")
    class GetReader {

        @Test
        @DisplayName("returns a BinaryDataReaderImpl")
        void returnsBinaryDataReaderImpl() {
            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01});

            BinaryDataReader reader = viewer.getReader();

            assertInstanceOf(BinaryDataReaderImpl.class, reader);
        }

        @Test
        @DisplayName("returns a fresh reader on every call")
        void returnsFreshReaderOnEveryCall() {
            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02});

            BinaryDataReader first = viewer.getReader();
            BinaryDataReader second = viewer.getReader();

            assertAll(
                    () -> assertNotSame(first, second),
                    () -> assertEquals(0, first.offset()),
                    () -> assertEquals(0, second.offset())
            );
        }

        @Test
        @DisplayName("returned reader reads from this viewer")
        void returnedReaderReadsFromThisViewer() {
            BinaryDataViewerImpl viewer = new BinaryDataViewerImpl(new byte[]{0x01, 0x02, 0x03});

            BinaryDataReader reader = viewer.getReader();

            assertAll(
                    () -> assertEquals(0x01, reader.readByte()),
                    () -> assertEquals(1, reader.offset()),
                    () -> assertArrayEquals(new byte[]{0x02, 0x03}, reader.readBytes(2)),
                    () -> assertEquals(3, reader.offset())
            );
        }
    }
}