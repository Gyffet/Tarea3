import java.rmi.Naming;

public class ServidorRMIP5 {
    public static void main(String[] args) throws Exception {
        String url = "rmi://localhost/pruebaPractica5";
        ClaseRMIP5 obj = new ClaseRMIP5();
        
        //registra una instancia en el rmiregistry
        Naming.rebind(url, obj);
    }
}
