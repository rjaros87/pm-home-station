_Language versions:_\
[![EN](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-US.png)](https://github.com/rjaros87/pm-station-usb) 
[![PL](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-PL.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=pl&u=https://github.com/rjaros87/pm-station-usb)
[![DE](https://github.com/rjaros87/pm-station-usb/raw/master/www/flags/lang-DE.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=de&u=https://github.com/rjaros87/pm-station-usb) 


# Home use particulate matter sensor on USB

Since we live in huge polluted cities the air quality is on a very poor level. We have created this little project for monitoring our home environment. Let's live healthier life and start from filtering air and measuring its quality.

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
> Caution for Windows users! - open the Device Manager, expand `Ports (COM & LPT)` and find your adapter. Ensure that the Device status has the following text `This device is working properly.`. If not, then you probably need to install old drivers for this adapter (workaround).

4. Start the application and check the air quality of your environment :smile:

## 3D enclosure for the PlanTower device & the UART-USB adapter

- 4x M3 x 25 machine screws with nuts are required to assemble the enclosure

- [Top v2 with hidden screws](/3d_models/top_v2_hidden_screws.stl)
- [Bottom v2 with hidden screws](/3d_models/bottom_v2_hidden_screws.stl)

or

- [Top v1](/3d_models/top_v1.stl)
- [Bottom v1](/3d_models/bottom_v1.stl)

Suggested printing parameters:
- Infill: 20%
- Resolution: 0.2
- Supports: No

## Tested platforms

- Windows 7/10
- Linux (The ownership of device `/dev/ttyUSB0` is `dialout` group so add yourself and re-login or run with `sudo`)
- macOS / OSX (you may need to install correct driver for your version of uart2usb, for PL2303 [this](http://www.prolific.com.tw/US/ShowProduct.aspx?p_id=229&pcid=41) one works well)

## Build and run


### Requirements

- Java Development Kit 8+

#### Android:
- Android Studio 3.0 (Android Plugin for Gradle 3.0.0+)
- Build tools and SDK 26+

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
