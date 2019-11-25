package hdlc;

import java.io.*;
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
                Frame frame = Frame.parseFrame(request);
                System.out.println("Message received: " + request);
            } catch (EOFException e) {

            } catch (IOException e) {

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

    public static void checkSum(int[] data, int[] checksum) {

        int[] result = Arrays.copyOfRange(data, 0, checksum.length);
        System.out.println("First");
        for (int k = 0; k < result.length; k++) {
            System.out.println(result[k]);
        }
        for (int i = 0; i < (data.length - checksum.length); i++) {

            if (result[0] == 1) {
                for (int j = 1; j < checksum.length; j++) {
                    result[j - 1] = (result[j] ^ checksum[j]);
                }
                result[result.length - 1] = data[i + checksum.length];

                System.out.println("Suivi");
                for (int k = 0; k < result.length; k++) {
                    System.out.println(result[k]);
                }
            } else {
                for (int j = 1; j < result.length; j++) {
                    result[j - 1] = result[j];
                }
                result[result.length - 1] = data[i + checksum.length];

                System.out.println("Suivi");
                for (int k = 0; k < result.length; k++) {
                    System.out.println(result[k]);
                }
            }
        }

        if (result[0] == 1) {
            for (int j = 0; j < checksum.length; j++) {
                result[j] = (result[j] ^ checksum[j]);
            }
        }

        System.out.println("Result");
        for (int k = 0; k < result.length; k++) {
            System.out.println(result[k]);
        }

        //Vérification
        boolean verif = true;
        int index = 0;

        while ((verif == true) && (index < result.length)) {

            if (result[index] != 0) {
                verif = false;
            } else {
                index++;
            }
        }
        System.out.println(verif);

    }

    //Tranform un string binaire en un tableau contenant le nombre binaire
    public static int[] transformStringToBin(String data) {

        int[] dataArray = new int[data.length()];

        for (int i = 0; i < data.length(); i++) {
            if (data.charAt(i) == '1') {
                dataArray[i] = 1;
            } else {
                dataArray[i] = 0;
            }

        }
        return (dataArray);

    }

    //Concatène 2 tableau de int ensembl, un à la suite de l'autre
    public static void concatenateArray(int[] array1, int[] array2) {

        int[] bothArray = new int[array1.length + array2.length];

        for (int i = 0; i < array1.length; i++) {
            bothArray[i] = array1[i];

        }

        for (int i = 0; i < array2.length; i++) {
            bothArray[i + array1.length] = array2[i];

        }

        for (int i = 0; i < bothArray.length; i++) {
            System.out.println(bothArray[i]);

        }
    }

    //Vérifie si le type, le num et le data ont été erronés durant l'envoi
    /*public static void verification(FrameType type, int num, String data, String crc) {

        int[] checksum = {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1}; //x16+x12+x5+1

        int[] typeArray = transformStringtoBin(Integer.toBinaryString(receivedFrames));
        int[] numArray = transformStringtoBin(Integer.toBinaryString(receivedFrames));
        int[] crcArray = transformStringtoBin(Integer.toBinaryString(receivedFrames));

        concatenateArray(typeArray, crcArray);
        concatenateArray(numArray, crcArray);
        concatenateArray(receivedFrame, crcArray);

        checkSum(typeArray + crcArray, checksum);


    }*/
    public static void main(String[] args) throws Exception {
        /*Receiver receiver = new Receiver(80);

        if (receiver.initialize() && receiver.acceptClient()) {

            receiver.listenForFrames();
            receiver.disconnectClient();
            receiver.disconnectSocket();
        }*/
        //                 01111110   00000001   00000001   10101010   1101 0101 1110 1010   01111110
        String rawFrame = "01111110" + "00000001" + "00000110" + "10101010" + "1101010111101010" + "01111110";
        System.out.println(Frame.parseFrame(rawFrame).toString());

        /*int[] data = {1, 0, 1, 0};
        for (int i = 0; i < data.length; i++) {
            System.out.println(data[i]);
        }
        int[] checksum = {1, 0};
        for (int i = 0; i < checksum.length; i++) {
            System.out.println(checksum[i]);
        }

        checkSum(data, checksum);

        //Verification des erreurs erronés*/
    }
}
