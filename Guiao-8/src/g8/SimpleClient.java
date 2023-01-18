package g8;

import java.net.Socket;

public class SimpleClient {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 12345);
        FramedConnection c = new FramedConnection(s);

        // send requests

        // get replies
        c.send("Ola".getBytes());
        c.send("Hola".getBytes());
        c.send("Hello".getBytes());

        byte[] b1 = c.receive();
        byte[] b2 = c.receive();
        byte[] b3 = c.receive();
        //ordem chega trocada porque o servidor é multi threaded
        //acrescentar uma etiqueta nos pedidos para sabermos a ordem no final => TaggedConnection
        //TaggedConnection => Primeiro escrevemos a tag e dps a frame (tag | n | data)

        System.out.println("Some Reply: " + new String(b1));
        System.out.println("Some Reply: " + new String(b2));
        System.out.println("Some Reply: " + new String(b3));

        c.close();
    }
}
