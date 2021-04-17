package com.fileManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * 
 * FileManager manage read/write from/to file save data of btree. It support
 * some method for basic read/write and tracking file's state
 * 
 */
public class FileManager {
    private RandomAccessFile file;

    public FileManager() throws IOException {
        this.file = new RandomAccessFile("default.dat", "rw");
    }

    public FileManager(String urlFile, String mode) throws IOException {
        this.file = new RandomAccessFile(urlFile, mode);
    }

    // Track file
    public void seek(long pos) throws IOException {
        file.seek(pos);
    }

    public void seekToEnd() throws IOException {
        file.seek(file.length());
    }

    public long getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    public long getLength() throws IOException {
        return file.length();
    }

    public void setLength(long newLength) throws IOException {
        file.setLength(newLength);
    }

    // Read
    public boolean readBoolean() throws IOException {
        return file.readBoolean();
    }

    public byte[] readBytes(int maxLength) throws IOException {
        byte[] b = new byte[maxLength];
        file.read(b);

        return b;
    }

    public double readDouble() throws IOException {
        return file.readDouble();
    }

    public int readInt() throws IOException {
        return file.readInt();
    }

    public long readLong() throws IOException {
        return file.readLong();
    }

    public short readShort() throws IOException {
        return file.readShort();
    }

    public String readString(int maxLength) throws IOException {
        byte[] buffer = new byte[maxLength];
        file.read(buffer);

        return new String(buffer).trim();
    }

    // Write
    public void writeBoolean(boolean b) throws IOException {
        file.writeBoolean(b);
    }

    public void writeBytes(byte[] b) throws IOException {
        file.write(b);
    }

    public void writeDouble(double d) throws IOException {
        file.writeDouble(d);
    }

    public void writeInt(int a) throws IOException {
        file.writeInt(a);
    }

    public void writeLong(long l) throws IOException {
        file.writeLong(l);
    }

    public void writeShort(short v) throws IOException {
        file.writeShort(v);
    }

    public void writeString(String s, int maxLength) throws IOException {
        byte[] limitStr = Arrays.copyOf(s.getBytes(), maxLength);
        file.write(limitStr);
    }

    public void writeArrLong(LinkedList<Long> arr) throws IOException {
        for (int i = 0; i < arr.size(); i++) {
            file.writeLong(arr.get(i));
        }
    }

    public void close() throws IOException {
        this.file.close();
    }
    // public void writeArrString(LinkedList<String> arrStr, int maxLength, long
    // pos) throws IOException {
    // // Seek to writting position
    // this.file.seek(pos);

    // // Start write
    // for (int i = 0; i < arrStr.size(); i++) {
    // byte[] str = arrStr.get(i).getBytes();
    // byte[] strLimit = Arrays.copyOf(str, maxLength);

    // this.file.write(strLimit);
    // }
    // }
}
