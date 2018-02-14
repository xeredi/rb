#!/bin/sh

echo create user transport
sudo adduser transport
sudo adduser transport sudo

echo update system
sudo apt-get update
sudo apt-get upgrade
sudo rpi-update

echo install oracle-java8-jdk
sudo apt-get install oracle-java8-jdk

echo install gps sw
sudo apt-get install minicom
sudo apt-get install gpsd gpsd-clients python-gps
sudo apt-get install librxtx-java

