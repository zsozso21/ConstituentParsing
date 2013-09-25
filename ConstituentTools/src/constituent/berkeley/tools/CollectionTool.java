package constituent.berkeley.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionTool {
	public static <T> void incValueInMap(Map<T, Integer> map, T key){
		if (map.containsKey(key)){
			map.put(key, map.get(key) + 1);
		} else {
			map.put(key, 1);
		}
	}
	
	public static <K, V> void WriteMap(Map<K, V> map, OutputStream stream) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
		for (Entry<K, V> ent : map.entrySet()) {
			writer.write(ent.getKey() + "\t" + ent.getValue() + "\n");
		}
		writer.flush();
	}
}
