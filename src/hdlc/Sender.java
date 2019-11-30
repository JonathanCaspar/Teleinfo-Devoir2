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

    private final String host;
    private final int port;
    private final String file_name;
    private final int protocol;

    private Socket socket;
    private DataOutputStream dOut;
    private Reader fileReader;
    
    private Timer timer;
	private boolean windowFull;
	private int frameToSend;
	private int[] unAckedFrame; //Pour garder en mémoire les frames envoyés
	private int positionWindow; //Conserve la derni�re frame envoy� mais non confirm�

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
    
    public int getProtocol(){
        return this.protocol;
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
    
    // Extrait les caractères d'un fichier texte, les fragmente et génère des Frame
    public boolean readFile(){
        try {
            this.fileReader = new FileReader(this.file_name);
            this.infoFrames = new ArrayList<Frame>();

            char[] section = new char[Frame.DATA_MAX_SIZE];
            int numSeq = 0;
            
            // Tant qu'on arrive pas à la fin du fichier
            while (fileReader.read(section, 0, Frame.DATA_MAX_SIZE) != -1){
                
                // On crée la trame d'Information (FrameType.I)
                // en y insérant la section de caractère lue dans le fichier
                Frame frame = new Frame (FrameType.I, numSeq, String.valueOf(section));
                
                // Débuggage
                System.out.println("");
                System.out.println("Frame created = " + frame.toString());
              //  System.out.println("Binary form   = " + frame.encode());
                
                // On ajoute la trame créée à notre ArrayList
                this.infoFrames.add(frame);

                // On calcule le prochain numéro de séquence
                numSeq = (numSeq + 1 ) % Frame.MAX_SEQ_NUM;
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
                SocketAddress sockaddr = new InetSocketAddress(this.host, this.port);
                this.socket = new Socket();
                this.socket.connect(sockaddr);

                System.out.println("Socket connected : " + this.socket);
                this.dOut = new DataOutputStream(this.socket.getOutputStream());
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
            this.socket.close();
            this.fileReader.close();
        } catch (IOException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void generateFrameAndSend(int frameToSend, Sender sender) {
    	
    	String crcString = Utils.calculateCRC(this.infoFrames.get(frameToSend));
System.out.println(crcString);
    	this.infoFrames.get(frameToSend).computeCRC(crcString);
        		
	sender.send(this.infoFrames.get(frameToSend));	
          
    }
    
    public void startTimer() {
    	timer = new Timer();
    	timer.schedule(new Task(), 1000); //Si le sender n'a pas re�u de r�ponse du receveur apr�s 1 seconde, il d�marre un timer.
    }
    
    class Task extends TimerTask{
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
                
                // On fait une demande de connexion pour transmission de données
                Frame connectionFrame = Frame.createConnectionFrame(sender.getProtocol());
                sender.send(connectionFrame);
                
/*
//                 01111110   00000001   00000011   10101010   1101 0101 1110 1010   01111110
                String rawFrame = "01111110" + "00000000" + "00000011" + "10101010" + "1101010111101010" + "01111110";
                Frame testFrame = Frame.parseFrame(rawFrame);
                sender.send(testFrame);
 */               
                sender.windowFull = false;
                sender.frameToSend = 0;
                sender.unAckedFrame = new int[8];
                sender.positionWindow = 0;
                
                //Envoyer tant qu'il y a des frames � envoyer
                while(true){

                	if(!sender.windowFull){ //envoyer tant qu'il y a de la place dans la fenêtre

                		sender.generateFrameAndSend(sender.frameToSend, sender);
                    	
                		sender.unAckedFrame[sender.positionWindow] = sender.frameToSend++; //Conserve les éléments envoyé dans la fenêtre
                		sender.positionWindow = sender.positionWindow++ % Frame.MAX_SEQ_NUM;
                		
                		if(sender.positionWindow == sender.unAckedFrame.length){
                			sender.windowFull = true;
                			
                		}

                	}
                	else{
                		//Si on a fait un tour complet de la fen�tre sans avoir re�u de confirmation
                		if(sender.positionWindow == sender.infoFrames.get(sender.frameToSend).getNum()) { 
                			sender.startTimer();//On d�marre le chrono. Si on n'a pas re�u la prochaine frame avant la fin du chrono, on r�envoit toute la fen�tre.
                		break;
                                }
                		
                	}
                	
                }
               
                Frame closureFrame = Frame.createClosureFrame();
                sender.send(closureFrame);
                
                
                sender.disconnect();
            }
        }
    }
}