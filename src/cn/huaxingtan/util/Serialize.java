package cn.huaxingtan.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.util.Base64;

public class Serialize {
	public static byte[] serialize(Serializable x) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        ObjectOutputStream oos = new ObjectOutputStream(bos);  
        oos.writeObject(x);  
        return bos.toByteArray();
	}
	
	public static Object deserialize(byte[] bytes) throws StreamCorruptedException, IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);  
        ObjectInputStream ois = new ObjectInputStream(bis);  
        return ois.readObject();  
	}
	
	public static String toBase64(Serializable x) throws IOException {
		return Base64.encodeToString(serialize(x), 0);
	}
	
	public static Object fromBase64(String x) throws StreamCorruptedException, IOException, ClassNotFoundException {
		return deserialize(Base64.decode(x, 0));
	}
}
