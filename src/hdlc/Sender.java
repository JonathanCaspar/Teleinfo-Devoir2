package hdlc;

import java.io.*;
import static java.lang.System.exit;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
   Agit en tant que SocketClient : tente de se connecter à un serveur
   Prend des arguments de la forme "<Nom_Machine> <Numero_Port> <Nom_fichier> <0>"
 */
public class Sender {

    private Frame[] sentFrames;
    private final int max_tries = 3;

    private final String host;
    private final int port;
    private final String file_name;
    private final int protocol;

    private Socket socket;
    private DataOutputStream dOut;

    public Sender() {
        this.host = "127.0.0.1"; //localhost
        this.port = 8080;
        this.file_name = null;
        this.protocol = 0;
    }

    public Sender(String host, int port, String file_name, int protocol) {
        this.host = host;
        this.port = port;
        this.file_name = file_name;
        this.protocol = protocol;
    }

    

    // Envoie des données (String) via un socket fourni en paramètre
    public void sendData(String data) {
        if (data != null && this.socket != null && this.socket.isConnected()) {

            try {
                this.dOut.writeUTF(data);
                this.dOut.flush(); // On envoie

            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean connect() {
        // Tente de se connecter plusieurs fois jusqu'à ce que le serveur soit lancé
        int try_attempt = 0;
        this.socket = new Socket();

        while (try_attempt <= this.max_tries && !socket.isConnected()) {
            try {
                SocketAddress sockaddr = new InetSocketAddress(this.host, this.port);
                this.socket = new Socket();

                // Connects this socket to the server with a specified timeout value
                // If timeout occurs, SocketTimeoutException is thrown
                this.socket.connect(sockaddr);

                System.out.println("Socket connected..." + this.socket);
                this.dOut = new DataOutputStream(this.socket.getOutputStream());
                return true;
            } catch (Exception e) {

                if (try_attempt == this.max_tries) {
                    System.out.println("Server was not found. Ending the program.");
                    return false;
                } else {
                    System.out.println("Attempt #" + (try_attempt + 1) + " : I/O Error " + e.getMessage() + ". Trying again ...");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try_attempt++;
                }
            }
        }
        return false;
    }

    public void disconnect() {
        try {
            this.dOut.close();
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public static void main(String[] args) throws Exception {

        // On fait une validation des arguments
        if (!Utils.validateSenderArgs(args)) {
            System.out.println("Les arguments fournis n'ont pas le format valide ('<Nom_Machine> <Numero_Port> <Nom_fichier> <0>')");
            exit(0);
        } else {
            Sender sender = new Sender(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));

            // Si la connection a fonctionné : on peut envoyer des données
            if (sender.connect()) {
                //                 01111110   00000001   00000011   10101010   1101 0101 1110 1010   01111110
                String rawFrame = "01111110" + "00000001" + "00000011" + "10101010" + "1101010111101010" + "01111110";
                //System.out.println(Frame.parseFrame(rawFrame).toString());
                sender.sendData(rawFrame);
                sender.disconnect();
            }
        }
    }
}
