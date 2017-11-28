package pmstation;

import pmstation.core.serial.ISerialUART;

/**
 * Created by sanch on 28.11.2017.
 */

public class SerialUART implements ISerialUART {

    @Override
    public byte[] readBytes(int dataLenght) {
        return new byte[0];
    }

    @Override
    public void writeBytes(byte[] writeBuffer) {

    }
}
