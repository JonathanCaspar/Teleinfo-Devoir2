package hdlc;

import java.io.*;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

public class Receiver {

    private Frame[] receivedFrames;

    private final int port;

    private ServerSocket sSocket;
    private Socket socket;
    private DataInputStream dIn;

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
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void listenForFrames() {
        boolean done = false;
        while (!done) {

            // On crée une Trame à partir de la suite reçue et on lit le type de message
            try {
                String request = this.dIn.readUTF();
                System.out.println("");
                System.out.println("Message received: " + request);
                
                Frame frame = Frame.parseFrame(request);
                //System.out.println("Re-encoded frame : " + frame.encode()); 
                System.out.println("Frame extracted: " + frame.toString());
                
                // Adapte la réponse selon le type de paquet recu
                switch(frame.getType()){
                    case I :
                        String crc = Utils.calculateCRC(frame);
                        int[] crcArray = Utils.transformStringToBinArray(crc);
                        boolean verification = Utils.verification(Utils.calculateForCRC(frame));
                        if(verification){
                        }
                        break;
                        
                    case C :
                        System.out.println("---- RECU UNE DEMANDE DE CONNEXION ! ----");
                        break;
                        
                    case A :
                        break;
                        
                    case R :
                        break;
                        
                    case F :
                        System.out.println("---- RECU UNE DEMANDE DE FERMETURE DE CONNEXION ! ----");
                        done = true;
                        break;
                        
                    case P :
                        break;
                    
                    default:
                        done = true;
                        break;
                }
                
            } 
            catch (EOFException e) {} 
            catch (IOException e) {}
            catch (IllegalArgumentException e){
                System.out.println("Frame string is invalid : " + e.toString());
            }
        }
    }

    public void disconnectClient() {
        try {
            this.dIn.close();
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
    	
    	boolean EOF = false;
        
        if (false && !Utils.validateReceiverArgs(args)) { // 'false' pour test seulement
            System.out.println("Les arguments fournis n'ont pas le format valide ('<Numero_Port>')");
            exit(0);
        } else {
            Receiver receiver = new Receiver(Integer.parseInt("82")); //'82' à remplacer par args[0]

            if (receiver.initialize() && receiver.acceptClient()) {
            	
            	while(!EOF) {

                receiver.listenForFrames();
                
            	}
            	
                receiver.disconnectClient();
                receiver.disconnectSocket();
            }

            
        }
    }
}
