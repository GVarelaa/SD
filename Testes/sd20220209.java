import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class sd20220209 {
    private Map<Integer, Integer> aEspera;
    private Integer numeroPessoas; // Key : nº da lista | Chave : número de membros da lista
    private Integer listaAtual;
    private ReentrantLock lock;
    private Condition cond;

    public sd20220119 sd20220119(){
        this.aEspera = new HashMap<>();
        this.numeroPessoas = 0;
        this.listaAtual = 0;
        this.lock = new ReentrantLock();
        this.cond = this.lock.newCondition();
    }

    void aumenta(int lista){
        if (!this.aEspera.containsKey(lista)) this.aEspera.put(lista, 1);
        else{
            int n = this.aEspera.get(lista);
            this.aEspera.put(lista, n+1);
        }
    }

    void diminui(int lista){
        int n = this.aEspera.get(lista);

        if (n-1 == 0) this.aEspera.remove(lista);
        else{
            this.aEspera.put(lista, n-1);
        }
    }

    int maisAEspera(){
        int lista = -1;
        int numero = -1;

        for(int key : this.aEspera.keySet()){
            if(this.aEspera.get(key) > numero){
                numero = this.aEspera.get(key);
                lista = key;
            }
        }

        return lista;
    }

    void participa(int lista){
        this.lock.lock();

        this.aumenta(lista);

        while(lista != listaAtual && numeroPessoas>0 && this.maisAEspera() != lista){
            this.cond.await();
        }

        this.diminui(lista);
        numeroPessoas++;
        listaAtual = lista;

        this.lock.unlock();
    }


    void abandona(int lista){
        this.lock.lock();

        this.numeroPessoas--;
        if (numeroPessoas == 0) this.cond.signalAll();

        this.lock.unlock();
    }

    int naSala(){
        try{
            this.lock.lock();
            return this.numeroPessoas;
        }
        finally {
            this.lock.unlock();
        }
    }

    int aEspera(){
        try{
            this.lock.lock();
            int aEspera = 0;
            for (int value : this.aEspera.values()) aEspera += value;

            return aEspera;
        }
        finally {
            this.lock.unlock();
        }
    }
}




public class ServerWorker implements Runnable{
    private Socket socket;
    private sd20220209 estado;
    private boolean participa;

    public ServerWorker(Socket socket, sd20220209 estado){
        this.socket = socket;
        this.estado = estado;
        this.participa = false;
    }

    public void run(){
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        int i = Integer.parseInt(is.readLine());

        if(i > 0){
            new Thread(()-> {
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                int lista = i;
                this.participa = true;
                this.estado.participa(lista);

                out.println("1");
                out.flush();

                int abandona = Integer.parseInt(is.readLine());

                if (abandona == 0) this.estado.abandona();

                this.socket.close();
            }).start;
        }
        else if (this.participa == true){
            new Thread(() -> {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                if (i == -1){
                    int n = this.estado.naSala();
                    out.println(n);
                    out.flush();
                }
                else if (i == -2){
                    int n = this.estado.aEspera();
                    out.println(n);
                    out.flush();
                }
            }).start();
        }


    }
}



public class Server{
    public static void main(String[] args) {
        ServerSocket serverSocket = new ServerSocket("12345");
        sd20220209 estado = new sd20220209();

        while(true){
            Socket socket = serverSocket.accept();
            ServerWorker serverWorker = new ServerWorker(socket, estado);

            for (int i = 0; i < 2; i++){
                Thread worker = new Thread(serverWorker);
                worker.start();
            }
        }
    }
}






