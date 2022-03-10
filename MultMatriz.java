
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MultMatriz {

    static int N = 4;
    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];
    static String IP;

    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws Exception {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    static double[][] read_parte_matriz(int tam_matriz_i, int tam_matriz_j, DataInputStream entrada) throws Exception {
        double parte_matriz[][] = new double[tam_matriz_i][tam_matriz_j];

        for (int i = 0; i < tam_matriz_i; i++) {
            byte[] buffer = new byte[tam_matriz_j * 8];   //almacenamos espacio para N elementos (renglon)
            read(entrada, buffer, 0, tam_matriz_j * 8);   //leemos o recibimos el renglon 
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            for (int j = 0; j < tam_matriz_j; j++) {
                parte_matriz[i][j] = byte_buffer.getDouble();   //extraemos elemento por elemento
            }
        }
        return parte_matriz;
    }

    static void enviar_parte_matriz(double[][] matriz, int ini_i, int fin_i, int tam_j, DataOutputStream salida) throws IOException {

        for (int i = ini_i; i < fin_i; i++) {
            //almacenamos espacio para un renglon de N elementos (8 es el tamanio del double)
            ByteBuffer byte_buffer_matriz = ByteBuffer.allocate(tam_j * 8);

            for (int j = 0; j < tam_j; j++) { //metemos elemento x elemento al byte_buffer
                byte_buffer_matriz.putDouble(matriz[i][j]);
            }

            byte[] byte_matriz = byte_buffer_matriz.array();    //lo convertimos a un arreglo de byte
            salida.write(byte_matriz);  //enviamos el byte[] al servidor
        }
    }

    static class Worker extends Thread { //CLIENTE

        int nodo;

        Worker(int nodo) {
            this.nodo = nodo;
        }

        public void run() {  //Cuerpo del hilo
            Socket conexion = null;
            for (;;) {
                try {
                    //conexion = new Socket("localhost", 51000 + nodo);
                    //conexion = new Socket(IP, 51000 + nodo);
                    conexion = new Socket(IP, 51000);
                    
                    System.out.println("IP asignada para la conexion: " + IP);

                    DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                    salida.writeInt(N);     //enviamos el tamanio de la matriz

                    if (nodo == 1) { //enviamos la parte correspondiente de las matrices A y B al nodo
                        enviar_parte_matriz(A, 0, N / 2, N, salida);
                        enviar_parte_matriz(B, 0, N / 2, N, salida);

                        double parte_C[][] = new double[N / 2][N / 2];
                        parte_C = read_parte_matriz(N / 2, N / 2, entrada); //leemos C1 del nodo 1 (server 1)

                        //pasando C1 a la matriz C
                        for (int i = 0; i < N / 2; i++) {
                            for (int j = 0; j < N / 2; j++) {
                                C[i][j] = parte_C[i][j];
                            }
                        }
                    } else if (nodo == 2) {
                        enviar_parte_matriz(A, 0, N / 2, N, salida);
                        enviar_parte_matriz(B, N / 2, N, N, salida);

                        double parte_C[][] = new double[N / 2][N / 2];
                        parte_C = read_parte_matriz(N / 2, N / 2, entrada); //leemos C2 del nodo 2 (server 2)

                        int pos_j = N / 2;
                        for (int i = 0; i < N / 2; i++) { //pasando C2 a la matriz C
                            for (int j = 0; j < N / 2; j++) {
                                C[i][pos_j] = parte_C[i][j];
                                pos_j++;
                            }
                            pos_j = N / 2;
                        }

                    } else if (nodo == 3) {
                        enviar_parte_matriz(A, N / 2, N, N, salida);
                        enviar_parte_matriz(B, 0, N / 2, N, salida);

                        double parte_C[][] = new double[N / 2][N / 2];
                        parte_C = read_parte_matriz(N / 2, N / 2, entrada); //leemos C3 del nodo 3 (server 3)

                        int pos_i = N / 2;
                        for (int i = 0; i < N / 2; i++) { //pasando C3 a la matriz C
                            for (int j = 0; j < N / 2; j++) {
                                C[pos_i][j] = parte_C[i][j];
                            }
                            pos_i++;
                        }

                    }

                    conexion.close();   //cerramos conexion y salimos de la ejecucion del hilo
                    break;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, Exception {
        if (args.length != 1) {
            System.err.println("usage: java Token <nodo>");
            System.exit(1);
        }

        int nodo = Integer.parseInt(args[0]);
        //IP = args[1];

        if (nodo == 0) {      //CLIENTE    
            for (int i = 0; i < N; i++) //Inicialización de matrices
            {
                for (int j = 0; j < N; j++) {
                    A[i][j] = i + 5 * j;
                    B[i][j] = 5 * i - j;
                    C[i][j] = 0;
                }
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

            for (int i = 0; i < N; i++) //transposición de matriz:
            {
                for (int j = 0; j < i; j++) {
                    double x = B[i][j];
                    B[i][j] = B[j][i];
                    B[j][i] = x;
                }
            }

            System.out.println("Matriz B transpuesta:");
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    System.out.print(B[i][j] + " ");
                }
                System.out.println("");

            }

            System.out.println("");

            Worker w[] = new Worker[3];     //instancia de 3 hilos (para conectarnos a c/u de los server
            for (int i = 0; i < 3; i++) {
                w[i] = new Worker(i + 1);
                if (i + 1 == 1) {
                    IP = "104.210.131.240";
                } else if (i + 1 == 2) {
                    IP = "20.225.43.192";
                } else if (i + 1 == 3) {
                    IP = "20.225.42.231";
                }
                
                w[i].start();   //los iniciamos
            }

            for (int i = 0; i < 3; i++) {
                if (i + 1 == 1) {
                    IP = "104.210.131.240";
                } else if (i + 1 == 2) {
                    IP = "20.225.43.192";
                } else if (i + 1 == 3) {
                    IP = "20.225.42.231";
                }
                System.out.println("Enviado al nodo: " + (i+1) + " con número de IP: " + IP);
                w[i].join();    //esperamos a que terminen los 3 hilos
            }

            double aux_A[][] = new double[N / 2][N];  //matrices auxiliares para el calculo de C4 
            double aux_B[][] = new double[N / 2][N];
            double aux_C[][] = new double[N / 2][N / 2];
            int pos_i = 0;
            for (int i = N / 2; i < N; i++) {   //extraemos A2 y B2 en las matrices
                for (int j = 0; j < N; j++) {
                    aux_A[pos_i][j] = A[i][j];
                    aux_B[pos_i][j] = B[i][j];
                }
                pos_i++;
            }

            for (int i = 0; i < N / 2; i++) //calculamos C4 
            {
                for (int j = 0; j < N / 2; j++) {
                    for (int k = 0; k < N; k++) {
                        aux_C[i][j] += aux_A[i][k] * aux_B[j][k];
                    }
                }

            }

            System.out.println("parte matriz C");
            for (int i = 0; i < N / 2; i++) {
                for (int j = 0; j < N / 2; j++) {
                    System.out.print(aux_C[i][j] + " ");
                }
                System.out.println("");
            }

            pos_i = N / 2;
            int pos_j = N / 2;

            //Pasamos C4 a la matriz C
            for (int i = 0; i < N / 2; i++) {
                for (int j = 0; j < N / 2; j++) {
                    C[pos_i][pos_j] = aux_C[i][j];
                    pos_j++;
                }
                pos_j = N / 2;
                pos_i++;
            }

            double checksum = 0;
            for (int i = 0; i < N; i++) { //calculamos el CHECKSUM
                for (int j = 0; j < N; j++) {
                    checksum += C[i][j];
                }
            }

            if (N == 1000) {
                System.out.println("Checksum: " + checksum);

            } else {  // N = 8
                System.out.println("Matriz C completa en el cliente: ");

                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        System.out.print(C[i][j] + " ");
                    }
                    System.out.println("");
                }
                System.out.println("");

                System.out.println("Checksum: " + checksum);
            }

        } else {      //-----------------SERVIDOR------------------
            System.out.println("Nodo: " + nodo);
            ServerSocket servidor = new ServerSocket(51000);
            //ServerSocket servidor = new ServerSocket(51000 + nodo);
            for (;;) {    //espera siempre la conexión del cliente
                Socket conexion = servidor.accept();    //aceptar la conexión del cliente

                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                int tam_matriz = entrada.readInt();     //leemos el tamanio de la matriz
                System.out.println("N :" + tam_matriz);

                double parte_A[][] = new double[tam_matriz / 2][tam_matriz];
                double parte_B[][] = new double[tam_matriz / 2][tam_matriz];
                double parte_C[][] = new double[tam_matriz / 2][tam_matriz / 2];

                for (int i = 0; i < tam_matriz / 2; i++) //inicializamos la parte de la matriz Ci en 0
                {
                    for (int j = 0; j < tam_matriz / 2; j++) {
                        parte_C[i][j] = 0;
                    }
                }

                //lectura de la parte de la matriz Ai
                parte_A = read_parte_matriz(tam_matriz / 2, tam_matriz, entrada);
                System.out.println("Parte matriz A");
                for (int i = 0; i < tam_matriz / 2; i++) {
                    for (int j = 0; j < tam_matriz; j++) {
                        System.out.print(parte_A[i][j] + " ");
                    }
                    System.out.println("");
                }
                //lectura de la parte de la matriz Bi
                parte_B = read_parte_matriz(tam_matriz / 2, tam_matriz, entrada);
                System.out.println("parte matriz B");
                for (int i = 0; i < tam_matriz / 2; i++) {
                    for (int j = 0; j < tam_matriz; j++) {
                        System.out.print(parte_B[i][j] + " ");
                    }
                    System.out.println("");
                }
                //calculando la parte de la matriz Ci
                for (int i = 0; i < tam_matriz / 2; i++) {
                    for (int j = 0; j < tam_matriz / 2; j++) {
                        for (int k = 0; k < tam_matriz; k++) {
                            parte_C[i][j] += parte_A[i][k] * parte_B[j][k];
                        }
                    }
                }

                System.out.println("Enviando matriz Ci al cliente");
                //Enviamos el calculo de Ci al nodo 0 (cliente)
                enviar_parte_matriz(parte_C, 0, tam_matriz / 2, tam_matriz / 2, salida);

            }
        }
    }
}
