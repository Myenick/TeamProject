/*
   Portable Multitasking Measuring Station
   By: Dawid Mienik
   If you have any doubts on program working, check info folder included in files.
*/

#include <SoftwareSerial.h>
#include<stdio.h>
#include<string.h>
#include <DHT.h>                //Warrning for DHT11 use 1.2.3 version instead 1.3.0 cause its bugged! 
#include <SD.h>


#define DHTPIN 3                //definition of the signal pin number
#define DHTTYPE DHT11           //definition of type of the sensor
#define DEBUG true              //definition for GPS sensor's function sendData()
//#define ARRAY_SIZE( array ) ( sizeof( array ) / sizeof( array[0] ))         //to define size of array in c

SoftwareSerial mySerial(7, 8);  //software serial instance for GPS/GSM module
DHT dht(DHTPIN, DHTTYPE);       //instance that defines sensor


const int buttonPin = 2;
int buttonState = 1;
int lastButtonState = 1;
int buttonPushCounter = 1;
int ledPinI = 5;
int ledPinII = 6;
int photoResistorPin = 0;
String latitude, longitude, state, timegps;
long id = 0;                    //id for saving data in a proper order in SD


void setup() {


  mySerial.begin(9600);
  Serial.begin(9600);                  //open serial port
  delay(10);

  pinMode(ledPinI, OUTPUT);
  pinMode(ledPinII, OUTPUT);

  pinMode(buttonPin, INPUT_PULLUP);     //initialize button pin with INPUT_PULLUP mode
  delay(10);

  dht.begin();                          //initialize DHT11 sensor
  delay(50);

  digitalWrite(ledPinI, HIGH);
  digitalWrite(ledPinII, HIGH);
  delay(500);
  digitalWrite(ledPinI, LOW);
  digitalWrite(ledPinII, LOW);
  delay(500);
  digitalWrite(ledPinI, HIGH);
  digitalWrite(ledPinII, HIGH);
  delay(500);
  digitalWrite(ledPinI, LOW);
  digitalWrite(ledPinII, LOW);
  delay(500);

  initializeSD();
  delay(10);
}


void loop() {

  float temp, hum;                       //temperature and humidity variables
  int lux;

  if (Serial.available() > 0)
    mySerial.write(Serial.read());
  if (mySerial.available() > 0)
    Serial.write(mySerial.read());

  checkButton();                         //check button status function

  if (buttonPushCounter % 2 == 0) {
    getGps();                        //initialize gps module function
    delay(50);

    while (1) {
      if (buttonPushCounter % 2 == 0) {
        sendTabData("AT+CGNSINF", 1000, DEBUG);  //send demand of gps localization

        temp = dht.readTemperature();            //read temperature
        hum = dht.readHumidity();                //read humidity
        checkDHTData(temp, hum);
        lux = analogRead(photoResistorPin);      //read voltage of photoresistor, need to do calculation to LUX !!!
        Serial.println(" ");
        Serial.println("Time: " + timegps + " Latitude: " + latitude + " Longitude: " + longitude + "\nHumidity: " + hum +
                       " Temperature: " + temp + " Lux: " + lux + " GPS Signal: " + state + "\n");

        if (latitude.length() > 0 && longitude.length() > 0 &&  hum > 0 && temp > 0 && !(state.equals("0") & lux > 0)) { //need to parse date

          saveToSD(timegps, latitude, longitude, String(hum), String(temp), String(lux));

          digitalWrite(ledPinII, HIGH);
          delay(1000);
          digitalWrite(ledPinII, LOW);

        } else {

          Serial.println("Error");
          digitalWrite(ledPinI, HIGH);
          delay(1000);
          digitalWrite(ledPinI, LOW);
          delay(100);
        }
        checkButton();                    //check button state
        delay(50);
      } else {

        digitalWrite(ledPinI, HIGH);
        digitalWrite(ledPinII, HIGH);
        delay(2000);
        digitalWrite(ledPinI, LOW);
        digitalWrite(ledPinII, LOW);

        mySerial.println("AT+CGNSPWR=0");   //turn off gps sensor
        break;
      }
    }

  } else {
    Serial.println();
    Serial.println("Push Button to turn on device");
    delay(1000);
  }
}

/*this function is responsible of checking button state*/
bool checkButton() {

  buttonState = digitalRead(buttonPin);         //get info about button state

  if (buttonState != lastButtonState) {
    if (buttonState == HIGH) {
      buttonPushCounter++;                      //if button pressed increment
      Serial.println("On");
    } else {
      Serial.println("Off");
    }
    delay(10);
  }
  lastButtonState = buttonState;                //switch states
}

/*this function sets SIM808 module in proper mode to retrieve GPS data*/
void getGps()
{
  delay(50);
  sendData("AT+CGNSPWR=1", 1000, DEBUG);     //Initialize GPS device
  delay(50);
  sendData("AT+CGNSSEQ=RMC", 1000, DEBUG);
  delay(150);

  while (1) {
    if (!(state.equals("1"))) {
      delay(300);
      sendTabData("AT+CGNSINF", 1000, DEBUG);
      Serial.println("Initializing GPS ... please wait");

      digitalWrite(ledPinI, HIGH);
      delay(1000);
      digitalWrite(ledPinI, LOW);
      digitalWrite(ledPinII, HIGH);
      delay(1000);
      digitalWrite(ledPinII, LOW);


    } else {

      timegps = "";
      latitude = "";
      longitude = "";

      digitalWrite(ledPinI, HIGH);
      digitalWrite(ledPinII, HIGH);
      delay(2000);
      digitalWrite(ledPinI, LOW);
      digitalWrite(ledPinII, LOW);

      delay(100);
      break;
    }
  }
}

/*this function reads data from the board and sends it*/
String sendData(String command, const int timeout, boolean debug) {

  String response = "";
  mySerial.println(command);
  long int time = millis();
  int i = 0;

  while ( (time + timeout) > millis()) {
    while (mySerial.available()) {
      char c = mySerial.read();
      response += c;
    }
  }
  if (debug) {
    Serial.print(response);
  }
  return response;
}

/*this function is responsible for fetching the data from gps string*/
bool sendTabData(String command, const int timeout, boolean debug) {

  String latlongtab[5];
  mySerial.println(command);
  long int time = millis();
  int i = 0;

  while ( (time + timeout) > millis()) {
    while (mySerial.available()) {
      char c = mySerial.read();
      if (c != ',') {             //read characters until you find comma, if found increment
        latlongtab[i] += c;
        delay(500);
      } else {
        i++;
      }
      if (i == 5) {
        delay(100);
        goto exitL;
      }
    }
} exitL:
  if (debug) {
    state = latlongtab[1];        //state = recieving data - 1, not recieving - 0
    timegps = latlongtab[2];
    latitude = latlongtab[3];     //latitude
    longitude = latlongtab[4];    //longitude
  }
  return true;
}

/*
    this function checks condition, if there is a proper data retrieved
    from DHT11 sensor and if there is some data,
    it prints it to serial
*/
void checkDHTData(float temp, float hum) {

  if (isnan(temp) || isnan(hum)) {
    Serial.println("Error in reading data from the sensor!!");
  } else {
    /*  We are printing in upper part of program
        Serial.print("Humidity: ");
      Serial.print(hum);
      Serial.print(" % ");
      Serial.print("Temperature: ");
      Serial.print(temp);
      Serial.println(" *C");
    */
  }
}

/*this function is responsible for formatting proper header into CSV file*/
void initializeSD() {

  if (!SD.begin(4)) {                                                   //initialize Card
    Serial.println("Card Failure");
    return;
  }
  Serial.println("Card Ready");

  File logFile = SD.open("LOG.csv", FILE_WRITE);

  if (logFile) {
    logFile.println(", , , , , , ,");                                               //just a leading blank line, in case there was previous data
    String header = "ID, Time, Latitude, Longitude, Humidity, Temperature, Lux";       //add header
    logFile.println(header);
    logFile.close();
    Serial.println(header);
  }
  else {
    Serial.println("Couldn't open log file");
  }
}

/*this function is responsible for saving data in proper format to CSV file on SD card*/
void saveToSD(String timegps, String lati, String longi, String humi, String temp, String lux) {

  String dataString = String(id) + ", " + timegps + ", " + lati + ", " + longi + ", " + humi + ", " + temp + ", " + lux;
  File logFile = SD.open("LOG.csv", FILE_WRITE);

  if (logFile) {
    logFile.println(dataString);
    logFile.close();
    Serial.println("Saved: " + dataString);
  }
  else {
    Serial.println("Couldn't open log file");
  }

  //Increment ID number
  id++;

  delay(50);
}



