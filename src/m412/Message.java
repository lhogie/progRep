package m412;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	public long ID = new Random().nextLong();
	private Object content;
	public final List<String> senders = new ArrayList<>();

	public Message(Object content)
	{
		this.content = content;
	}

	public Object getContent()
	{
		return content;
	}

	@Override
	public String toString()
	{
		return senders + "> " + content;
	}

	public void makeSureImDeclaredAsTheSender(String username)
	{
		if (senders.isEmpty() || ! senders.get(senders.size() - 1).equals(username))
			senders.add(username);
	}

	public byte[] toByteArray()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try
		{
			// generates a stream of bytes out of an object
			ObjectOutputStream oos = new ObjectOutputStream(bos);

			// do the conversion
			oos.writeObject(this);
			oos.close();

			// obtain the byte array that was written out
			return bos.toByteArray();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}

	}

	public static Message fromBytes(byte[] buf) throws IOException, ClassNotFoundException
	{
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf));
		Message msg = (Message) in.readObject();
		in.close();
		return msg;
	}

}
