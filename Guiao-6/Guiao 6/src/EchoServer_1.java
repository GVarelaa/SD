import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer_1 {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(12345);

            while (true) {
                Socket socket = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Reader - transformar em bytes em texto
                PrintWriter out = new PrintWriter(socket.getOutputStream());

                int soma = 0;
                int n = 0;

                String line;
                while ((line = in.readLine()) != null) {
                    int numero = Integer.parseInt(line);

                    n++;
                    soma += numero;

                    out.println(soma);
                    out.flush(); // força o envio de dados
                }

                float media = soma / n;
                out.println(media);
                out.flush();

                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
