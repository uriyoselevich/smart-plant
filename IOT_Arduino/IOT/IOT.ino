#include <ESP8266WiFi.h>
#include <AmazonIOTClient.h>
#include <ESP8266AWSImplementations.h>

#include "DHT.h"        // including the library of DHT11 temperature and humidity sensor
#define DHTTYPE DHT11   // DHT 11

#define dht_dpin 12
DHT dht(dht_dpin, DHTTYPE); 


Esp8266HttpClient httpClient;
Esp8266DateTimeProvider dateTimeProvider;

AmazonIOTClient iotClient;
ActionError actionError;

// ### Technion WIFI ####
//const char* ssid = "eewifi";


//// ### Google Campus WIFI ####
const char* ssid = "Campus-Guest";
const char* password = "helloworld";

void initWLAN()
{
  Serial.println("##### Initializing WIFI #####");
   WiFi.begin(ssid, password);
//  //for Tehcnion:
//  WiFi.begin(ssid);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
  }
  Serial.println("##### WIFI Connected !!! #####");
}

void initAWS()
{
  iotClient.setAWSRegion("us-east-1");
  iotClient.setAWSEndpoint("amazonaws.com");
  iotClient.setAWSDomain("XXXXXXXXXXXX.iot.us-east-1.amazonaws.com");
  iotClient.setAWSPath("/things/esp8266_AA5336/shadow");
  iotClient.setAWSKeyID("XXXXXXXXXXXXXXXXXX");
  iotClient.setAWSSecretKey("XXXXXXXXXXXXX7/9873545Vdfvhfvndf");
  iotClient.setHttpClient(&httpClient);
  iotClient.setDateTimeProvider(&dateTimeProvider);
}

int relay_pin =15; //D8 relay out
int sensor_pin =16; //D0
int led_green = 4; //D2
int led_red = 13; //D7
int cycle_counter = 0 ; //counts half hour cycles

void open_water_valve(){
  digitalWrite(relay_pin, LOW); // relay opens in LOW
  delay(5000);//give water for 5 sec
  digitalWrite(relay_pin, HIGH);//close valve
  delay(200);
}

  
void setup() {
  Serial.begin(115200);
  delay(10);
  Serial.println("begin");
  initWLAN();
  Serial.println("wlan initialized");
  initAWS();
  Serial.println("iot initialized");
  pinMode(sensor_pin, INPUT);
  pinMode(led_green, OUTPUT); // Declare the LED as an output
  pinMode(led_red, OUTPUT); // Declare the LED as an output
  digitalWrite(led_green, HIGH); // Turn the LED on
  digitalWrite(led_red, LOW); // Turn the LED off
  pinMode(relay_pin, OUTPUT);//relay out
  digitalWrite(relay_pin, HIGH);
 // cycle_counter = 0;
}

void loop()
{

    cycle_counter+=1; //count cycles of half hour
    Serial.println("NEW LOOP! Cycle counter is :");
    Serial.println(cycle_counter);
    delay(500);
    if(cycle_counter >= 100){ // check if not counting too much
      cycle_counter = 3;
    }
    float h = dht.readHumidity();
    float t = dht.readTemperature(); 
    delay(500);
    //check values:
    if(h > 100.00 || h < 0.0 ){
      h = 80.00;  //default      
    }
    if(t > 60.00 || t<0.00 ){
      t = 25.00; //default
    }
    Serial.print("Current humidity = ");
    Serial.print(h);
    Serial.print("%  ");
    Serial.print("temperature = ");
    Serial.print(t); 
    Serial.println("C  ");
    
  int sensorValue = analogRead(A0);//light sensor
  int DsensorValue = digitalRead(sensor_pin);//digitalRead(D0) - soil mositure;
  Serial.println("##### Here is the Light measurement : #####");
  Serial.println(sensorValue);
  Serial.println("##### Here is the Mositure measurement : #####");
  Serial.println(DsensorValue);//0 = no need for water.
  char shadow[100];
  char LightVal[12];
  char TempVal[12];
  char HumidityVal[12];
  char needsWater[10] = "\"FALSE\"";
  digitalWrite(led_green, HIGH); // Turn the LED on
  digitalWrite(led_red, LOW); // Turn the LED off
  if(DsensorValue){ // 1 = need water!
    strcpy(needsWater,"\"TRUE\"");
    digitalWrite(led_green, LOW); // Turn the LED off
    digitalWrite(led_red, HIGH); // Turn the LED on
  }
  

  sprintf(LightVal, "%d", sensorValue); 
  sprintf(TempVal, "%d", (int)t);
  sprintf(HumidityVal, "%d", (int)h);
  strcpy(shadow, "{\"state\":{\"reported\":{\"Temp\":");//121, \"Moisture\":");//$sensorValue}}}");
  strcat(shadow,TempVal); //Yes or No water
  strcat(shadow,",\"Humidity\": ");
  strcat(shadow,HumidityVal); 
  strcat(shadow,",\"Light\": ");
  strcat(shadow,LightVal); 
  strcat(shadow,",\"NeedsWater\": ");
  strcat(shadow,needsWater); 
  strcat(shadow,"}}}");
  Serial.println("##### Here is the Shadow you created : #####");
  Serial.print(shadow);
  Serial.println("");

  //water algorythm :
  if(DsensorValue == 1){//soil is dry - NeedsWater  
    if(sensorValue >= 700){ // light is above min val
      if(cycle_counter >= 2){//1 hour from last watering
        open_water_valve();
        delay(500);
        cycle_counter = 0;
      } 
    }
  }

//test relay block:
//  if(cycle_counter>=1){
//    digitalWrite(relay_pin, LOW);
//    cycle_counter = 0 ;
//  }else{
//    digitalWrite(relay_pin, HIGH);
//    cycle_counter++;
//  }
  Serial.println("# Current cycle counter is : #");
  Serial.print(cycle_counter);
  Serial.println("");
  
  char* result = iotClient.update_shadow(shadow, actionError);
  Serial.print(result);

 
  //Half an  hour!
  delay(1800000);
}
