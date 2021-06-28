package com.viettel.vtag.model.transfer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Data
@Accessors(fluent = true)
public class BinaryPayload {

    private static final byte[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public final ByteBuffer buffer;

    public BinaryPayload(byte[] payload) {
        this.buffer = ByteBuffer.wrap(payload);
    }

    public static void main(String[] args) {
        // var content = new byte[] {(byte) 0x4C, (byte) 0x65, (byte) 0x20, (byte) 0x4D, (byte) 0x69, (byte) 0x6E,
        //     (byte) 0x68, (byte) 0x20, (byte) 0x44, (byte) 0x75, (byte) 0x63};
        var content = new byte[] {(byte) 0x11, (byte) 0x63, (byte) 0x13, (byte) 0x00, (byte) 0xCE, (byte) 0x11,
            (byte) 0x1D, (byte) 0x1A, (byte) 0xC1, (byte) 0x03, (byte) 0x30, (byte) 0x34, (byte) 0xC5, (byte) 0xAA,
            (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x0F, (byte) 0xAA, (byte) 0xBB,
            (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x0F, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC,
            (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x0F, (byte) 0x4C, (byte) 0x65, (byte) 0x20, (byte) 0x4D,
            (byte) 0x69, (byte) 0x6E, (byte) 0x68, (byte) 0x20, (byte) 0x44, (byte) 0x75, (byte) 0x63};
        var payload = new BinaryPayload(content);
        var s1 = payload.getString(34);
        var string = payload.getString(11);
        System.out.println(string);

        printBytes("Le Minh Duc".getBytes(StandardCharsets.UTF_8));
    }

    public String getString(int length) {
        var bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes);
    }

    public static void printBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            System.out.format("%02X ", bytes[i]);

            if (i % 10 == 9) {
                System.out.print("   ");
            }
            if ((i + 10) % 20 == 9) {
                System.out.println();
            }
        }
        System.out.println();
    }

    public static String toString(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public String getMAC() {
        var sb = new byte[18];
        var bytes = new byte[6];
        this.buffer.get(bytes);
        for (var i = 0; i < 6; i++) {
            var b = bytes[i] & 0xFF;
            sb[i * 3] = HEX[b >> 4];
            sb[i * 3 + 1] = HEX[b & 0x0F];
            sb[i * 3 + 2] = ':';
        }
        return new String(sb, 0, 17);
    }

    public byte get() {
        return buffer.get();
    }

    public char getChar() {
        return buffer.getChar();
    }

    public long getLong() {
        return buffer.getLong();
    }

    public float getFloat() {
        return buffer.getFloat();
    }

    public double getDouble() {
        return buffer.getDouble();
    }
}
