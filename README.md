_Language versions:_\
[![EN](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-US.png)](https://github.com/rjaros87/pm-station-usb) 
[![PL](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-PL.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=pl&u=https://github.com/rjaros87/pm-station-usb)
[![DE](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-DE.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=de&u=https://github.com/rjaros87/pm-station-usb) 


# Home use particulate matter sensor on USB

Since we live in big polluted cities the air quality is on a very poor level. But, the problem is not only connected to the big cities - even smaller cities and villages may suffer from air pollution.
That's why we've created this little project to let you build your own Particulate Matter monitoring station and use our open-source software so you could monitor the air quality in your place of living.

Let's live healthier life and start measuring the air quality and filtering it if necessary.

## Requirements

- PlanTower PMS7003 ~ 24 $
- ICD10 adapter with 10 pins(1.27 mm) to 6 pins (2.54 mm) (also called `G7 switch`) ~ 2 $
- Female-Female wires 5 items ~ 1 $
- UART to USB (PL2303) adapter ~ 2 $
- USB cable ~ 2 $

## Assemble the PM sensor

1. Connect the PlanTower device with ICD10 adapter (~~like on photo below~~). 
> Caution! The pins on the PlanTower device are located in the top right corner. Numeration starts from the right top corner (1) and ends in the left bottom corner (10). 

2. Connect IDC10 adapter (bigger pins :smile:) to the UART-USB adapter. Wire connections are presented in a table below
> Caution! The adapter must have a separate VCC pin for 5V or a switch between 3.3V and 5V since the PlanTower device works on 5V voltage.

ICD10 adapter | UART-USB adapter
------------- | ----------------
VCC (5V)      | VCC (5V)
GND           | GND
RX            | TXD
TXD           | RXD

3. Connect the UART-USB adapter with a USB cable to your computer
> Caution! Refer to [this](#tested-platforms) section for remarks regarding your platform

- For Bluetooth version connect the power (5V) from USB cable or socket to Vcc and GND (2 most outside pins of USB plug). You may need to shortcircuit data pin/wires (2 center pins) or connect them via ~200ohm resistor in order to be recognized by charger. Details can be found [here](http://pinouts.ru/Slots/USB_pinout.shtml "USB pinout").

4. Start the application and check the air quality of your environment :smile:

## 3D enclosure for the PlanTower device & the UART-USB adapter

- 4x M3 x 25 machine screws with nuts are required to assemble the enclosure

- [Top v3 with hidden screws](/3d_models/top_v3_hidden_screws.stl)
- [Bottom v3 with hidden screws](/3d_models/bottom_v3_hidden_screws.stl)

or

- [Top v1](/3d_models/top_v1.stl)
- [Bottom v1](/3d_models/bottom_v1.stl)

Suggested printing parameters:
- Infill: 20%
- Resolution: 0.2
- Supports: No

> Caution! Make sure the soldered ends of the USB-UART pins (at the bottom of the adapter) are properly insulated from the sensor's metal case! Some adapters have them so long that they become too close to the sensor and may short-circuit the adapter. It's best to sand them off and use insulation tape.

## Tested platforms

- Android APK
  - Android 7 Nougat (for USB version the OTG feature must be supported)

- Desktop (standalone) app
  - Windows 7 & 10
    - before running the app, open the Device Manager, expand `Ports (COM & LPT)` and find your adapter. Ensure that the Device status states `This device is working properly`. If not, then you probably need to install older drivers for this adapter (a workaround).
  - Linux Ubuntu 16.04
    - the ownership of device `/dev/ttyUSB0` is `dialout` group so must add yourself to the group and re-login or run with `sudo` (not recommended)
  - macOS Sierra 10.12
    - you may need to install correct driver for your version of uart-to-usb
      - for PL2303 you can use [this](http://www.prolific.com.tw/US/ShowProduct.aspx?p_id=229&pcid=41) one, however for some uart-to-usb versions you must always detach the device before closing the app otherwise the java process may hang blocked by the driver on I/O operation.
      - for FT232 the built-in (com.apple.driver.AppleUSBFTDI) driver just works, what's more FT232 based uart-to-usb implementations behave much better on macOS (see above)

## Build and run


### Requirements

- Java Development Kit 8+

#### Android:
- Android Studio 3.0 (Android Plugin for Gradle 3.0.0+)
- Build tools and SDK 27+

### Useful commands

#### Desktop:
- `gradlew desktop:jar` - create a single Jar file with the desktop application
- `gradlew desktop:run` - run the desktop application

#### Android:
- `gradlew android:build` - create APK-s for Android devices

## Contributing

* [Contribution Guidelines](/CONTRIBUTING.md)

## License

- [License GPL-3.0](/LICENSE)


## Authors

- [Radoslaw Gabiga](https://github.com/sanchin)
- [Radoslaw Jaros](https://github.com/rjaros87)
- [Piotr Skowronek](https://github.com/pskowronek)

## TODOs

- [Release 1.0.0](https://github.com/rjaros87/pm-station-usb/projects/1)

## Disclaimer

The information provided on this project is true and complete to the best of our knowledge. However, like various Do-It-Yourself (DIY) projects this DIY Project is at your own risk only.
As with any DIY project, unfamiliarity with the tools and process can be dangerous. We are not liable for any damage or injury resulting from the assembly or use of this project including but not limited to hardware damage, body injuries etc.

You have been warned.
