package com.sdklite.aapt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Abstraction of binary file parser
 * 
 * @author johnsonlee
 *
 */
public abstract class FileParser {

    /**
     * The file to parse
     */
    protected final File file;

    /**
     * The random access file
     */
    protected final RandomAccessFile raf;

    private final ByteOrder byteOrder;

    /**
     * Instantialize with the specified file
     * 
     * @param file
     *            The file to parse
     * @throws FileNotFoundException
     */
    public FileParser(final File file) throws FileNotFoundException {
        this(file, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Instantialize with the specified file and byte order
     * 
     * @param file
     *            The file to parse
     * @param byteOrder
     *            The byte order
     * @throws FileNotFoundException
     *             if file doesn't exists
     */
    public FileParser(final File file, final ByteOrder byteOrder) throws FileNotFoundException {
        this.file = file;
        this.raf = new RandomAccessFile(file, "rw");
        this.byteOrder = byteOrder;
    }

    /**
     * Determine if has more data available
     * 
     * @return true if current position is not at the end of file
     * @throws IOException
     */
    public boolean hasRemaining() throws IOException {
        return this.raf.getFilePointer() < this.raf.length();
    }

    /**
     * Returns the number of bytes remaining
     * 
     * @throws IOException
     */
    public long remaining() throws IOException {
        return this.raf.length() - this.raf.getFilePointer();
    }

    /**
     * Returns current position of file pointer
     * 
     * @throws IOException
     */
    public long tell() throws IOException {
        return this.raf.getFilePointer();
    }

    /**
     * Sets the file pointer to the specified position
     * 
     * @param pos
     *            The file pointer position
     * @throws IOException
     */
    public void seek(final long pos) throws IOException {
        this.raf.seek(pos);
    }

    /**
     * Skips the specified number of bytes
     * 
     * @param n
     *            The number bytes to skip
     * @throws IOException
     */
    public void skip(final int n) throws IOException {
        this.raf.skipBytes(n);
    }

    /**
     * Reads the next byte but not change the file pointer position
     * 
     * @return The next byte
     * @throws IOException
     */
    public byte peek() throws IOException {
        final long p = tell();
        try {
            return read();
        } finally {
            seek(p);
        }
    }

    /**
     * Reads the next char but not change the file pointer position
     * 
     * @return The next char
     * @throws IOException
     */
    public char peekChar() throws IOException {
        final long p = tell();
        try {
            return readChar();
        } finally {
            seek(p);
        }
    }

    /**
     * Reads the next short but not change the file pointer position
     * 
     * @return The next short
     * @throws IOException
     */
    public short peekShort() throws IOException {
        final long p = tell();
        try {
            return readShort();
        } finally {
            seek(p);
        }
    }

    /**
     * Reads the next int but not change the file pointer position
     * 
     * @return The next int
     * @throws IOException
     */
    public int peekInt() throws IOException {
        final long p = tell();
        try {
            return readInt();
        } finally {
            seek(p);
        }
    }

    /**
     * Reads the next long but not change the file pointer position
     * 
     * @return The next long
     * @throws IOException
     */
    public long peekLong() throws IOException {
        final long p = tell();
        try {
            return readLong();
        } finally {
            seek(p);
        }
    }

    /**
     * Reads the next byte and move file pointer position forward
     * 
     * @return The next byte
     * @throws IOException
     */
    public byte read() throws IOException {
        return (byte) this.raf.read();
    }

    /**
     * Reads bytes up to the capacity of specified buffer
     * 
     * @param buffer
     *            The byte buffer into which the data is read
     * @return The number of bytes read into buffer
     * @throws IOException
     */
    public int read(final ByteBuffer buffer) throws IOException {
        return this.raf.read(buffer.array());
    }

    /**
     * Reads bytes up to the length of specified byte array
     * 
     * @param buffer
     *            The byte array into which the data is read
     * @return The number of bytes read into buffer
     * @throws IOException
     */
    public int read(final byte[] buffer) throws IOException {
        return this.raf.read(buffer);
    }

    /**
     * Reads bytes up to {@code len} into {@code buf}
     * 
     * @param buf
     *            The byte array into which the data is read
     * @param off
     *            The start offset in array {@code buf} at which the data is
     *            written
     * @param len
     *            The maximum number of bytes read
     * @return The number of bytes read into {@code buf}
     * @throws IOException
     */
    public int read(final byte[] buf, final int off, final int len) throws IOException {
        return this.raf.read(buf, off, len);
    }

    /**
     * Reads the next char
     * 
     * @return The next char
     * @throws IOException
     */
    public char readChar() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(2).order(this.byteOrder);
        this.raf.read(buffer.array());
        buffer.rewind();
        return buffer.getChar();
    }

    /**
     * Reads the next short
     * 
     * @return The next short
     * @throws IOException
     */
    public short readShort() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(2).order(this.byteOrder);
        this.raf.read(buffer.array());
        buffer.rewind();
        return buffer.getShort();
    }

    /**
     * Reads the next int
     * 
     * @return The next int
     * @throws IOException
     */
    public int readInt() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(this.byteOrder);
        this.raf.read(buffer.array());
        buffer.rewind();
        return buffer.getInt();
    }

    /**
     * Reads the next long
     * 
     * @return The next long
     * @throws IOException
     */
    public long readLong() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(8).order(this.byteOrder);
        this.raf.read(buffer.array());
        buffer.rewind();
        return buffer.getLong();
    }

    /**
     * Writes the specified byte into the parsing file
     * 
     * @param b
     *            The byte to be written
     * @throws IOException
     */
    public void write(final byte b) throws IOException {
        this.raf.write(b);
    }

    /**
     * Writes {@code b.length} bytes from {@code b} into the parsing file
     * 
     * @param b
     *            The data to be written
     * @throws IOException
     */
    public void write(final byte[] b) throws IOException {
        this.raf.write(b);
    }

    /**
     * Writes {@code len} bytes from {@code b} starting at offset {@code off}
     * into the parsing file
     * 
     * @param b
     *            The data to be written
     * @param off
     *            The start offset in the data
     * @param len
     *            The number of bytes to write
     * @throws IOException
     */
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.raf.write(b, off, len);
    }

    /**
     * Writes the specified short value into the parsing file
     * 
     * @param b
     *            The short value to be written
     * @throws IOException
     */
    public void writeShort(final short b) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(2).order(this.byteOrder);
        this.raf.write(buffer.putShort(b).array());
    }

    /**
     * Writes the specified int value into the parsing file
     * 
     * @param b
     *            The int value to be written
     * @throws IOException
     */
    public void writeInt(final int b) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(4).order(this.byteOrder);
        this.raf.write(buffer.putInt(b).array());
    }

    /**
     * Writes the specified long value into the parsing file
     * 
     * @param b
     *            The long value to be written
     * @throws IOException
     */
    public void writeLong(final long b) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(8).order(this.byteOrder);
        this.raf.write(buffer.putLong(b).array());
    }

    @Override
    protected void finalize() throws Throwable {
        this.raf.close();
        super.finalize();
    }
}
