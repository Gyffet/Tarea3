import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceRMIP5 extends Remote{
    public float[][] multiplica_matrices(float[][] A, float[][] B, int N) throws RemoteException;
}
