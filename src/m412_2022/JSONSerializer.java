package m412_2022;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONSerializer implements Serializer {
	static final Gson gson;

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		gson = builder.create();
	}

	@Override
	public Message deserializeMessage(byte[] bytes) throws IOException {
		String received = new String(bytes);

		// deserialize the UDP packet to a Message object
		return gson.fromJson(received, Message.class);
	}

	@Override
	public byte[] serialize(Message msg) throws IOException {
		return gson.toJson(msg).getBytes();
	}
}