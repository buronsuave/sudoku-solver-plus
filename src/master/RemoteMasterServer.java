package master;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import utils.RemoteMasterServerInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteMasterServer extends UnicastRemoteObject implements RemoteMasterServerInterface {
    private final MasterApp context;

    public RemoteMasterServer(MasterApp context) throws RemoteException {
        this.context = context;
    }

    public int getSudokuIndex() throws RemoteException {
        // -1 if stack is empty
        // pop action is synchronized
        int index = context.stackPop();
        System.out.println("[RemoteMasterServer] Delivering " + index);
        return index;
    }

    public void register(String computeNode) throws RemoteException {
        context.addComputeNode(computeNode);
    }

    public void receiveResults(String results) throws RemoteException {
        System.out.println("[RemoteMasterServer] Results from compute node: " + results);
        JsonArray resultsArray = JsonParser.parseString(results).getAsJsonArray();
        if (resultsArray.isEmpty()) return;
        if (resultsArray.get(0).getAsJsonObject().get("main").getAsString().equals("parallel")) {
            context.getParallelResults().add(resultsArray.get(0).getAsJsonObject().get("node").getAsString(),
                    resultsArray);
        } else if (resultsArray.get(0).getAsJsonObject().get("main").getAsString().equals("sequential")) {
            context.getResults().add("sequential", resultsArray);
        } else {
            context.getResults().add("concurrent", resultsArray);
        }
    }

    public void listen() {
        try {
            // 3000 port for serving the stack
            LocateRegistry.createRegistry(3000);

            System.setProperty("java.rmi.server.hostname", context.getHostname());
            RemoteMasterServerInterface remote = new RemoteMasterServer(context);
            System.out.println("[RemoteMasterServer] Listening in //" + context.getHostname() + ":3000/Master");
            java.rmi.Naming.rebind("//" + context.getHostname() + ":3000/Master", remote);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
