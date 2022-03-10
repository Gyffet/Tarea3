import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MultMatriz {
    static int N = 8;
    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];
    
    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws Exception{
        while(longitud > 0)
        {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }
    
    static double[][] read_parte_matriz(int tam_matriz, DataInputStream entrada) throws Exception{
        double parte_matriz[][] = new double[tam_matriz/2][N];
        for(int i = 0; i < tam_matriz/2; i++){
            byte[] buffer = new byte[tam_matriz * 8];
            read(entrada, buffer, 0, tam_matriz * 8);
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            for(int j = 0; j < tam_matriz; j++){
                parte_matriz[i][j] = byte_buffer.getDouble();
            }                    
        }
        return parte_matriz;
    }



static void enviar_parte_matriz(double[][] matriz, int ini_i, int fin_i, DataOutputStream salida) throws IOException{
            for(int i = ini_i; i < fin_i; i++){
                
                ByteBuffer byte_buffer_matriz = ByteBuffer.allocate(N * 8);
                
                for(int j = 0; j < N; j++){
                    byte_buffer_matriz.putDouble(matriz[i][j]);
                }
                
                byte[] byte_matriz = byte_buffer_matriz.array();
                salida.write(byte_matriz);
            }
        }
    
    
    static class Worker extends Thread{
        int nodo;
        Worker(int nodo){
            this.nodo = nodo;
        }
                
        


        
        public void run(){
            Socket conexion = null;
            for(;;){
                try{
                    conexion = new Socket("localhost", 51000+nodo);
                    
                    DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());                    
                    
                    salida.writeInt(N);     //enviamos el tam de la matriz
                    
                    if(nodo == 1){ //enviamos la parte correspondiente de las matrices A y B al nodo
                        enviar_parte_matriz(A, 0, N/2, salida);
                        enviar_parte_matriz(B, 0, N/2, salida);
                        
                    }else if(nodo == 2){
                        enviar_parte_matriz(A, 0, N/2, salida);
                        enviar_parte_matriz(B, N / 2 , N, salida);
                        
                    }else if(nodo == 3){
                        enviar_parte_matriz(A, N / 2 , N, salida);
                        enviar_parte_matriz(B, 0, N/2, salida);   
                        
                    }
                    
                    conexion.close();
                    break;
                    
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    

    public static void main(String[] args) throws IOException, Exception {
        int nodo = Integer.parseInt(args[0]);
        
        if(nodo == 0){            
            for(int i = 0; i < N; i++) //Inicialización de matrices
                for(int j = 0; j < N; j++){
                    A[i][j] = i + 5 * j;
                    B[i][j] = 5 * i - j;
                    C[i][j] = 0;
                }          

		System.out.println("Matriz A:");
            for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(A[i][j] + " ");
            }
		System.out.println("");
        }
            System.out.println("");


            System.out.println("Matriz B:");
           for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(B[i][j] + " ");
            }
		System.out.println("");
        }

            System.out.println("");


            
            for(int i = 0; i < N; i++) //transposición de matriz:
                for(int j = 0; j < i; j++){
                    double x = B[i][j];
                    B[i][j] = B[j][i];
                    B[j][i] = x;
                }            
            
		System.out.println("Matriz B transpuesta:");
            for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.print(B[i][j] + " ");
            }
            System.out.println("");

        }

            System.out.println("");

            Worker w[] = new Worker[3];
            for(int i = 0; i < 3; i++){
                w[i] = new Worker(i+1);
                w[i].start();
            }
            for(int i = 0; i < 3; i++)
                w[i].join();
            
            
            double aux_A[][] = new double[N/2][N];
            double aux_B[][] = new double[N/2][N];
            double aux_C[][] = new double[N/2][N/2];
            int pos_i = 0;
            for(int i = N/2; i < N; i++){
                for(int j = 0; j < N; j++){
                    aux_A[pos_i][j] = A[i][j];
                    aux_B[pos_i][j] = B[i][j];                    
                }
                pos_i++;
            }

            for(int i = 0; i < N / 2; i++)
                        for(int j = 0; j < N / 2; j++)
                            for(int k = 0; k < N; k++)
                                aux_C[i][j] += aux_A[i][k] * aux_B[j][k];
           
            System.out.println("parte matriz C");
                    for(int i = 0; i < N / 2; i++){
                        for(int j = 0; j < N/2; j++){
                            System.out.print(aux_C[i][j] + " ");
                        }System.out.println("");
                    }
            
        }else{
            System.out.println("Nodo: " + nodo);
            ServerSocket servidor = new ServerSocket(51000 + nodo);            
            for(;;){    //espera la conexión del cliente
                Socket conexion = servidor.accept();    //aceptar la conexión del cliente
            
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                
                int tam_matriz = entrada.readInt();
                System.out.println("N :" + tam_matriz);
                
                double parte_A[][] = new double[tam_matriz/2][tam_matriz];
                double parte_B[][] = new double[tam_matriz/2][tam_matriz];
                double parte_C[][] = new double[tam_matriz/2][tam_matriz/2];
                
                for(int i = 0; i < tam_matriz / 2; i++)
                    for(int j = 0; j < tam_matriz / 2; j++)
                        parte_C[i][j] = 0;
                
                //lectura de la parte de la matriz A
                parte_A = read_parte_matriz(tam_matriz, entrada);                
                System.out.println("Parte matriz A");
                for(int i = 0; i < tam_matriz / 2; i++){
                    for(int j = 0; j < tam_matriz; j++){
                        System.out.print(parte_A[i][j] + " ");
                    }System.out.println("");
                }

                parte_B = read_parte_matriz(tam_matriz, entrada);
                System.out.println("parte matriz B");
                for(int i = 0; i < tam_matriz / 2; i++){
                    for(int j = 0; j < tam_matriz; j++){
                        System.out.print(parte_B[i][j] + " ");
                    }System.out.println("");
                }
                
                //calculando la parte de C
                for(int i = 0; i < tam_matriz / 2; i++)
                        for(int j = 0; j < tam_matriz / 2; j++)
                            for(int k = 0; k < tam_matriz; k++)
                                parte_C[i][j] += parte_A[i][k] * parte_B[j][k];
                
                System.out.println("parte matriz C");
                    for(int i = 0; i < tam_matriz / 2; i++){
                        for(int j = 0; j < tam_matriz/2; j++){
                            System.out.print(parte_C[i][j] + " ");
                        }System.out.println("");
                    }



            }
        }
    }
}