# Home particulate matter station on USB

Since we live in huge polluted cities, the air quality is on a very poor level. I have created this little project for monitoring our home space. Let's live healthier life and start from filtering air and measuring its quality.

## Requirements

- PlanTower PMS7003 ~ 24 $
- ICD10 adapter with 10 pins(1.27 mm) to 6 pins (2.54 mm) (also called `G7 switch`) ~ 2 $
- Female-Female wires 5 items ~ 1 $
- UART to USB (PL2303) adapter ~ 2 $
- USB cable ~ 2 $

## Assemble the home PM station

1. Connect the PlanTower device with ICD10 adapter
> Caution! The pins on the PlanTower device are located in the top right corner. Numeration starts from the right top corner (1) and ends in the left bottom corner (10). 

2. Connect IDC10 adapter (bigger pins :smile:) to the UART-USB adapter. Wire connections are presented in table below
> Caution! The adapter must have separate VCC pin for 5V or switch between 3.3V and 5V. The PlanTower device works on 5V voltage.

ICD10 adapter | UART-USB adapter
------------- | ----------------
VCC (5V)      | VCC (5V)
GND           | GND
RX            | TXD
TXD           | RXD

3. Connect the UART-USB adapter with a USB cable to your computer
> Caution for Windows users! - open the Device Manager, expand `Ports (COM & LPT)` and find your adapter. Ensure that the Device status has the following text `This device is working properly.`. If not, then you probably need to install  old drivers for this adapter (workaround).

4. Start the application and check your air quality :smile:

## Assembly photos

The photo gallery of assembly can be found [here](https://github.com/rjaros87/pm-station-usb/raw/master/www/assembly/index.html "Assembly of pm-station-usb").


## Tested platforms

- Windows 7/10
- Linux (The ownership of device `/dev/ttyUSB0` is `dialout` group so add yourself and re-login or run with `sudo`)
- ~~macOS~~

## Build and run


### Requirements

- Java Development Kit 8+

### Useful commands

- `gradlew jar` - create a single Jar file
- `gradlew run` - run the application
- `gradlew distZip` - create a zip file with executable binary file and include all dependencies

## TODO

- Add config file
- Add observer which log measurements to file
- Add support for others PlanTower devices
- Add 3D model project with covers for the PlanTower device & the UART-USB adapter
- Tests
