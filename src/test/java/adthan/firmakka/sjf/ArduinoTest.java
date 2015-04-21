package adthan.firmakka.sjf;

import org.junit.Test;

public class ArduinoTest {

	@Test
	public void test() {
		Arduino arduino = new Arduino(Serial.list()[0], 57600);
		arduino.pinMode(3, Arduino.ANALOG);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.err.println(arduino.analogRead(3));
		arduino.pinMode(2, Arduino.SERVO);
		arduino.servoWrite(2, 120);
	}

}
