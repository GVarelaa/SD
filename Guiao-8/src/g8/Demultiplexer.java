package g8;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements AutoCloseable {

    private HashMap<Integer, TaggedConnection.Frame[]> map;
    private final TaggedConnection connection;
    private final Lock lock = new ReentrantLock(); // global porque estamos a mexer no map para garantir acesso a maps
    private final Map<Integer, Entry> buf = new HashMap<>();
    private Exception exception;

    private class Entry{
        final Condition cond = lock.newCondition();
        final ArrayDeque<byte[]> queue = new ArrayDeque<>();
    }

    private Entry get(int tag){

    }

    public Demultiplexer(TaggedConnection conn) {
        this.map = new HashMap<Integer, TaggedConnection.Frame[]>();
        this.connection = conn;
    }
    public void start() {
        Thread thread = new Thread(() -> {
            while(true){
                TaggedConnection.Frame frame = this.connection.receive();
                this.lock.lock(); // vou alterar o mapa

                try{
                    Entry e = get(frame.tag);

                    e.queue.add(frame.data);
                    e.cond.signal();
                }
                finally {
                    this.lock.unlock();
                }
            }

            catch(IOException e){
                this.lock.lock();

                try{
                    exception = e;

                }
            }
        }).start();
    }
    public void send(TaggedConnection.Frame frame) throws IOException {
        this.connection.send(frame);
    }
    public void send(int tag, byte[] data) throws IOException {
        this.connection.send(tag, data);
    }
    public byte[] receive(int tag) throws IOException, InterruptedException {
        this.lock.lock();

        try{
            Entry e = get(tag);

            while(e.queue.isEmpty() && exception != null){
                e.cond.await();
            }

            if(!e.queue.isEmpty()){
                return e.queue.poll();
            }
            else{
                throw exception;
            }

        }
        finally {
            this.lock.unlock();
        }
    }

    public void close() throws IOException {

    }
}