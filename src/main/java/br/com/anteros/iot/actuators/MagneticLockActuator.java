package br.com.anteros.iot.actuators;

import java.util.HashMap;
import java.util.Map;

import com.diozero.util.SleepUtil;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

import br.com.anteros.iot.Actuator;
import br.com.anteros.iot.Thing;
import br.com.anteros.iot.support.Pi4JHelper;
import br.com.anteros.iot.things.MagneticLock;
import br.com.anteros.iot.triggers.TriggerType;

public class MagneticLockActuator implements Actuator<Boolean> {

	public static final String OPEN = "open";

	protected Map<Thing, GpioPinDigitalOutput> pins = new HashMap<>();

	@Override
	public boolean isSupportedThing(Thing thing) {
		return thing instanceof MagneticLock;
	}

	@Override
	public Boolean executeAction(String action, Thing thing) {
		if (thing instanceof MagneticLock) {
			if (action.equals(OPEN)) {
				GpioController gpio = Pi4JHelper.getGpioController();
				final GpioPinDigitalOutput pin = getOutputPinFromThing(gpio, thing);
				gpio.low(pin);
				SleepUtil.sleepMillis(((MagneticLock)thing).getTimeWaitOpening());
				gpio.high(pin);
				return true;
			}
		}
		return false;
	}

	protected GpioPinDigitalOutput getOutputPinFromThing(GpioController gpio, Thing thing) {
		if (!pins.containsKey(thing)) {
			int pinNumber = ((MagneticLock) thing).getPin();
			GpioPinDigitalOutput result = Pi4JHelper.getDigitalOutputPin(gpio, pinNumber, "MAGLOCK");
			pins.put(thing, result);
		}
		return pins.get(thing);
	}

	@Override
	public String toString() {
		return "MagneticLockActuator [pins=" + pins + "]";
	}

	@Override
	public void fireTriggers(TriggerType type, String action, Thing thing) {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
