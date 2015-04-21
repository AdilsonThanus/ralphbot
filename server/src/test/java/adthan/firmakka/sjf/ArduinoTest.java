package adthan.firmakka.sjf;

import com.adthan.firmakka.sjf.Arduino;
import com.adthan.firmakka.sjf.Serial;
import org.junit.Test;

public class ArduinoTest {

	@Test
	public void test() {
		Arduino arduino = new Arduino(Serial.list()[0], 57600);
		//arduino.pinMode(3, Arduino.ANALOG);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.err.println(arduino.analogRead(3));
		arduino.pinMode(22, 4); //SERVO
		arduino.servoWrite(22, 90);
	}

}
