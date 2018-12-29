README.txt

Hey All!

This is the Arduino code for the SmartPlant Project
This code is designed for the following HW:

1. NodeMcu + ESP8266 processor.
2. Soil Moisture sensor - https://www.gearbest.com/lcd-led-display-module/pp_1604163.html?wid=1433363&currency=ILS&vip=4446207&gclid=Cj0KCQiA05zhBRCMARIsACKDWjefo3_WkRJnF0epIDUyFFAgVdG-GHNZP8IAownGqWmCjsWxtK8qVNQaAooLEALw_wcB
3. Air humidity and Temp sensor - DHT11 - https://howtomechatronics.com/tutorials/arduino/dht11-dht22-sensors-temperature-and-humidity-tutorial-using-arduino/
4. Light sensor (diaode) - https://maker.pro/arduino/tutorial/how-to-use-an-ldr-sensor-with-arduino

In order to use this code you should dowload the following libraries:
1. ESP8266WiFi.h - library for wifi connectivity
2. DHT.h - use DHT11 with built in methods
3. AmazonIOTClient.h + ESP8266AWSImplementations.h - use this device as a"thing" and connect it to AWS (Amazon)

In order to get your AWS credentials and other parameter you should
follow AWS tutorial about how to initialize a "thing".

hope you enjoy your smart plant!