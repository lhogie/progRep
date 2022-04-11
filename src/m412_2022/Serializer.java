package m412_2022;

import java.io.IOException;

public interface Serializer {
	Message deserializeMessage(byte[] bytes) throws IOException;

	byte[] serialize(Message msg) throws IOException;
}