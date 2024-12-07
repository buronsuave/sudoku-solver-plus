package utils;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMasterServerInterface extends Remote {
    int getSudokuIndex() throws RemoteException;
    void register(String computeNode) throws RemoteException;
    void receiveResults(String results) throws RemoteException;
}
