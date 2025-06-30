#include <WiFi.h>
#include "FirebaseESP32.h"
#include <PZEM004Tv30.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>

// ==== BLE UUID ==== 
#define SERVICE_UUID        "12345678-1234-1234-1234-1234567890ab"
#define CHARACTERISTIC_UUID "abcd1234-5678-90ab-cdef-1234567890ab"

// ==== Firebase ==== 
#define FIREBASE_HOST "https://cayvl-13f88-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "PIGuIkpwAWyYCwuMVQe2Ls83SIf57JvTqxX6t1rj"

// ==== UART2 cho PZEM   ==== ////////////////////
#define RXD2 16
#define TXD2 17
HardwareSerial pzemSerial(2);
PZEM004Tv30 pzem(pzemSerial, RXD2, TXD2);

// ==== Firebase objects ==== 
FirebaseData fbdo;
FirebaseConfig config;
FirebaseAuth auth;

// ==== WiFi credentials ==== 
String wifiSSID = "";
String wifiPASS = "";

bool wifiConnected = false;

// ==== Tách SSID/PASS từ BLE ==== 
void parseWiFiData(String data) {
  int ssidStart = data.indexOf("ssid:") + 5;
  int passStart = data.indexOf("pass:");

  if (ssidStart >= 5 && passStart != -1) {
    wifiSSID = data.substring(ssidStart, data.indexOf(";", ssidStart));
    wifiPASS = data.substring(passStart + 5);

    Serial.println(" Đã nhận:");
    Serial.println(" SSID: " + wifiSSID);
    Serial.println(" PASS: " + wifiPASS);

    WiFi.begin(wifiSSID.c_str(), wifiPASS.c_str());
    Serial.print(" Đang kết nối WiFi");
    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - start < 10000) {
      Serial.print(".");
      delay(500);
    }

    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("\n Kết nối WiFi thành công! IP: " + WiFi.localIP().toString());
      wifiConnected = true;

      // Khởi động Firebase
      config.host = FIREBASE_HOST;
      config.signer.tokens.legacy_token = FIREBASE_AUTH;
      Firebase.begin(&config, &auth);
      Firebase.reconnectWiFi(true);
      Serial.println(" Firebase đã sẵn sàng");
    } else {
      Serial.println("\n Kết nối WiFi thất bại!");
    }

  } else {
    Serial.println(" Dữ liệu sai định dạng! (Cần: ssid:...;pass:...)");
  }
}

class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String received = String((char*)pCharacteristic->getData());
    Serial.println(" Dữ liệu BLE nhận được:");
    Serial.println(received);
    parseWiFiData(received);
  }
};

void setup() {
  Serial.begin(115200);

  // UART cho PZEM////////////////////////////
  pzemSerial.begin(9600, SERIAL_8N1, RXD2, TXD2);
  Serial.println(" Khởi tạo UART cho PZEM-004T...");

  // BLE setup
  BLEDevice::init("ESP32-CONFIG");
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_WRITE
  );
  pCharacteristic->setCallbacks(new MyCallbacks());
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->start();

  Serial.println(" ESP32 đang phát BLE: ESP32-CONFIG");
}

void loop() {
  if (wifiConnected && Firebase.ready()) {
    // Đọc dữ liệu PZEM
    float voltage = pzem.voltage();
    float current = pzem.current();
    float power = pzem.power();
    float energy = pzem.energy();
    float frequency = pzem.frequency();
    float pf = pzem.pf();

    Serial.println("\n===== DỮ LIỆU CẢM BIẾN =====");
    Serial.printf("Điện áp (V): %.2f\n", voltage);
    Serial.printf("Dòng điện (A): %.2f\n", current);
    Serial.printf("Công suất (W): %.2f\n", power);
    Serial.printf("Điện năng (Wh): %.2f\n", energy);
    Serial.printf("Tần số (Hz): %.2f\n", frequency);
    Serial.printf("Hệ số PF: %.2f\n", pf);

    if (!isnan(voltage) && !isnan(current)) {
      bool success =
        Firebase.setFloat(fbdo, "/Phong_khach/dienap", voltage) &&
        Firebase.setFloat(fbdo, "/Phong_khach/dongdien", current) &&
        Firebase.setFloat(fbdo, "/Phong_khach/congsuat", power) &&
        Firebase.setFloat(fbdo, "/Phong_khach/diennang", energy) &&
        Firebase.setFloat(fbdo, "/Phong_khach/tanso", frequency) &&
        Firebase.setFloat(fbdo, "/Phong_khach/pf", pf);

      if (success) {
        Serial.println(" Gửi dữ liệu thành công lên Firebase");
      } else {
        Serial.print(" Lỗi Firebase: ");
        Serial.println(fbdo.errorReason());
      }
    }

    delay(5000);
  } else {
    delay(1000); // Chờ kết nối WiFi xong mới gửi
  }
}
