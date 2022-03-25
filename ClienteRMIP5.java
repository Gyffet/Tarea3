import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteRMIP5 {
    static int N = 8;
    static float[][] A = new float[N][N];
    static float[][] B = new float[N][N];
    static float[][] C = new float[N][N];
    
    static float[][] A1, A2, A3, A4, B1, B2, B3, B4;
    
    static void inicializa_matrices(float[][] A, float[][] B, float[][] C, int N){
        for(int i = 0; i < N; i++)
            for(int j = 0; j < N; j++){
                A[i][j] = i + 2 * j;
                B[i][j] = 3 * i - j;
                C[i][j] = 0;
            }     
    }
    
    static void transpuesta(float[][] B, int N){
        for(int i = 0; i < N; i++)
            for(int j = 0; j < i; j++){
                float x = B[i][j];
                B[i][j] = B[j][i];
                B[j][i] = x;
            }
    }
    
    static void imprime_matriz(float[][] A, int N){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                System.out.print(A[i][j] + " ");
            }System.out.println("");
        }
    }
    
    static float[][] separa_matriz(float[][] A, int inicio, int N){
        float[][] M = new float[N/4][N];
        for(int i = 0; i < N / 4; i++)
            for(int j = 0; j < N; j++)
                M[i][j] = A[i + inicio][j];
        return M;
    }        
            
    static void acomoda_matriz(float[][] C, float[][] A, int renglon, int columna, int N){
        for(int i = 0; i < N / 4; i++)
            for(int j = 0; j < N / 4; j++)
                C[i + renglon][j + columna] = A[i][j];
    }    
    
    static float checksum(float[][] C, int N){
        float check = 0;
        for(int i = 0; i < N; i++)
            for(int j = 0; j < N; j++)
                check += C[i][j];
            
        return check;
    }
    
    static class Worker extends Thread {
        int nodo;
        InterfaceRMIP5 interfaceNodo;
        Worker(int nodo, InterfaceRMIP5 interfaceNodo){
            this.nodo = nodo;
            this.interfaceNodo = interfaceNodo;

        }
        
        public void run() {
            try {
                //String url = "rmi://localhost/pruebaPractica5";
                //InterfaceRMIP5 interf = (InterfaceRMIP5)Naming.lookup(url);
                
                if(nodo == 1){
                    float C1[][] = interfaceNodo.multiplica_matrices(A1, B1, N);
                    float C2[][] = interfaceNodo.multiplica_matrices(A1, B2, N);
                    float C3[][] = interfaceNodo.multiplica_matrices(A1, B3, N);
                    float C4[][] = interfaceNodo.multiplica_matrices(A1, B4, N);
                    acomoda_matriz(C, C1, 0, 0, N); //matriz original, cual pasamos, renglon, col, N
                    acomoda_matriz(C, C2, 0, N/4, N);
                    acomoda_matriz(C, C3, 0, N/2, N);
                    acomoda_matriz(C, C4, 0, 3*N/4, N);
                    
                }else if(nodo == 2){
                    float C5[][] = interfaceNodo.multiplica_matrices(A2, B1, N);
                    float C6[][] = interfaceNodo.multiplica_matrices(A2, B2, N);
                    float C7[][] = interfaceNodo.multiplica_matrices(A2, B3, N);
                    float C8[][] = interfaceNodo.multiplica_matrices(A2, B4, N);
                    acomoda_matriz(C, C5, N/4, 0, N); //matriz original, cual pasamos, renglon, col, N
                    acomoda_matriz(C, C6, N/4, N/4, N);
                    acomoda_matriz(C, C7, N/4, N/2, N);
                    acomoda_matriz(C, C8, N/4, 3*N/4, N);
                    
                }else if(nodo == 3){
                    float C9[][] = interfaceNodo.multiplica_matrices(A3, B1, N);
                    float C10[][] = interfaceNodo.multiplica_matrices(A3, B2, N);
                    float C11[][] = interfaceNodo.multiplica_matrices(A3, B3, N);
                    float C12[][] = interfaceNodo.multiplica_matrices(A3, B4, N);
                    acomoda_matriz(C, C9, N/2, 0, N); //matriz original, cual pasamos, renglon, col, N
                    acomoda_matriz(C, C10, N/2, N/4, N);
                    acomoda_matriz(C, C11, N/2, N/2, N);
                    acomoda_matriz(C, C12, N/2, 3*N/4, N);
                    
                }else if(nodo == 4){
                    float C13[][] = interfaceNodo.multiplica_matrices(A4, B1, N);
                    float C14[][] = interfaceNodo.multiplica_matrices(A4, B2, N);
                    float C15[][] = interfaceNodo.multiplica_matrices(A4, B3, N);
                    float C16[][] = interfaceNodo.multiplica_matrices(A4, B4, N);
                    acomoda_matriz(C, C13, 3*N/4, 0, N); //matriz original, cual pasamos, renglon, col, N
                    acomoda_matriz(C, C14, 3*N/4, N/4, N);
                    acomoda_matriz(C, C15, 3*N/4, N/2, N);
                    acomoda_matriz(C, C16, 3*N/4, 3*N/4, N);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(ClienteRMIP5.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        int nodo = 0;
        
        if(nodo == 0){ //Cliente
            inicializa_matrices(A, B, C, N);
            
            if(N == 8){
                System.out.println("Matriz A:");
                imprime_matriz(A,N);
                System.out.println("Matriz B");
                imprime_matriz(B,N);
            }            
            
            transpuesta(B, N);
            
            A1 = separa_matriz(A, 0, N);
            A2 = separa_matriz(A, N/4, N);
            A3 = separa_matriz(A, N/2, N);
            A4 = separa_matriz(A, 3*N/4, N);
            
            B1 = separa_matriz(B, 0, N);
            B2 = separa_matriz(B, N/4, N);
            B3 = separa_matriz(B, N/2, N);
            B4 = separa_matriz(B, 3*N/4, N);
            
            String url1 = "rmi://13.66.2.11/Tarea5";    // aqui van las IP privadas de cada nodo
            String url2 = "rmi://104.215.81.19/Tarea5";    // en lugar de localhost            
            String url3 = "rmi://13.84.214.206/Tarea5";
            String url4 = "rmi://20.114.73.30/Tarea5/Tarea5";

            InterfaceRMIP5 nodo1 = (InterfaceRMIP5)Naming.lookup(url1);
            
            System.out.println("URL1 Conectada");
            
            InterfaceRMIP5 nodo2 = (InterfaceRMIP5)Naming.lookup(url2);
            
            System.out.println("URL2 Conectada");

            InterfaceRMIP5 nodo3 = (InterfaceRMIP5)Naming.lookup(url3);
            
            System.out.println("URL3 Conectada");
            
            InterfaceRMIP5 nodo4 = (InterfaceRMIP5)Naming.lookup(url4);
            
            System.out.println("URL4 Conectada");

            Worker w[] = new Worker[4];   //instancia de 4 hilos (para conectarnos a c/u de los server)
            
            w[0] = new Worker(1, nodo1);
            w[1] = new Worker(2, nodo2);
            w[2] = new Worker(3, nodo3);
            w[3] = new Worker(4, nodo4);

            for(int i = 0; i < 4; i++){
                //w[i] = new Worker(i+1);
                w[i].start();   //los iniciamos
            }
            
            for(int i = 0; i < 3; i++)
                w[i].join();    //esperamos a que terminen los 3 hilos            
            
            if(N == 8){
                System.out.println("Matriz C:");
                imprime_matriz(C, N);
            }
            
            float checksum = checksum(C, N);
            System.out.println("Checksum: " + checksum);
        }
    }
}
