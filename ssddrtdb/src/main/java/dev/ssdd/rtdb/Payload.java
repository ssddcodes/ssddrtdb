package dev.ssdd.rtdb;

public class Payload {
    private final int opcode;
    private final byte[] data;
    public Payload(int opcode, byte[] data) {
        this.opcode = opcode;
        this.data = data;
    }
    public int getOpcode() {
        return opcode;
    }
    public byte[] getData() {
        return data;
    }
}