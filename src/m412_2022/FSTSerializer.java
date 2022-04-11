package m412_2022;

import java.io.IOException;

import org.nustaq.serialization.FSTConfiguration;

public class FSTSerializer implements Serializer {
	static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	@Override
	public Message deserializeMessage(byte[] bytes) throws IOException {
		return (Message) conf.asObject(bytes);
	}

	@Override
	public byte[] serialize(Message msg) throws IOException {
		return conf.asByteArray(msg);
	}
}