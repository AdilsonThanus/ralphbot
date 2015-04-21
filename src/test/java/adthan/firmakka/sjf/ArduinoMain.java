package adthan.firmakka.sjf;

public class ArduinoMain  {
    public static void main(String [] args) {
        Arduino arduino = new Arduino(Serial.list()[0], 57600);
        arduino.pinMode(3, Arduino.ANALOG);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println(arduino.analogRead(3));
        System.err.println(arduino.analogRead(3));
        System.err.println(arduino.analogRead(3));
        System.err.println(arduino.analogRead(3));
        arduino.pinMode(2, Arduino.SERVO);
        arduino.servoWrite(2, 120);
    }
}
