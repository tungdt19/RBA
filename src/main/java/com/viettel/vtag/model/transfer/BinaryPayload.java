package com.viettel.vtag.model.transfer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;

@Data
@Accessors(fluent = true)
public class BinaryPayload {

    private final byte[] payload;
    private final int length;
    private int pointer;

    public BinaryPayload(byte[] payload) {
        this.payload = payload;
        this.length = payload.length;
        this.pointer = 0;
    }

    public static void main(String[] args) {
        var content = new byte[] {0x11, 0x63, 0x13, (byte) 0xCE, 0x11, 0x1D, (byte) 0xC1, 0x00, (byte) 0xC2, 0x03,
            0x30, 0x34, (byte) 0xC5, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF,
            0x0F, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, 0x0F, (byte) 0xAA,
            (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, 0x0F};
        printBytes(content);
        // var payload = new BinaryPayload(content);
    }

    public static void printBytes(byte[] bytes) {
        System.out.println(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            System.out.format("%02X ", bytes[i]);

            if (i % 10 == 9) {
                System.out.print("   ");
            }
            if ((i + 10) % 20 == 9) {
                System.out.println();
            }
        }
    }

    byte[] getBytes(int from, int to) {
        var length = to - from + 1;
        var bytes = new byte[length];
        System.arraycopy(payload, from, bytes, 0, length);
        return bytes;
    }

    public String getMAC() {
        return getMAC(pointer);
    }

    public String getMAC(int from) {
        return "";
    }

    public int getInt(int bitSize) {
        var integer = getInt(pointer, bitSize);
        pointer += bitSize;
        return integer;
    }

    public int getInt(int from, int bitSize) {
        return 0;
    }
}
