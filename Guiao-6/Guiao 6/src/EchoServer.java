import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Register{
    private int sum = 0;
    private int n = 0;

    public void add(int value){

    }

    public double avg(){

    }
}

class ServerWorker implements Runnable{
    private Socket socket;
    private Register register;

    //Meter lock para garantir controlo de concorrencia

    ServerWorker(Socket socket, Register reg){
        this.socket = socket;
        this.register = reg;
    }

    @Override
    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Reader - transformar em bytes em texto
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            int soma = 0;
            int n = 0;

            String line;
            while ((line = in.readLine()) != null) {
                int numero = Integer.parseInt(line);

                n++;
                soma += numero;

                register.add(numero);

                out.println(soma);
                out.flush(); // força o envio de dados
            }

            out.println(register.avg());
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

public class EchoServer{
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);
            Register reg = new Register();

            while (true) {
                Socket socket = ss.accept();

                Thread worker = new Thread(new ServerWorker(socket, reg));
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
