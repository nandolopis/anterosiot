package br.com.anteros.iot;

import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttCallback;

public interface SlaveDeviceController extends DeviceController, MqttCallback, Runnable {
	
	public MasterDeviceController getMaster();
	
	public void setMaster(MasterDeviceController master);
	
	public Set<DeviceController> controllers();

}
