package hdlc;

import java.io.*;
import static java.lang.System.exit;
import java.net.*;


/*
   Agit en tant que SocketClient : tente de se connecter à un serveur
   Prend des arguments de la forme "<Nom_Machine> <Numero_Port> <Nom_fichier> <0>"
*/
public class Sender {
    
    private Frame[] sentFrames;
    private static int max_tries = 3;

    // Valeurs par défaut
    private static String host = "127.0.0.1"; //localhost
    private static int port = 8080;
    private static String file_name = null;
    private static int protocol = 0;
    
    
    // Vérifie si les argumeargs.lengthnts passés en paramètres sont valides
    public static boolean validateArgs(String[] args){
        if(args.length > 4) return false;
        // reste autre arguments
        return true;
    }


    public static void main(String[] args) throws Exception {
        
        // On fait une validation des arguments
        if(!validateArgs(args)){
            System.out.println("Les arguments fournis n'ont pas le format valide ('<Nom_Machine> <Numero_Port> <Nom_fichier> <0>')");
            exit(0);
        }
        
        host = args[0];
        port = Integer.parseInt(args[1]);
        file_name = args[2];
        protocol = Integer.parseInt(args[3]);
        
        // Tente de se connecter plusieurs fois jusqu'à ce que le serveur soit lancé
        int try_attempt = 0;
        Socket socket = new Socket();
        
        while(try_attempt <= max_tries && !socket.isConnected()){
            try {
                SocketAddress sockaddr = new InetSocketAddress(host, port);
                socket = new Socket();
                
                // Connects this socket to the server with a specified timeout value
                // If timeout occurs, SocketTimeoutException is thrown
                socket.connect(sockaddr);

                System.out.println("Socket connected..." + socket);

            }
            catch (Exception e) {
                
                if(try_attempt == max_tries){
                    System.out.println("Server was not found. Ending the program.");
                    return;
                }
                else{
                    System.out.println("Attempt #" + (try_attempt+1) + " : I/O Error " + e.getMessage() + ". Trying again ...");
                    Thread.sleep(500);
                    try_attempt++;
                }
            }
        }
        
        // Si la connection a fonctionné : on peut envoyer des données
        if(socket.isConnected()){
            BufferedReader plec = new BufferedReader(
                                   new InputStreamReader(socket.getInputStream())
                                   );

            PrintWriter pred = new PrintWriter(
                                 new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())),
                                 true);

            String str = "bonjour";
            for (int i = 0; i < 10; i++) {
               pred.println(str);          // envoi d'un message
               str = plec.readLine();      // lecture de l'écho
            }
            System.out.println("END");     // message de terminaison
            pred.println("END") ;
            plec.close();
            pred.close();
            socket.close();
        }
   }
}
