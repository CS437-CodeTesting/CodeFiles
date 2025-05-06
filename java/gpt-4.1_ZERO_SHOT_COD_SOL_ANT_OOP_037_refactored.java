import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an immutable block of bytes read from a file at a specific position.
 * Encapsulates all logic for reading, validating, and accessing block data.
 */
public final class Block {
    private final byte[] bytes;
    private final long blockPosition;

    /**
     * Reads a block of bytes from the given file at its current position.
     *
     * @param file   the RandomAccessFile to read from (must not be null)
     * @param amount the number of bytes to read (must be positive)
     * @return a new Block containing the bytes read and their file position
     * @throws IOException              if an I/O error occurs or not enough bytes are available
     * @throws IllegalArgumentException if amount is not positive
     * @throws NullPointerException     if file is null
     */
    public static Block readFromFile(RandomAccessFile file, int amount) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got: " + amount);
        }
        long position = file.getFilePointer();
        byte[] buffer = new byte[amount];
        file.readFully(buffer);
        return new Block(buffer, position);
    }

    /**
     * Constructs a Block with the given bytes and position.
     * The byte array is defensively copied to ensure immutability.
     *
     * @param bytes         the byte array (must not be null)
     * @param blockPosition the starting position of the block in the file (must be >= 0)
     * @throws NullPointerException     if bytes is null
     * @throws IllegalArgumentException if blockPosition is negative
     */
    public Block(byte[] bytes, long blockPosition) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (blockPosition < 0) {
            throw new IllegalArgumentException("blockPosition must be non-negative, got: " + blockPosition);
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.blockPosition = blockPosition;
    }

    /**
     * Checks if the given file position is contained within this block.
     *
     * @param position the file position to check
     * @return true if the position is within this block, false otherwise
     */
    public boolean contains(long position) {
        return position >= blockPosition && position < blockPosition + bytes.length;
    }

    /**
     * Returns the byte at the given file position within this block.
     *
     * @param position the file position (must be within this block)
     * @return the byte at the specified position
     * @throws IndexOutOfBoundsException if the position is not within this block
     */
    public byte getByte(long position) {
        if (!contains(position)) {
            throw new IndexOutOfBoundsException(
                "Position " + position + " is not within block range [" +
                blockPosition + ", " + (blockPosition + bytes.length) + ")"
            );
        }
        return bytes[(int) (position - blockPosition)];
    }

    /**
     * Returns the starting position of this block in the file.
     */
    public long getBlockPosition() {
        return blockPosition;
    }

    /**
     * Returns the size of this block in bytes.
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Returns a copy of the block's bytes.
     */
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public String toString() {
        return "Block[position=" + blockPosition + ", size=" + bytes.length + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Block)) return false;
        Block other = (Block) obj;
        return blockPosition == other.blockPosition && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(blockPosition);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }
}