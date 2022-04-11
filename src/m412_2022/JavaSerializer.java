package m412_2022;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerializer implements Serializer {

	@Override
	public Message deserializeMessage(byte[] bytes) throws IOException {
		var bis = new ByteArrayInputStream(bytes);
		var ois = new ObjectInputStream(bis);

		try {
			var m = (Message) ois.readObject();
			ois.close();
			return m;
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte[] serialize(Message msg) throws IOException {
		var bos = new ByteArrayOutputStream();
		var oos = new ObjectOutputStream(bos);
		oos.writeObject(msg);
		return bos.toByteArray();
	}
}