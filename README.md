_Language versions:_\
[![EN](https://github.com/rjaros87/pm-home-station/raw/master/www/flags/lang-US.png)](https://github.com/rjaros87/pm-home-station) 
[![PL](https://github.com/rjaros87/pm-home-station/raw/master/www/flags/lang-PL.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=pl&u=https://github.com/rjaros87/pm-home-station)
[![DE](https://github.com/rjaros87/pm-home-station/raw/master/www/flags/lang-DE.png)](https://translate.googleusercontent.com/translate_c?sl=en&tl=de&u=https://github.com/rjaros87/pm-home-station) 
<a href="https://zrzutka.pl/en/ra6pw7/wplac"><img src="https://img.shields.io/badge/Donate-zrzutka.pl-green.svg" height="24" align="right" /></a>

# Home use particulate matter sensor [![Build Status](https://travis-ci.org/rjaros87/pm-home-station.svg?branch=master)](https://travis-ci.org/rjaros87/pm-home-station)

Since we live in big polluted cities the air quality is on a very poor level. But, the problem is not only connected to the big cities - even smaller cities and villages may suffer from air pollution.
That's why we've created this little project to let you build your own Particulate Matter monitoring station and use our open-source software so you could monitor the air quality in your place of living.

Let's live healthier life and start measuring the air quality and filtering it if necessary.

|  Android  | iPhone & Watch |    Desktop    |
|-----------|----------------|---------------|
|<a href="https://play.google.com/store/apps/details?id=pmstation.android&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="58" /></a>|<a href="https://itunes.apple.com/us/app/pm-home-station/id1347597971?mt=8"><img src="https://linkmaker.itunes.apple.com/assets/shared/badges/en-us/appstore-lrg.png" width="135" height="40" /></a>|<a href="https://github.com/rjaros87/pm-home-station/releases"><img alt="Download and install" src="https://github.com/rjaros87/pm-home-station/raw/master/www/other/laptop+install.png" width="135" height="58"/></a>|

## Hardware Requirements

### USB version (supported by Android & Desktop apps)
- [PlanTower PMS7003](https://kamami.pl/czujniki-gazow/564008-plantower-pms7003-laserowy-czujnik-pylu-pm25.html) ~$26
- [ICD10 adapter with 10 pins(1.27 mm) to 6 pins (2.54 mm) (also called `G7 switch`)](https://kamami.pl/zlacza-inne/564553-adapter-idc10-127mm-na-zlacze-254mm-dla-czujnika-pms7003.html) ~$2
- UART to USB adapter:
  - [cheap PL2303](https://kamami.pl/konwertery-usb-uart-rs232/561382-modul-konwertera-usb-uart-rs232-ttl-z-ukladem-pl2303hx.html?search_query=PL2303&results=11) ~$2, or
  - [better quality FT232 (suggested for macOS for its superb compatibility)](https://kamami.pl/konwertery-usb-uart-rs232/561568-konwerter-usb-uart-ft232rl-waveshare.html?search_query=FT232&results=58) ~$5
- [Female-Female wires 5 items (required for PL2303 otherwise optional since FT232 can be bought with wires already included)](https://kamami.pl/przewody-f-f/199418--przewody-polaczeniowe-f-f-roznokolorowe-17-cm-10-szt.html) ~$1-2
- [USB OTG cable](https://kamami.pl/przewody-usb/560191-przewod-adapter-microusb-otg.html) ~$1

### Bluetooth 2.0 (supported by Desktop app, possible support by Android app)
> Caution! Connectivity to the sensor over Bluetooth is currently only supported by the desktop application ran on macOS.

> Caution! If you want to connect from Apple iPhone or Apple Watch - please see Low Energy version (below)

- [PlanTower PMS7003](https://kamami.pl/czujniki-gazow/564008-plantower-pms7003-laserowy-czujnik-pylu-pm25.html) ~$26
- [ICD10 adapter with 10 pins(1.27 mm) to 6 pins (2.54 mm) (also called `G7 switch`)](https://kamami.pl/zlacza-inne/564553-adapter-idc10-127mm-na-zlacze-254mm-dla-czujnika-pms7003.html) ~$2
- [Female-Female wires 5 items](https://kamami.pl/przewody-f-f/199418--przewody-polaczeniowe-f-f-roznokolorowe-17-cm-10-szt.html) ~$1-2
- [Bluetooth UART HC-06](https://kamami.pl/moduly-bluetooth/198260-zestaw-bluetooth-z-modulem-hc-06-20edr-zasilany-3-6-6-v.html?search_query=hc-06&results=2) ~$7
- recycled old USB cable to provide power to Sensor and Bluetooth tandem

### Bluetooth Low Energy (supported by iOS app, possible support by Android app)
> Caution! Connectivity to the sensor over Bluetooth LE is currently only tested on the iOS application running on iPhone or Apple Watch. Multi-platform desktop support not planned in near feature.

- [PlanTower PMS7003](https://kamami.pl/czujniki-gazow/564008-plantower-pms7003-laserowy-czujnik-pylu-pm25.html) ~$26
- [ICD10 adapter with 10 pins(1.27 mm) to 6 pins (2.54 mm) (also called `G7 switch`)](https://kamami.pl/zlacza-inne/564553-adapter-idc10-127mm-na-zlacze-254mm-dla-czujnika-pms7003.html) ~$2
- [Bluetooth Low Energy UART HC-08](https://kamami.pl/moduly-bluetooth/562367-modhc-08-modul-bluetooth-hc-08.html) (includes wires) ~$7
- recycled old USB cable to provide power to Sensor and Bluetooth tandem

## Discount offers

You may want to use the following rebate coupon: __A9XS1FPF__ on purchases made on [KAMAMI.COM](https://kamami.com) or [KAMAMI.PL](https://kamami.pl) to get 5% OFF on orders which include PMS7003 (to be used only once for each buyer).

## Assemble the PM sensor

### USB version

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

4. Start the application and check the air quality of your environment :smile:

### Bluetooth version

1. Follow 1-3 steps of USB instruction above, but use your Bluetooth or Bluetooth LE module instead of UART-USB adapter

2. Cut off any old USB cable

3. Connect the power (5V) from USB cable or socket to Vcc and GND (2 most outside pins of USB plug, usually red 5V cable and black GND). You may need to shortcircuit data pin/wires (2 center pins) or connect them via ~200ohm resistor in order to be recognized by charger. Details can be found [here](http://pinouts.ru/Slots/USB_pinout.shtml "USB pinout").

## 3D printed enclosure

- 4x M3 x 25 machine screws with nuts are required to assemble the enclosure

- [Top v3 with hidden screws](/3d_models/top_v3_hidden_screws.stl)
- [Bottom v3 with hidden screws](/3d_models/bottom_v3_hidden_screws.stl)

or

- [Top v1](/3d_models/top_v1.stl)
- [Bottom v1](/3d_models/bottom_v1.stl)

or for FT232 usb-uart adapter

- [Top v4 with hidden screws for FT232 usb-uart adapter](/3d_models/top_v4_hidden_screws_ft232_adapter.stl)
- [Bottom v3 with hidden screws](/3d_models/bottom_v3_hidden_screws.stl)


Suggested printing parameters:
- Infill: 20%
- Resolution: 0.2
- Supports: No

> Caution! Make sure the soldered ends of the USB-UART pins (at the bottom of the adapter) are properly insulated from the sensor's metal case! Some adapters have them so long that they become too close to the sensor and may short-circuit the adapter. It's best to sand them off and use insulation tape.

## Assembly photos

The photo galleries of assembly:
- [PL2303 USB-to-UART version](https://rjaros87.github.io/pm-home-station-project/assembly/index.html "Assembly of pm-home-station using PL2303")
- [FT232 USB-to-UART version](https://rjaros87.github.io/pm-home-station-project/assembly-ft232/index.html "Assembly of pm-home-station using FT232")
- [UART-over-Bluetooth version](https://rjaros87.github.io/pm-home-station-project/assembly-bt/index.html "Assembly of pm-home-station over Bluetooth")

## Enclosure photos

The photo gallery of assembled device in 3d printed enclosure can be found [here](https://rjaros87.github.io/pm-home-station-project/enclosure/index.html "3d printed enclosure of pm-home-station").

## App Screenshots

The app screenshots gallery can be found [here](https://rjaros87.github.io/pm-home-station-project/screenshots/index.html "App screenshots of pm-home-station apps").

## Tested platforms

[See wiki page](https://github.com/rjaros87/pm-home-station/wiki/Tested-(supported)-platforms)

## Build and run

[See wiki page](https://github.com/rjaros87/pm-home-station/wiki/Building-&-Requirements)


## Contributing

[Contribution Guidelines](/CONTRIBUTING.md)

## License

[License GPL-3.0](/LICENSE)

## Authors

- [Radoslaw Gabiga](https://github.com/sanchin)
- [Radoslaw Jaros](https://github.com/rjaros87)
- [Piotr Skowronek](https://github.com/pskowronek)
- [Jerzy Łukjaniec](https://github.com/idf3d)

## Releases

The latest release versions can be found [here](https://github.com/rjaros87/pm-home-station/releases).

## Disclaimer

The information provided on this project is true and complete to the best of our knowledge. However, like various Do-It-Yourself (DIY) projects this DIY Project is at your own risk only.
As with any DIY project, lack of familiarity with the tools and process can be dangerous. We are not liable for any damage or injury resulting from the assembly or use of this project including but not limited to hardware damage, body injuries etc.

You have been warned.

<sub><sup>Google Play and the Google Play logo are trademarks of Google LLC.</sup></sub>

<sub><sup>Apple, the Apple logo, Apple Watch and iPhone are trademarks of Apple Inc., registered in the U.S. and other countries. App Store is a service mark of Apple Inc., registered in the U.S. and other countries.</sup></sub>
