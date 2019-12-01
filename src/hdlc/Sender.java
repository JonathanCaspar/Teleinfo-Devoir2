package hdlc;

import java.io.*;
import static java.lang.System.exit;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;

/*
   Agit en tant que SocketClient : tente de se connecter à un serveur
   Prend des arguments de la forme "<Nom_Machine> <Numero_Port> <Nom_fichier> <0>"
 */
public class Sender {

    private ArrayList<Frame> infoFrames;
    private final int MAX_TRIES = 5;

    private final String HOST;
    private final int PORT;
    private final String FILE_NAME;
    private final int PROTOCOL;

    private Socket socket;
    private DataOutputStream dOut; // pour envoi
    private DataInputStream dIn; // pour reception
    private Reader fileReader;

    Frame[] ackedFrame = new Frame[8]; //Pour conserver les ack recus

    private Timer timer;
    private boolean windowFull;
    private int frameToSend;
    private int[] unAckedFrame; //Pour garder en mémoire les frames envoyés
    private int positionWindow; //Conserve la derni�re frame envoy� mais non confirm�

    public Sender() {
        this.HOST = "127.0.0.1"; //localhost
        this.PORT = 8080;
        this.FILE_NAME = null;
        this.PROTOCOL = 0;
    }

    public Sender(String host, int port, String file_name, int protocol) {
        this.HOST = host;
        this.PORT = port;
        this.FILE_NAME = file_name;
        this.PROTOCOL = protocol;
    }

    public int getProtocol() {
        return this.PROTOCOL;
    }

    // Envoie un Frame via un socket fourni en paramètre
    public void send(Frame frame) {
        if (frame != null && this.socket != null && this.socket.isConnected()) {
            System.out.println("Trame envoyée : " + frame.toString());
            String binaryFrame = frame.encode();
            try {
                this.dOut.writeUTF(binaryFrame);
                this.dOut.flush(); // On envoie

            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Attend la reception d'un Frame de Receiver
    public Frame receive() {
        try {
            while (this.dIn.available() != 0); // bloque jusqu'à recevoir des données

            String request = this.dIn.readUTF();
            Frame frame = Frame.parseFrame(request);
            return frame;
        } catch (IOException e) {
            System.err.println("Send: Error on BufferedReader.readLine() - IOException");
        }
        return null;
    }

    // Extrait les caractères d'un fichier texte, les fragmente et génère des Frame
    public boolean readFile() {
        try {
            this.fileReader = new FileReader(this.FILE_NAME);
            this.infoFrames = new ArrayList<Frame>();

            char[] section = new char[Frame.DATA_MAX_SIZE];
            int numSeq = 0;

            // Tant qu'on arrive pas à la fin du fichier
            while (fileReader.read(section, 0, Frame.DATA_MAX_SIZE) != -1) {

                // On crée la trame d'Information (FrameType.I)
                // en y insérant la section de caractère lue dans le fichier
                Frame frame = Frame.createInfoFrame(numSeq, String.valueOf(section));

                // Débuggage
                //System.out.println("\nFrame created = " + frame.toString());
                //System.out.println("Binary form   = " + frame.encode());
                // On ajoute la trame créée à notre ArrayList
                this.infoFrames.add(frame);

                // On calcule le prochain numéro de séquence
                numSeq = (numSeq + 1) % Frame.MAX_SEQ_NUM;
            }

            return true;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean connect() {
        // Tente de se connecter plusieurs fois jusqu'à ce que le serveur soit lancé
        int try_attempt = 0;
        this.socket = new Socket();

        while (try_attempt <= this.MAX_TRIES && !socket.isConnected()) {
            try {
                SocketAddress sockaddr = new InetSocketAddress(this.HOST, this.PORT);
                this.socket = new Socket();
                this.socket.connect(sockaddr);

                System.out.println("Socket connected : " + this.socket);
                this.dOut = new DataOutputStream(this.socket.getOutputStream());
                this.dIn = new DataInputStream(this.socket.getInputStream());
                return true;
            } catch (Exception e) {

                if (try_attempt == this.MAX_TRIES) {
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
            this.dIn.close();
            this.socket.close();
            this.fileReader.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<Frame> getInfoFrames() {
        return this.infoFrames;
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new Task(), 1000); //Si le sender n'a pas re�u de r�ponse du receveur apr�s 1 seconde, il d�marre un timer.
    }

    class Task extends TimerTask {

        public void run() {
            frameToSend = unAckedFrame[positionWindow];
            windowFull = false;
            timer.cancel();
            System.out.println("Ack not received, sending everything back!");
        }
    }

    public static void main(String[] args) throws Exception {

        // On fait une validation des arguments
        if (!Utils.validateSenderArgs(args)) {
            System.out.println("Les arguments fournis n'ont pas le format valide ('<Nom_Machine> <Numero_Port> <Nom_fichier> <0>')");
            exit(0);
        } else {
            Sender sender = new Sender(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));

            // Si la connection a fonctionné
            // ET que la lecture et l'extraction des trames du fichier a fonctionné
            // on peut commencer à envoyer les données
            if (sender.connect() && sender.readFile()) {

                // 1) Demande de connexion pour transmission de données
                Frame connectionFrame = Frame.createConnectionFrame(sender.getProtocol());
                sender.send(connectionFrame);

                // Attend la réponse du destinataire
                System.out.println("En attente d'une réponse pour la demande de transmission...");
                Frame response = sender.receive();
                
                // Si la réponse est OUI
                // 2) Envoyer les frames
                if (response.getType() == FrameType.A) {
                    System.out.println("Demande de transmission acceptée");
                    ArrayList<Frame> infoFrames = sender.getInfoFrames();

                    for (int i = 0; i < infoFrames.size(); i++) {
                        sender.send(infoFrames.get(i));
                    }
                }
                else System.out.println("Demande de transmission refusée");

                // Tentative d'implémentation de Go-back-N côté Sender avec https://github.com/tomersimis/Go-Back-N/blob/master/Sender.java
                // mais infructueux car numérotation de séquence circulaire (modulo) dans notre cas
                /*while (true) {
                    
                    final int WINDOW_SIZE = 2;
                    final int TIMER = 30;

                    // Numero de séquence de la derniere trame envoyée
                    int lastSent = 0;

                    // Numéro de séquence de la derniere trame acquittée
                    int waitingForAck = 0;

                    // Dernier numéro de trame
                    int lastSeq = 0;
                    int infoFrameIndex = 0;

                    while (lastSent - waitingForAck < WINDOW_SIZE && lastSent < lastSeq) {
                        Frame frame = infoFrames.get(infoFrameIndex);
                        // Add packet to the sent list
                        sentFrame.add(frame);

                        sender.send(frame);

                        lastSent = (lastSent + 1) % Frame.MAX_SEQ_NUM;
                        infoFrameIndex++;

                    }

                    // Byte array for the ACK sent by the receiver
                    byte[] ackBytes = new byte[40];

                    // Creating packet for the ACK
                    DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);

                    try {
                        // If an ACK was not received in the time specified (continues on the catch clausule)
                        sender.setSoTimeout(TIMER);

                        // Receive the packet
                        Frame ackFrame = sender.receive();
                        System.out.println("Received ACK for " + ackFrame);

                        // If this ack is for the last packet, stop the sender (Note: gbn has a cumulative acking)
                        if (ackFrame.getNum() == lastSeq) {
                            break;
                        }

                        waitingForAck = Math.max(waitingForAck, ackFrame.getNum());

                    } catch (SocketException e) {
                        // then send all the sent but non-acked packets

                        for (int i = waitingForAck; i < lastSent; i++) {

                            sender.send(infoFrames.get(i));

                            System.out.println("REsending packet with sequence number " + infoFrames.get(i).getNum());
                        }
                    }

                }*/
                // 3) Demande de fermeture
                Frame closureFrame = Frame.createClosureFrame();
                sender.send(closureFrame);

                sender.disconnect();
            }
        }
    }
}
