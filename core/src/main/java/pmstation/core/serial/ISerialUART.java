package pmstation.core.serial;

public interface ISerialUART {
    byte[] readBytes(int dataLenght);
    void writeBytes(byte[] writeBuffer);
}
