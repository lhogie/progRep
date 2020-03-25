package m412;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils
{
	public static void printIPs() throws SocketException
	{
		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
				.getNetworkInterfaces();

		while (networkInterfaces.hasMoreElements())
		{
			NetworkInterface nic = networkInterfaces.nextElement();
			Enumeration<InetAddress> ips = nic.getInetAddresses();

			while (ips.hasMoreElements())
			{
				InetAddress ip = ips.nextElement();

				if (ip instanceof Inet4Address && ! nic.isVirtual() && ! nic.isLoopback())
					System.out.println(ip + " applies to network interface: "
							+ nic.getDisplayName());
			}
		}
	}

	public static void sleep(int seconds)
	{
		try
		{
			Thread.sleep(seconds * 1000);
		}
		catch (InterruptedException e)
		{
			throw new IllegalStateException();
		}
	}
}
