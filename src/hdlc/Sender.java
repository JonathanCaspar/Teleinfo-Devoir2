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

            String binaryFrame = frame.encode();
            try {
                this.dOut.writeUTF(binaryFrame);
                this.dOut.flush(); // On envoie

            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Attend la reception d'un paquet de Receiver
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

    public void sendInfoFrame(int frameToSend) {
        this.send(this.infoFrames.get(frameToSend));
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

        Frame[] ackedFrame = new Frame[8]; //Pour conserver les ack recus

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
                System.out.println("Waiting for response for connection request...");
                Frame response = sender.receive();
                
                System.out.println("Received frame : " + response.toString());

                // 2) Envoyer tant qu'il y a des frames à envoyer
                sender.windowFull = false;
                sender.frameToSend = 0;
                sender.unAckedFrame = new int[8];
                sender.positionWindow = 0;
                int seqFirst = 0;
                
                while (true) {

                    if (!sender.windowFull) { //envoyer tant qu'il y a de la place dans la fenêtre

                        sender.sendInfoFrame(sender.frameToSend);

                        sender.unAckedFrame[sender.positionWindow] = sender.frameToSend++; //Conserve les éléments envoyé dans la fenêtre
                        sender.positionWindow++;
                        //System.out.println("Position window " + sender.positionWindow);

                        if (sender.positionWindow == sender.unAckedFrame.length) {
                            //***Vérifier si dans l'array ackedframe on retrouve le Ack du de la frame à la position PositionWindow
                            sender.windowFull = true;

                        }

                    } else {
                        //Si on a fait un tour complet de la fen�tre sans avoir re�u de confirmation
                        if (sender.positionWindow == sender.infoFrames.get(sender.frameToSend).getNum()) {

                            sender.startTimer();//On d�marre le chrono. Si on n'a pas re�u la prochaine frame avant la fin du chrono, on r�envoit toute la fen�tre.
                            break;
                        }

                    }

                }

                // 3) Demande de fermeture
                Frame closureFrame = Frame.createClosureFrame();
                sender.send(closureFrame);

                sender.disconnect();
            }
        }
    }
}
