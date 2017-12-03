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

- [Top v2 with hidden screws](/3d_models/top_v2_hidden_screws.stl)
- [Bottom v2 with hidden screws](/3d_models/bottom_v2_hidden_screws)

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

### Useful commands

- `gradlew jar` - create a single Jar file
- `gradlew run` - run the application
- `gradlew distZip` - create a zip file with executable binary file and include all dependencies

## TODOs

- Add photos to `Assemble the home PM station`
- Add config file
- Add observer which log measurements to file
- Add support for others PlanTower devices
- Tests
