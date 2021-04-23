package m412_2020;

import java.util.Set;

public class PeerListMessage extends Message<Set<Peer>>
{
	private static final long serialVersionUID = 1L;

	public PeerListMessage(Set<Peer> peers)
	{
		super(peers);
	}

	@Override
	public String toString()
	{
		return "*** Received " + getContent().size() + " peer(s) from " + sender();
	}
}
