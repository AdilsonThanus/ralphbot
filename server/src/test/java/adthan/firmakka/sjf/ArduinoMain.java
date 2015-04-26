package adthan.firmakka.sjf;

import com.adthan.firmakka.Arduino;
import com.adthan.firmakka.sjf.Serial;
//import com.adthan.firmakka.sjfscala.Serial;
//import com.adthan.firmakka.sjf.Arduino;
//import com.adthan.firmakka.sjf.Serial;
//import com.adthan.firmakka.sjf.Serial;

public class ArduinoMain  {
    public static void main(String [] args) {
        com.adthan.firmakka.sjf.Arduino arduino = new com.adthan.firmakka.sjf.Arduino(Serial.list()[0], 57600);
        //arduino.pinMode(3, Arduino.ANALOG);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println(arduino.analogRead(3));
        System.err.println(arduino.analogRead(3));
    //  System.err.println(arduino.analogRead(3));
     //   System.err.println(arduino.analogRead(3));
        arduino.pinMode(21, 4); //servo
        arduino.servoWrite(21, 120);
    }
}
