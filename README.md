# Pi4J Demo

This is a Spring-based demo project to show how a Vaadin User Interface (website) can interact with the GPIOs of a
Raspberry Pi by using the [Pi4J library](https://www.pi4j.com).

Initially, the code and included examples are created to interact with components of
the [CrowPi](https://www.elecrow.com/mcu/raspberry-pi/development-kit.html) and have been used in various demos with
both the CrowPi 1, 2, and 3. As they contain different components, not all of the examples work identically on the all
devices. Therefor, a config is added, which needs to be specified
in [the enum DemoSetupConfig.java](src/main/java/be/webtechie/vaadin/pi4j/service/DemoSetupConfig.java) and [
`DemoSetupConfig DEMO_SETUP_CONFIG` in Pi4JService.java](src/main/java/be/webtechie/vaadin/pi4j/service/Pi4JService.java)

Ofcourse, you don't need to use a CrowPi to use this code. You can use any other Raspberry Pi with a GPIO header and
connect the electronic components directly to the GPIO pins. Change the pin numbers as needed, and you are good to go.

Following versions of this code are available in branches:

* [main](https://github.com/FDelporte/Vaadin-examples/tree/main):
    * Dependencies got upgraded to latest versions in September 2025 of Spring Boot, Vaadin
    * Many code changes to be compatible with those new versions.
    * Uses Pi4J V4.0.0-SNAPSHOT.
    * Uses Pi4J Drivers V0.0.1-SNAPSHOT.
    * Separate services per type of device to make it easier to understand and reuse in other applications.
* [vaadin-23-spring-2.6](https://github.com/FDelporte/Vaadin-examples/tree/vaadin-23-spring-2.6): Initial example with
  LED output and button input, based on Vaadin 23 and Spring Boot 2.6. It is described in detail
  in [this Foojay.io blog post](https://foojay.io/today/blink-a-led-on-raspberry-pi-with-vaadin/).
* [vaadin-24-spring-3.0](https://github.com/FDelporte/Vaadin-examples/tree/vaadin-24-spring-3.0): Example used during a
  few conference talks,
  e.g. [Controlling Electronics with Java and Pi4J through a web interface (J-Spring 2023)](https://www.youtube.com/watch?v=FXKsBKKB_Xg)
  and [Unlocking the Potential of Bits and Bytes (Devoxx 2023)](https://www.youtube.com/watch?v=ex0t2uaL27I).

## Vaadin UI

The base code with different Vaadin layouts, was generated on [start.vaadin.com/app](https://start.vaadin.com/app).

![Vaadin UI](doc/screenshot-running-on-rpi.png)

## Components

The application can interact with various components in the CrowPi1.

![Demo content on CrowPi](doc/demo-content-on-crowpi.jpg)

## Upload to Raspberry Pi

If you are developing on a PC, you can build the application with the following command

* Windows: `mvnw clean package -Pproduction`
* Mac & Linux: `./mvnw clean package -Pproduction`

and upload to your Raspberry Pi with (replace login `pi` and the IP address with the one of your board)

```shell
$ scp target/pi4jdemo-1.0-SNAPSHOT.jar pi@192.168.0.222://home/pi/
```

Build and upload in one command:

```shell
./mvnw clean package -Pproduction && scp target/pi4jdemo-1.0-SNAPSHOT.jar frank@crowpi3.local://home/frank/
```

## Run on Raspberry Pi

PWM and DHT11 need to be enabled on the Raspberry Pi. For example, on a Raspberry Pi 5:

```shell
$ sudo nano /boot/firmware/config.txt 
# Add at the end of the file:
dtoverlay=pwm-2chan,pin=18,func=2,pin2=12,func2=4
dtoverlay=dht11,gpiopin=4
```

We can now start the application on your Raspberry Pi to interact with the GPIOs. Make sure to specify the version of
the CrowPi you are using (1, 2, or 3).

```shell
$ java -jar /home/pi/pi4jdemo-1.0-SNAPSHOT.jar --crowpi.version=3
```
