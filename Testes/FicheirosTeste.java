import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface Ficheiros{
    void using(String path);
    void notUsing(String path, boolean modified);
    List<String> startBackup();
    void endBackup();
}


public class FicheirosTeste implements Ficheiros{
    private class Ficheiro{
        public Boolean aSerUsado;
        public LocalDateTime ultimaModificacao;
    }

    private Map<String, Ficheiro> ficheiros;
    private LocalDateTime ultimoBackup;
    private ReentrantLock lock;
    private Condition cond;

    FicheirosTeste(){
        this.ficheiros = new HashMap<>();
        this.ultimoBackup = null;
        this.lock = new ReentrantLock();
        this.cond = lock.newCondition();
    }

    public void using(String path){
        this.lock.lock();

        Ficheiro ficheiro = this.ficheiros.get(path);
        if (ficheiro.aSerUsado == false){
            ficheiro.aSerUsado = true;
        }

        this.lock.unlock();
    }

    public void notUsing(String path, boolean modified){
        this.lock.lock();

        Ficheiro ficheiro = this.ficheiros.get(path);
        ficheiro.aSerUsado = false;
        if (modified){
            ficheiro.ultimaModificacao = LocalDateTime.now();
        }

        this.cond.signalAll();
        this.lock.unlock();
    }

    public boolean beingUsed(){
        for (Ficheiro ficheiro : this.ficheiros.values()){
            if (ficheiro.aSerUsado) return true;
        }
        return false;
    }

    public List<String> startBackup(){
        this.lock.lock();

        while(this.beingUsed()){
            this.cond.await();
        }

        List<String> paths = new ArrayList<>();

        for (String path : this.ficheiros.keySet()){
            if (this.ultimoBackup == null || this.ultimoBackup.isBefore(this.ficheiros.get(path).ultimaModificacao)){
                paths.add(path);
            }
        }

        this.lock.unlock();
        return paths;
    }

    public void endBackup(){
        this.lock.lock();

        this.ultimoBackup = LocalDateTime.now();

        this.lock.unlock();
    }
}

public class ServerWorker implements Runnable{
    private Socket socket;
    private Ficheiros ficheiros;

    public ServerWorker(Socket socket, Ficheiros ficheiros){
        this.socket = socket;
        this.ficheiros = ficheiros;
    }

    public void run(){
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter os = new PrintWriter(socket.getOutputStream());

        int op = Integer.parseInt(is.readLine());

        if (op == 1){
            String path = is.readLine();
            this.ficheiros.using(path);

            int notUsing = Integer.parseInt(is.readLine());
            if (notUsing == 0)  this.ficheiros.notUsing(path, false);
            else if (notUsing == 1) this.ficheiros.notUsing(path, true);
        }
        else if(op == 2){
            List<String> paths = this.ficheiros.startBackup();

            for(String path : paths){
                os.print(path);
            }
            os.flush();

            int end = Integer.parseInt(is.readLine());
            if (end == 3) this.ficheiros.endBackup();
        }

        this.socket.close();
    }
}


public class Server{
    public static void main(String[] args){
        ServerSocket serverSocket = new ServerSocket(12345);
        Ficheiros ficheiros = new FicheirosTeste();

        while(true){
            Socket socket = serverSocket.accept();
            ServerWorker serverWorker = new ServerWorker(socket, ficheiros);

            Thread worker = new Thread(serverWorker);
            worker.start();
        }
    }
}


