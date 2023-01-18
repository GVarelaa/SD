package g8;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class FramedConnection implements AutoCloseable {
    private final Socket socket;
    private final DataInputStream is;
    private final DataOutputStream os;
    //lock para input e output stream, para aumentar eficiencia
    //read write lock, exclusivo em escrita e partilhado em leituras

    private final ReentrantLock sendLck = new ReentrantLock();
    private final ReentrantLock receiveLck = new ReentrantLock();

    public FramedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.is = new DataInputStream(new BufferedInputStream(socket.getInputStream())); // input stream
        this.os =  new DataOutputStream(new BufferedOutputStream(socket.getOutputStream())); // output stream
    }

    public void send(byte[] data) throws IOException {
        try{
            this.sendLck.lock();
            //temos de enviar o tamanho do array(tamanho dos dados)
            this.os.writeInt(data.length); // tamanho dos dados pra depois saber ler
            this.os.write(data);
            this.os.flush();
        }
        finally{
            this.sendLck.unlock();
        }
    }

    public byte[] receive() throws IOException {
        byte[] data;

        try{
            this.receiveLck.lock();
            int size = this.is.readInt();
            data = new byte[size];

            this.is.readFully(data); // so desbloquea quando le tudo, garante q n lemos bocados de mensagens
        }
        finally {
            this.receiveLck.unlock();
        }

        return data;
    }
    public void close() throws IOException {
        this.socket.close();
    }
}

