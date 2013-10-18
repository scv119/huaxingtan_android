package cn.huaxingtan.player.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Json {
	private static final Gson naturalGson;
	private static final Gson objGson = new Gson();
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
		naturalGson = gsonBuilder.create();
	}
	
	public static Object loads(String s) {
		return naturalGson.fromJson(s, Object.class);
	}
	
	public static String dumps(Object o) {
		return objGson.toJson(o);
	}

}
