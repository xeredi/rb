#!/bin/sh
echo wifi_casa
wpa_passphrase "Orange-D570" "E95CA79F" | sudo tee -a /etc/wpa_supplicant/wpa_supplicant.conf > /dev/null
echo wifi_casa_repetidor
wpa_passphrase "Orange-D570_EXT" "E95CA79F" | sudo tee -a /etc/wpa_supplicant/wpa_supplicant.conf > /dev/null
echo reconfigure_wlan0
wpa_cli -i wlan0 reconfigure
echo thats_all_folks

