package compute;

import utils.NetworkHelper;
import utils.RemoteMasterServerInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class ComputeApp {
    public static final String MASTER_IP = "192.168.1.102";
    public static final String MASTER_SERVER_PORT = "3000";

    private final RemoteComputeServer remoteComputeServer;
    private final String hostname;

    public ComputeApp() {
        try {
            remoteComputeServer = new RemoteComputeServer(this);
            hostname = NetworkHelper.getIP();
            register();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void register() {
        try {
            RemoteMasterServerInterface remote = (RemoteMasterServerInterface)
                    Naming.lookup("//" + MASTER_IP + ":" + MASTER_SERVER_PORT + "/Master");
            remote.register(hostname);
        } catch (Exception e) {
            System.out.println("[ComputeApp] " + e.getMessage());
        }
    }

    public void listen() {
        remoteComputeServer.listen();
    }

    public String getHostname() {
        return hostname;
    }

    public static void main(String[] args) {
        ComputeApp app = new ComputeApp();
        Thread serverThread = new Thread(app::listen);
        serverThread.start();

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("[ComputeApp] App interrupted: " + e.getMessage());
        }
    }
}
