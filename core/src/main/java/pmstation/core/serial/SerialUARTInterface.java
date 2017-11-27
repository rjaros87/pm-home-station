package pmstation.core.serial;

public interface SerialUARTInterface {
    public byte[] readBytes(int dataLenght);

    public void writeBytes(byte[] writeBuffer);
}
