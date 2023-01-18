import com.sun.source.tree.Scope;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class sd20220119 {
    private Set<Integer> identidades;
    private Integer taoAcabar;
    private Map<Integer, Boolean> cabines;
    private Map<Integer, Integer> votos;
    private ReentrantLock lock;
    private Condition cond;
    private Condition acabar;
    private boolean end;

    public sd20220119 sd20220119(){
        this.identidades = new HashSet<>();
        this.taoAcabar = 0;
        this.cabines = new HashMap<>();
        this.votos = new HashMap<>();
        this.lock = new ReentrantLock();
        this.cond = lock.newCondition();
        this.acabar = lock.newCondition();
        this.end = false;
    }

    boolean verifica(int identidade){
        this.lock.lock();

        if (this.end == false){
            boolean bool = false;

            for(int id : identidades){
                if (id == identidade) bool = true;
            }

            if (bool == false){
                this.identidades.add(identidade);
                this.taoAcabar++;
            }

            this.lock.unlock();
            return bool;
        }
        else {
            this.lock.unlock();
            return false;
        }
    }

    void vota(int escolha){
        this.lock.lock();

        if (this.votos.containsKey(escolha)){
            int numero = this.votos.get(escolha);
            this.votos.put(escolha, numero+1);
        }
        else this.votos.put(escolha, 1);

        this.taoAcabar--;
        this.acabar.signalAll();

        this.lock.unlock();
    }

    void desocupaCabine(int cabine){
        this.lock.lock();

        this.cabines.put(cabine, true);
        this.cond.signalAll();

        this.lock.unlock();
    }

    int procuraCabine(){
        int ret = -1;
        for(int key : this.cabines.keySet()){
            if (this.cabines.get(key) == true) return key;
        }
        return ret;
    }

    int esperaPorCabine(){
        this.lock.lock();
        int cabine;

        while((cabine = procuraCabine()) == -1){
            this.cond.await();
        }

        this.lock.unlock();
        return cabine;
    }

    int vencedor(){
        this.lock.lock();
        this.end = true;

        while(this.taoAcabar != 0){
            this.acabar.await();
        }
        this.lock.unlock();

        int escolhaVencedora = -1;
        int votosEscolha = -1;
        for (int escolha : this.votos.keySet()){
            if(this.votos.get(escolha) > votosEscolha){
                votosEscolha = this.votos.get(escolha);
                escolhaVencedora = escolha;
            }
        }

        return escolhaVencedora;
    }
}


public class ServerWorker implements Runnable{
    private Socket socket;
    private sd20220119 estado;

    public ServerWorker(Socket s, sd20220119 e){
        this.socket = s;
        this.estado = e;
    }

    public void run(){
        BufferedReader is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        int cabine;

        int id = Integer.parseInt(is.readLine());

        if(this.estado.verifica(id) == false){
            out.println("0");
        }
        else{
            cabine = this.estado.esperaPorCabine();
            out.println(cabine);
        }

        out.flush();

        int voto = Integer.parseInt(is.readLine());
        this.estado.vota(voto);
        this.estado.desocupaCabine(cabine);

        out.println("1");
        out.flush();

        socket.close();
    }
}


public class Server{
    public static void main(String[] args) {
        ServerSocket serverSocket = new ServerSocket("12345");
        sd20220119 estado = new sd20220119();

        while(true){
            Socket socket = serverSocket.accept();
            ServerWorker worker = new ServerWorker();

            Thread worker = new Thread(new ServerWorker(socket, estado));
            worker.start();
        }
    }
}





