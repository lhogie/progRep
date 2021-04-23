package m412_2021;
public abstract class AbstractApplication {

	final NetworkAdapter server;

	public AbstractApplication(NetworkAdapter server) {
		this.server = server;
		this.server.client = this;
	}

	
	public abstract void process(PeerInfo from, Object o);
}
