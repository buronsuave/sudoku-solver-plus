package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteComputeServerInterface extends Remote {
    void start(String config) throws RemoteException;
}
