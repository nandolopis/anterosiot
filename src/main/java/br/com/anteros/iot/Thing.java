package br.com.anteros.iot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.anteros.iot.domain.PlantItemNode;
import br.com.anteros.iot.triggers.Trigger;
import br.com.anteros.iot.triggers.TriggerType;

public interface Thing {

	public String getThingID(); // UUID

	public ThingStatus getStatus();

	public Set<Part> getParts();

	public boolean hasParts();

	public Thing addPart(Part part);

	public Thing removePart(Part part);

	public Part getPartById(String part);
	
	public Thing loadConfiguration(PlantItemNode node);
	
	public void setDeviceController(DeviceController controller);
	
	public DeviceController getDeviceController();
	
	public String[] getActions();
	
	public Thing addTrigger(Trigger trigger);
	
	public Thing removeTrigger(Trigger trigger);
	
	public Trigger[] getTriggers();

	default public boolean hasTriggers(TriggerType type, String action) {
		for (Trigger trigger : getTriggers()) {
			if (trigger.getType().equals(type) && trigger.getSourceAction().getAction().equals(action)) {
				return true;
			}
		}
		return false;
	}

	
	default public Trigger[] getTriggersByType(TriggerType type, String action) {
		List<Trigger> result = new ArrayList<>();
		for (Trigger trigger : getTriggers()) {
			if (trigger.getType().equals(type) && trigger.getSourceAction().getAction().equals(action)) {
				result.add(trigger);
			}
		}
		return result.toArray(new Trigger[] {});
	}

	
}
