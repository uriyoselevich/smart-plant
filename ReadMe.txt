ReadMe.txt

Welcome to smart plant project.

In this project I used HW + sensors to send my plant's vitals to AWS and present them in an Android App.

Process :

1. Sensors sampling - every 60 minutes the last measurement is stored onboard the NodeMcu from all sensors.

2. Cloud connectivity - The device then generates a MQTT request and update the device's shadow in AWS.

3. Data storage - Using IOT rules - measurements are stored in DynamoDB table.

4. Application - Android App querys the DynamoDB table and present last measurements and a graph ativity of the last 30 days.


HW - was developed with Arduino IDE.
App - was developed in Android Studio.

Enjoy!