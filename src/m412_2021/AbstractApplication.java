package m412_2021;

public abstract class AbstractApplication {
	// this provides the networking functionality
	final Network server;

	public AbstractApplication(Network server) {
		this.server = server;
		this.server.client = this;
	}

	public abstract void newMessage(PeerInfo from, Object o);
}
