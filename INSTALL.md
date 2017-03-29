# Raspberry Pi Stoplight Installation

1. Get a MicroSD card with NOOBS on it.
    1.	Apparently you can buy MicroSD cards with NOOBS pre-installed on it.  I didn’t do this, INSTEAD, I did:
    1.	Format a MicroSD card and copy NOOBS files to it:
        1.	On a PC, download the latest [NOOBS installer](https://www.raspberrypi.org/documentation/installation/noobs.md).  This is a general OS install manager.  When its files are put on an SD card, the Raspberry will boot to the installer UI and prompt for installing an OS.
        1.	Format the MicroSD card.  I used the official formatter from http://sdcard.org, but I don’t think that was necessary.
        1.	Expand the files from the downloaded NOOBS .zip onto the MicroSD card.
1. If using a Vilros kit (which is nice!), the plastic case has to be assembled—like adding rubber feet.  Also, the peal-and-stick heatsinks can just be applied to the two processors on the board.  Insert the MicroSD card after installing the board into its case.
1.	Unplug the Raspberry Pi, put the MicroSD card into it.  Also connect a temporary USB keyboard, USB mouse, and HDMI display.  Then plug the Raspberry Pi in.
1.	The NOOBS OS install manager will have Raspbian as a built-in option.  (If connected to a network, it will give other options as well.)  I picked the default Raspbian to install that.
1.	After installation it will boot Raspbian.
    1.	Go to application menu&rarr;Preferences&rarr;Raspberry Pi Configuration and
    1.	Click “Change Password” and set a new password for the pi user.
    1.	Under “Interfaces”, enable SSH.
    1.	Change the “Hostname” to something else (I used ‘stoplight’).
    1.	Allow it to reboot after this change.
1.	Connect the Raspberry Pi to a network.
    1.	If it’s using a hardwired Ethernet cable, there’s nothing else to do.  OR,
    1.	Using the temporary mouse, keyboard, and HDMI display, click on the network icon in the upper right toolbar, select the wireless access point, and (if needed) enter the private key.
1.	Upgrade and update the OS.
    1.	I use `ssh pi@stoplight` from a PC.  You can also open a terminal via the interactive session.
    1.	`sudo apt-get upgrade`
        `sudo apt-get dist-upgrade`  (confirm ‘yes’ to continue)
        (Edit: Might be better to run this from the interactive terminal, because I got several messages saying `(gconftool-2:1868): GConf-WARNING **: Client failed to connect to the D-BUS daemon: Unable to autolaunch a dbus-daemon without a $DISPLAY for X11`.)
    1.	I rebooted (`sudo shutdown -r now`).  Probably not necessary.
1.	Configure OpenVPN to connect to the Innotas VPN.
    1.	Install OpenVPN with `sudo apt-get install openvpn`.
    1.	From a browser, on a PC or the Raspberry Pi, go to https://aws-dev.innotas.com.
        1.	On a PC, select “Login” rather than Connect, and provide your LDAP username and password.
    1.	After login, click the link to download a connection profile for “Yourself (autologin profile)” and place it in `/etc/openvpn/` with a `.conf` extension.
        1.	I renamed the file from `client.ovpn` to `client_autologin.ovpn.conf`.
        1.	Then I copied from my PC to the Raspberry using scp: 
            1.	From my PC:
                `scp /d/tmp/client_autologin.ovpn.conf pi@stoplight:/tmp/`
            1.	Then from the Raspberry Pi:
                `sudo mv /tmp/client_autologin.ovpn.conf /etc/openvpn/`
                `chmod 755 /etc/openvpn/client_autologin.ovpn.conf`
    1.	Edit the file `/etc/default/openvpn` on the Raspberry Pi (`sudo vi /etc/default/openvpn`) and uncomment the line that says `AUTOSTART="ALL"`.
    1.	From the Raspberry Pi, `ping ci1s2.innotas.net`.  It probably won’t work.
    1.	Reboot the Raspberry Pi (`sudo shutdown -r now`).
    1.	From the Raspberry Pi, `ping ci1s2.innotas.net`.  It should work if our VPN is set up and connected.
1.	Install the USB drivers needed to control the stoplight.
    1. Following steps similar to the outdated ones on this page, which installs the ClewareControl utility from https://www.vanheusden.com/clewarecontrol/:
        1.	Install the pre-requisites that are listed on https://github.com/flok99/clewarecontrol.  From the Raspberry, do:
            `sudo apt-get -y install libhidapi-dev`
        1.	Now download, build, and and install clewarecontrol itself on the Raspberry:
```
cd ~
mkdir dev
cd dev
wget https://www.vanheusden.com/clewarecontrol/files/clewarecontrol-4.4.tgz
tar xvfz clewarecontrol-4.4.tgz
cd clewarecontrol-4.4
sudo make install
```
    1. Now the command `sudo clewarecontrol -l` from a Raspberry command-line should list the available device(s).
1.	Install the stoplight controller Java app.  (This can probably be automated in a better way, but here are the manual steps.)
    1.	Build the complete Jar from the jenkins_xfd project, `jenkins_xfd-all.jar`.
    1.	Move this file to `/usr/share/java/` on the Raspberry Pi.
        1.	On the dev machine: `scp build/libs/jenkins_xfd-all.jar pi@stoplight:/tmp/`
        1.	On the Raspberry Pi machine:
```
sudo mv /tmp/jenkins_xfd-all.jar /usr/share/java
sudo chown root /usr/share/java/jenkins_xfd-all.jar
sudo chgrp root /usr/share/java/jenkins_xfd-all.jar
sudo chmod 644 /usr/share/java/jenkins_xfd-all.jar
```
    1.	Set up start/stop scripts.
```
sudo mkdir /var/log/stoplight/
sudo vi /usr/local/bin/stoplight-start.sh
```
    1. add add this content:

```
    #!/bin/bash
    
    cd /var/log/stoplight
    java -jar /usr/share/java/jenkins_xfd-all.jar >> stdout_stoplight.log 2>> stderr_stoplight.log
```
        1.	`sudo chmod 755 /usr/local/bin/stoplight-start.sh`
        1.	`sudo vi /usr/local/bin/stoplight-stop.sh`
            add add this content:
```
    #!/bin/bash
    pid=`ps aux | grep jenkins_xfd | grep -v grep | awk '{print $2}'`
    kill -9 $pid
```
        1.	`sudo chmod 755 /usr/local/bin/stoplight-stop.sh`
        1.	`sudo vi /etc/init.d/stoplight`
            add add this content:
```
#!/bin/bash
### BEGIN INIT INFO
# Provides:          stoplight
# Required-Start:    $openvpn $syslog
# Required-Stop:     $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: stoplight
# Description:       Controls a USB stoplight as a Jenkins extreme feedback device.
### END INIT INFO

case $1 in
    start)
        sleep 15;
        /bin/bash /usr/local/bin/stoplight-start.sh
    ;;
    stop)
        /bin/bash /usr/local/bin/stoplight-stop.sh
    ;;
    restart)
        /bin/bash /usr/local/bin/stoplight-stop.sh
        /bin/bash /usr/local/bin/stoplight-start.sh
    ;;
esac
exit 0
```
        1. `sudo chmod 755 /etc/init.d/stoplight`
        1. `sudo update-rc.d stoplight defaults`
    1. Reboot.

