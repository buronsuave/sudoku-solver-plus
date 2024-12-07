package utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkHelper {
    // For dynamic assignment of Remote Server hostname (IP)
    public static String getIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface anInterface = interfaces.nextElement();
                if (anInterface.isLoopback() || !anInterface.isUp()
                        || anInterface.isVirtual() || anInterface.isPointToPoint())
                    continue;

                Enumeration<InetAddress> addresses = anInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    final String ip = address.getHostAddress();
                    if(Inet4Address.class == address.getClass() && isValidIp(ip)) return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    // Verify if IP is in recognized segment
    public static boolean isValidIp(String ip) {
        String[] IPs = {"192.168.0", "192.168.1", "192.168.84"};
        for (String IP : IPs) {
            if (ip.contains(IP)) return true;
        }
        return false;
    }
}
