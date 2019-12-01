package hdlc;

import java.io.*;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {

    private ArrayList<Frame> receivedFrames;

    private final int port;

    private ServerSocket sSocket;
    private Socket socket;
    private DataInputStream dIn;
    private DataOutputStream dOut;

    public Receiver(int port) {
        this.port = port;
    }

    public boolean initialize() {
        try {
            this.sSocket = new ServerSocket(port);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean acceptClient() {
        try {
            this.socket = this.sSocket.accept();
            this.dIn = new DataInputStream(this.socket.getInputStream());
            this.dOut = new DataOutputStream(this.socket.getOutputStream());
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void send(Frame frame) {
        if (frame != null && this.socket != null && this.socket.isConnected()) {
            
            String binaryFrame = frame.encode();
            try {
                this.dOut.writeUTF(binaryFrame);
                this.dOut.flush(); // On envoie

            } catch (IOException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void listenForFrames() {
        boolean done = false;
        int expectedNum = 0;
        
        while (!done) {

            // On crée une Trame à partir de la suite reçue et on lit le type de message
            try {
                String request = this.dIn.readUTF();
                //System.out.println("\nMessage received: " + request);

                Frame frame = Frame.parseFrame(request);

                /*//Introduction volontaire d'une erreur dans data d'une trame dinformation :
                if (frame.getType() == FrameType.I) {
                    String oldData = frame.getData();
                    frame.setData("xx");
                    System.out.println("!!! Introduction d'une erreur : data = '" + oldData + "' devient data = 'xx'");
                }*/

                System.out.println("------- Frame received: " + frame.toString());

                // Vérifie si la trame n'est pas corrompue
                if (frame.checkValidity()) {
                    
                    // Adapte la réponse selon le type de paquet recu
                    switch (frame.getType()) {
                        
                        case I:
                            // Le numéro recu est celui attendu
                            if(expectedNum == frame.getNum()){
                                System.out.print("Reception du message : "+ frame.getData());
                                this.receivedFrames.add(frame);
                                
                                expectedNum = (expectedNum + 1) % Frame.MAX_SEQ_NUM;

                                send(Frame.createAckFrame(expectedNum));
                            }
                            
                            break;

                        case C:
                            System.out.print("Demande de connexion recu avec ");
                            
                            if(frame.getNum() == 0){
                                System.out.println("le protocole Go-Back-N.");
                                System.out.println("Autorisé : Envoi d'une trame d'acquittement");
                                send(Frame.createAckFrame(0));
                            }
                            else{
                                System.out.println("un protocole inconnu.");
                                System.out.println("Refusé : Envoi d'une trame de refus.");
                                send(Frame.createRejFrame(0));
                                System.out.println("Arrêt du serveur.");
                                done = true;
                            }
                            break;
                            
                        case F:
                            System.out.println("RECU UNE DEMANDE DE FERMETURE DE CONNEXION !");
                            done = true;
                            break;

                        case P:
                            break;

                        default:
                            done = true;
                            break;
                    }
                    
                } else {
                    System.out.println("Frame " + frame.getNum() + " received is corrupted!");
                }

            } catch (EOFException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                System.out.println("Frame string is invalid : " + e.toString());
            }
        }
    }

    public void disconnectClient() {
        try {
            this.dIn.close();
            this.dOut.close();
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void disconnectSocket() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws Exception {

        if (false && !Utils.validateReceiverArgs(args)) { // 'false' pour test seulement
            System.out.println("Les arguments fournis n'ont pas le format valide ('<Numero_Port>')");
            exit(0);
        } else {
            Receiver receiver = new Receiver(Integer.parseInt("82")); //'82' à remplacer par args[0]

            if (receiver.initialize() && receiver.acceptClient()) {

                receiver.listenForFrames();

                receiver.disconnectClient();
                receiver.disconnectSocket();
            }

        }
    }
}
