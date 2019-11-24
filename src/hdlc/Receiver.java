package hdlc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver {
    
    private Frame[] receivedFrames;
    
    private final int port;
    
    private ServerSocket sSocket;
    private Socket socket;
    private DataInputStream dIn;

    public Receiver(int port) {
       this.port = port;
    }
    
    public boolean initialize(){
        try {
            this.sSocket = new ServerSocket(port);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public boolean acceptClient(){
        try {
            this.socket = this.sSocket.accept();
            this.dIn = new DataInputStream(this.socket.getInputStream());
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void listenForFrames(){
        boolean done = false;
        while(!done) {
          
          // On crée une Trame à partir de la suite reçue et on lit le type de message
          try{
            String request = this.dIn.readUTF();
            Frame frame = Frame.parseFrame(request);
            System.out.println("Message received: " + request);
          } 
          catch (EOFException e){
              
          }
          catch(IOException e){
              
          }
        }
    }
    
    public void disconnectClient(){
        try {
            this.dIn.close();
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void disconnectSocket(){
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        Receiver receiver = new Receiver(80);
        
        if(receiver.initialize() && receiver.acceptClient()){
           
            receiver.listenForFrames();
            receiver.disconnectClient();
            receiver.disconnectSocket();
        }
   }
}
