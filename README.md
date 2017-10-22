# Jenkins Stoplight eXtreme Feedback Device (Jenkins XFD)

A set of status indicator lights, in the form of a stoplight, controlled by a Raspberry Pi, to monitor the current state 
of a continuous build system running Jenkins.

![Stoplight Red Green](doc/stoplight_photo_red_green_scaled.jpg?raw=true "High-priority builds are broken!") ![Stoplight Red Yellow](doc/stoplight_photo_red_yellow_scaled.jpg?raw=true "All builds are broken!")

It could certainly be adapted to control other status indicators, and to monitor other build systems. This repository is
for the software that monitors Jenkins and controlling the lights.

## How it Works

![Architecture Diagram](doc/architecture_diagram.png?raw=true "Architecture Diagram")

Once configured, the stoplight sits on a desk or prominent place and shows the real-time status of your most important
(and separately your less important) continuous integration builds, 24/7 non-stop.  No PC is required.  The Raspberry
Pi controller connects via wifi or wired ethernet connection to monitor the state of your build system. 

## I Want One! What Do I Need?

To build your own extreme feedback device, you'll need a light and a Raspberry Pi to control it. My specific products
ordered:   

- A [USB Traffic Light](https://shop.strato.de/epages/63698188.sf/en_US/?ViewObjectPath=%2FShops%2F63698188%2FProducts%2F43%2FSubProducts%2F43-2) from cleware.de. This was the most expensive piece. I chose this model because (a) it's well built and had the look that I wanted, and (b) at the time I specifically wanted to learn to control devices via USB using Java. You can definitely build your own for cheaper, like using the GPIO to control individual LEDs or relays.   
- A [Raspberry Pi with Enclosure](https://www.amazon.com/gp/product/B01DMFQZXK/ref=oh_aui_detailpage_o00_s00?ie=UTF8&psc=1) from amazon.com. These Vilros kits are great about coming with a complete kit, everything you need (including power supply) except an SD card.
- [8GB MicroSD Card](https://www.amazon.com/gp/product/B007KFXICK/ref=oh_aui_detailpage_o00_s00?ie=UTF8&psc=1), also from amazon.com. You can also get one [with NOOBS already installed](https://www.amazon.com/gp/product/B00VD614PU/ref=oh_aui_detailpage_o06_s00?ie=UTF8&psc=1) to save a step.

## Setting Up the Raspberry Pi

The controller service built by this repository runs as a Java process, so the only hard requirement is that the 
Raspberry Pi have a Java VM. Most Raspberry Pis run some flavor of Linux, but it could technically be Windows 10 IoT or
[any other Raspberry OS](https://www.raspberrypi.org/downloads/).

The exact steps I used to set up the initial device are recorded in the 
[Raspberry Pi Stoplight Installation](doc/INSTALL.md) guide, from installing the operating system to setting up the 
Linux service to auto-start when booted.

## Building and Deploying This Software

This project can be built and tested on any development machine (any platform supported by Gradle) and then deployed to 
a Raspberry Pi.   

### How to Build

Compile and create a single distributable Jar that includes all dependencies using:

`./gradlew fatJar`

That will create `build/libs/jenkins_xfd-all.jar`, which can be deployed to a Raspberry Pi with USB light controls. 

### How to Deploy

The complete Jar file can be simply copied to the Raspberry Pi (or test VM!) manually, usually from 
`build/libs/jenkins_xfd-all.jar` to `/usr/share/java/` on the target machine. However, for iterative 
development it may be more convenient to configure Gradle to deploy directly to your target host.

#### Setting Up the Gradle `deploy` Task

_TODO: We need to have some build properties that specify where the remote device is and how to connect and authenticate, but 
currently it's hardcoded in build.gradle._

First, set up passwordless access to the `pi@stoplight` machine (or whatever `username@hostname` it's called on your network), with something like `cat ~/.ssh/id_rsa.pub | ssh pi@stoplight 'cat >> .ssh/authorized_keys'`.

Then check the `remotes` section of `build.gradle`.  It should look something like:

    remotes {
        stoplightServer {
            host = 'stoplight'
            user = 'pi'
            identity = file('~/.ssh/id_rsa')
        }
    }

Specifying the remote user and host, as well as the local identity file.

Second, run the build to deploy to this target.

    ./gradlew deploy

Note: This build task is often hanging on the client side on Windows machines, even though it successfully deploys.

## Additional Info

- [ToDo List](doc/TODO.md)

