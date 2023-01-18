import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Arrays.asList;

class ContactManager {
    private HashMap<String, Contact> contacts;
    private ReentrantLock lock = new ReentrantLock();

    public ContactManager() {
        this.contacts = new HashMap<>();
        // example pre-population
        this.update(new Contact("John", 20, 253123321, null, asList("john@mail.com")));
        this.update(new Contact("Alice", 30, 253987654, "CompanyInc.", asList("alice.personal@mail.com", "alice.business@mail.com")));
        this.update(new Contact("Bob", 40, 253123456, "Comp.Ld", asList("bob@mail.com", "bob.work@mail.com")));
    }


    // @TODO
    public void update(Contact c) {
        try{
            this.lock.lock();
            this.contacts.put(c.getName(), c);
        }
        finally {
            this.lock.unlock();
        }
    }

    // @TODO
    public ContactList getContacts() { return new ContactList(this.contacts.values());}
}

class ServerWorker implements Runnable {
    private Socket socket;
    private ContactManager manager;

    public ServerWorker (Socket socket, ContactManager manager) {
        this.socket = socket;
        this.manager = manager;
    }

    // @TODO
    @Override
    public void run() {
        //ler contact do inputStream
        DataInputStream in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));

        try {
            while(true){
                Contact contact = Contact.deserialize(in);
                this.manager.update(contact);
            }

            socket.close();
        }
        catch (IOException e){

            System.out.println("Connection is closed....");
            //this.socket.shutdownInput();
        }
        //
    }
}



public class Server {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        ContactManager manager = new ContactManager();

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerWorker(socket, manager));
            worker.start();
        }
    }

}
