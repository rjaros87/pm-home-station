/*
 * pm-home-station
 * 2017 (C) Copyright - https://github.com/rjaros87/pm-home-station
 * License: GPL 3.0
 */
package pmstation.core.serial;

public interface ISerialUART {
    byte[] readBytes(int dataLenght);
    void writeBytes(byte[] writeBuffer);
    boolean isConnected();
}
