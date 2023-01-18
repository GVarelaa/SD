import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface ControloTrafegoAereo{
    int pedirParaDescolar();
    int pedirParaAterrar();
    void descolou(int pista);
    void aterrou(int pista);
}

public class ControTrafegoAreoTeste implements ControloTrafegoAereo{
    private Map<Integer, Boolean> pistas;
    private Integer pedidosAterragem;
    private Integer pedidosDescolar;
    private Integer pedido;
    private ReentrantLock lock;
    private Condition esperaPorPista;

    public ControTrafegoAreoTeste(){
        this.pistas = new HashMap<>(); // livre
        this.pedidosAterragem = 0;
        this.pedidosDescolar = 0;
        this.pedido = 0; // 0 - Aterragem ; 1 - Descolagem
        this.lock = new ReentrantLock();
        this.esperaPorPista = lock.newCondition();
    }

    public List<Integer> pistasLivres(){
        List<Integer> pistas = new ArrayList<>();
        for(int pista : this.pistas.keySet()){
            if (this.pistas.get(pista) == false) pistas.add(pista);
        }
        return pistas;
    }


    @Override
    public int pedirParaDescolar() {
        this.lock.lock();
        List<Integer> pistas = this.pistasLivres();

        while(pistas.size() == 0 || pistas.size() == 1 && this.pedido == 0){
            this.esperaPorPista.await();
            pistas = this.pistasLivres();
        }
        
        this.pistas.put(pistas.get(0), false);
        this.pedido = 0;

        this.lock.unlock();
        return pistas.get(0);
    }

    @Override
    public int pedirParaAterrar() {
        this.lock.lock();
        List<Integer> pistas = this.pistasLivres();

        while(pistas.size() == 0 || pistas.size() == 1 && this.pedido == 1){
            this.esperaPorPista.await();
            pistas = this.pistasLivres();
        }

        this.pistas.put(pistas.get(0), false);
        this.pedido = 1;

        this.lock.unlock();
        return pistas.get(0);
    }

    @Override
    public void descolou(int pista) {
        this.lock.lock();

        this.pistas.put(pista, true);
        this.esperaPorPista.signalAll();

        this.lock.unlock();
    }

    @Override
    public void aterrou(int pista) {
        this.lock.lock();

        this.pistas.put(pista, true);

        this.lock.unlock();
    }
}

public class ServerWorker implements Runnable{
    private Socket socket;
    private ControloTrafegoAereo controlo;

    public ServerWorker(Socket socket, ControloTrafegoAereo controlo){
        this.socket = socket;
        this.controlo = controlo;
    }

    public void run(){
        BufferedReader is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter os = new PrintWriter(this.socket.getOutputStream());

        int op = Integer.parseInt(is.readLine());

        if (op == 0){
            int pista = this.controlo.pedirParaAterrar();
            this.controlo.aterrou(pista);
        }
        else if (op == 1){
            int pista = this.controlo.pedirParaDescolar();
            this.controlo.descolou(pista);
        }

        this.socket.close();
    }
}



public class Server{
    public static void main(String[] args){
        ServerSocket serverSocket = new ServerSocket(12345);
        ControloTrafegoAereo controlo = new ControTrafegoAreoTeste();

        while(true){
            Socket socket = serverSocket.accept();
            ServerWorker serverWorker = new ServerWorker(socket, contrlo);

            Thread worket = new Thread(serverWorker);
            worket.start();
        }
    }
}