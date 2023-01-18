// Exercicio 6

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


interface Votacao {
    boolean verifica(int identidade);
    int esperaPorCabine();
    void vota(int escolha);
    void desocupaCabine(int i);
    int vencedor(); // apenas para a alinea de Valorização
}

// Barrar a entrada no verifica para fechar a eleição (valorização)

class Exercicio6 implements Votacao{
    // Estrutura para os que ja votaram
    // Estrutura para as cabines
    // Estrutura para os votos

    // Conjunto de identidades que ja votaram (verifica)
    // Estado de ocupação de cabines (esperaPorCabine e desocupaCabine => variavel de condição)
    // Contagem de votos por escolha E (vota)
    // Conhecer os eleitores em votação (vencedor e verifica)

    private Set<Integer> identidades = new HashSet<Integer>()
    private Map<Integer, Boolean> cabines = new HashMap<Integer, Boolean>();
    private Map<Integer, Integer> votos = new HashMap<Integer, Boolean>();
    private ReentrantLock lock = new ReentrantLock();
    private Condiction c = lock.newCondition();

    public boolean verifica(int identidade){
        try{
            this.lock.lock();

            for (Integer id : this.identidades){
                if(id == identidade) return true;
            }

            this.identidades.add(identidade);
            return false;
        }
        finally {
            this.lock.unlock();
        }
    }


    public void vota(int escolha){
        try{
            this.lock.lock();

            if(this.votos.get(escolha) == null){ // ou containsKeys
                this.votos.put(escolha, 1);
            }
            else{
                numero_votos = this.votos.get(escolha);
                this.votos.put(escolha, numero_votos);
            }
        }
        finally {
            this.lock.unlock();
        }
    }


    public int esperaPorCabine(){
        try{
            this.lock.lock();

            while(this.cabines.containsKey())
        }
        finally {
            this.lock.unlock();
        }
    }
}


class ServerWorker implements Runnable{
    private Socket socket;
    private Votacao votacao;

    //Meter lock para garantir controlo de concorrencia

    ServerWorker(Socket socket, Votacao votacao){
        this.socket = socket;
        this.votacao = votacao;
    }

    @Override
    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Reader - transformar em bytes em texto
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            int cabine = 0;
            String line;

            // não usamos while porque nao temos tag e nao sabemos qual string corresponde a quê
            // le mensagem id
            line = in.readLine();
            int id = Integer.parseInt(line);

            if(votacao.valida(id))
                out.println(cabine = votacao.esperaporCabine());
            else
                out.println("0");

            out.flush();

            // le mensagem voto
            line = in.readLine();
            int voto = Integer.parseInt(line);

            votacao.voto(voto);
            votacao.desocupaCabine(cabine);

            out.println("1");
            out.flush();


            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}


// Exercício 7
public class Server{
    public static void main(String[] args) {
        ServerSocket ss = new ServerSocket("localhost", 12345);
        Votacao votacao = new Votacao();

        while(true){
            Socket socket = ss.accept();

            Thread worker = new Thread(new ServerWorker(socket, votacao));
            worker.start();
        }

    }
}