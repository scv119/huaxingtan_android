package cn.huaxingtan.model;

import java.util.Map;

public class Serial {
	private int id;
	private String name;
	private long duration;
	private int quantity;
	private String coverUrl;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getCoverUrl() {
		return coverUrl;
	}
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	@SuppressWarnings("rawtypes")
	public static Serial loadJson(Object o) {
		assert(o instanceof Map);
		Map map = (Map)o;
		Serial item = new Serial();
		item.name = (String) map.get("name");
		item.coverUrl = (String) map.get("coverUrl");
		double tmp = (Double) map.get("duration");
		item.duration = (long) tmp;
		tmp = (Double) map.get("quantity");
		item.quantity = (int)tmp;
		tmp = (Double) map.get("id");
		item.id = (int) tmp;
		return item;
	}
	
}
