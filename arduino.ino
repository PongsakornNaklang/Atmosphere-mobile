#include <ArduinoJson.h>
#include <FirebaseArduino.h>
#include <ESP8266WiFi.h>
#include <TinyGPS++.h>

#define WIFI_SSID ""
#define WIFI_PASSWORD ""
#define FIREBASE_HOST ""
#define FIREBASE_KEY ""

// Arduino pin numbers.
const int sharpLEDPin = D5; // Arduino digital pin D5 connect to sensor LED.
const int sharpVoPin = A0; // Arduino analog pin A0 connect to sensor Vo.

// For averaging last N raw voltage readings.
#define N 100
static unsigned long VoRawTotal = 0;
static int VoRawCount = 0;
static float Voc = 0.9; // Set the typical output voltage in Volts when there is zero dust. 
const float K = 0.5; // Use the typical sensitivity in units of V per 100ug/m3.
int VoRaw = 0;
float dustDensity = 0;
float dV = 0;
float Vo =0;
TinyGPSPlus gps;

void setup(){
    Serial.begin(9600);
    pinMode(sharpLEDPin, OUTPUT);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  
    Serial.print("Connecting");
    while (WiFi.status() != WL_CONNECTED) {
      Serial.print(" .");
      delay(500);
    }
    Firebase.begin(FIREBASE_HOST,FIREBASE_KEY);
    Serial.println("");
    Serial.println("Connected");
}
 
void loop(){
    digitalWrite(sharpLEDPin, LOW); // Turn on the dust sensor LED.
    delayMicroseconds(280); // Wait 0.28ms before taking a reading of the output voltage as per spec.
    VoRaw = analogRead(sharpVoPin); // Record the output voltage. This operation takes around 100 microseconds.
    digitalWrite(sharpLEDPin, HIGH); // Turn off the dust sensor LED.
    delayMicroseconds(9620); //Wait for remainder of the 10ms cycle = 10000 - 280 - 100 microseconds.
      
    // Averaging
    Vo = VoRaw;
    VoRawTotal += VoRaw;
    VoRawCount++;
    if(VoRawCount >= N){
      Vo = 1.0*VoRawTotal/N;
      VoRawCount = 0;
      VoRawTotal = 0;
    }else{
      return;
    }
  
    // Compute the output voltage in Volts.
    Vo = Vo/1024.0*5.0;
    Serial.print("Vo => ");
    Serial.print(Vo*1000.0);
    Serial.print(" mV");
  
    // Convert to Dust Density in units of ug/m3.
    dV = Vo-Voc;
    if(dV<0){
      dV = 0;
      Voc = Vo;
    }
    
    dustDensity = (dV/K)*100.0;
    
    Serial.print(", DustDensity => ");
    Serial.print(dustDensity);
    Serial.print("ug/m3");
    Serial.println("");
    Firebase.setFloat("pm25",dustDensity);
    
    Serial.print("Latitude => ");
    Serial.print(gps.location.lat(), 6);
    Serial.print(", Longitude => ");
    Serial.print(gps.location.lng(), 6);;
    Serial.println("");
    Firebase.setFloat("long",gps.location.lng());
    Firebase.setFloat("lat",gps.location.lat());
    delay(1000);
}
