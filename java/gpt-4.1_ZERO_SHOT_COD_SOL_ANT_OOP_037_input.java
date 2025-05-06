    private static final class Block {
        private byte[] bytes = new byte[0];
        private long blockPosition;

        boolean contains(long position) {
            return position >= blockPosition && position < blockPosition + bytes.length;
        }

        public void read(RandomAccessFile file, int amount) throws IOException {
            blockPosition = file.getFilePointer();
            // reuse byte array, if possible
            if (amount != bytes.length) {
                bytes = new byte[amount];
            }
            file.readFully(bytes);
        }

        public byte get(long position) {
            return bytes[(int) (position - blockPosition)];
        }
    }