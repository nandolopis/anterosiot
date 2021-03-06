package br.com.anteros.iot.collectors;

import javax.json.JsonObjectBuilder;

public class SimpleResult implements CollectResult {
	
	protected Object value;
	
	public SimpleResult(Object value) {
		super();
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public JsonObjectBuilder toJson(JsonObjectBuilder builder) {
		builder.add("value",value+"");
		return builder;
	}

}
