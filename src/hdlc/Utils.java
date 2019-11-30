package hdlc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Utils {
    final public static String binaryRegex = "^\\b[01]+\\b$";
    final public static int[] CCITT = {1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1};
    final public static String CCITTString = "0000000000000000";
    
    // Vérifie si les arguments args.length passés en paramètres sont valides
    public static boolean validateSenderArgs(String[] args) {
        if (args.length != 4 ) return false;
        else if (!args[0].matches("^[\\w\\.]+$")) return false; // machine doit être un String
        else if (!args[1].matches("^\\d+$")) return false; // num port doit être un nombre
        else if (!args[2].matches("^\\w+\\.\\w+$")) return false; // fichier doit être un valide (xxx.xxx)
        else if (!args[3].matches("^\\d{1}$")) return false; // protocole doit un seul chiffre
        else return true;
    }
    
    // Vérifie si les arguments args.length passés en paramètres sont valides (A COMPLETER)
    public static boolean validateReceiverArgs(String[] args) {
        if (args.length != 1 ) return false;
        else if (!args[0].matches("^\\d+$")) return false; // num port doit être un nombre
        else return true;
    }
    
    // Retourne une chaine à laquelle on a ajouté des 0 (à gauche) jusqu'à obtenir une chaine finale de taille "maxSize"
    public static String bitStuff(String bits, int maxSize) {
        if (bits.length() < maxSize) {
            String stuffedBits = bits;
            for (int i = 0; i < maxSize - bits.length(); i++) {
                stuffedBits = "0" + stuffedBits;
            }
            return stuffedBits;
        } else {
            return bits;
        }
    }

    
    //Tranform un string binaire en un tableau contenant le nombre binaire
    public static int[] transformStringToBinArray(String data) {

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

    public static String transformBinArrayToString(int[] data) {

        String dataString = "";

        for (int i = 0; i < data.length; i++) {
            dataString += data[i];
        }
        return (dataString);

    }

    
    //Concatène 2 tableau de int ensembl, un à la suite de l'autre
    public static int[] concatenate3Array(int[] array1, int[] array2, int[] array3) {

        int[] array = new int[array1.length + array2.length + array3.length];

        for (int i = 0; i < array1.length; i++) {
            array[i] = array1[i];

        }

        for (int i = 0; i < array2.length; i++) {
            array[i + array1.length] = array2[i];

        }

         for (int i = 0; i < array3.length; i++) {
            array[i + array1.length + array3.length] = array3[i];

        }

        return (array);
    }

    //Transform un StringBuilder de code binaire en un string que l'on peut envoyer
     public static String transformBinToString(StringBuilder data) {

        String dataString = "";

        for (int i = 0; i < data.length(); i++) {
            dataString += data.charAt(i);

        }
        return (dataString);

    }

    //Transforme un string de texte en latin en un string binaire
    public static StringBuilder transformLatinToBin(String data) {

        Charset iso88591charset = Charset.forName("ISO-8859-1");

        byte[] bytes = data.getBytes(iso88591charset);

        StringBuilder binary = new StringBuilder();

        for (byte b : bytes){

            int val = b;
            for (int i = 0; i < 8; i++){
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
          }

       
    return(binary);

    }

    //Transforme un string binaire en un texte en latin
    public static String transformBinToLatin(String data) throws UnsupportedEncodingException{
        
        String s = "";
        for(int index = 0; index < data.length(); index+=8) {
             String temp = data.substring(index, index+8);
             int num = Integer.parseInt(temp,2);
             char letter = (char) num;
             s = s+letter;
        }

        return(s);
    }
    
    //Permet de vérifier s'il y a une erreur dans la trame avec le résultat de la division polynomiale
    public static boolean verification(int[] result){
		boolean verif = true;
        int index = 0;

        while ((verif == true) && (index < result.length)) {

            if (result[index] != 0) {
                verif = false;
            } else {
                index++;
            }
        }
        return (verif);

    }
    
    //Permet d'obtenir le résultat d'une division polynomiale entre deux nombre binaire
//Utilisé pour créer le CRC (sender) et pour vérifier si des erreurs se sont intégré (receiver)
    public static int[] checkSum(int[] data, int[] checksum) {

        int[] result = Arrays.copyOfRange(data, 0, checksum.length);

        for (int i = 0; i < (data.length - checksum.length); i++) {

            if (result[0] == 1) {
                for (int j = 1; j < checksum.length; j++) {
                    result[j - 1] = (result[j] ^ checksum[j]);
                }
                result[result.length - 1] = data[i + checksum.length];

            } else {
                for (int j = 1; j < result.length; j++) {
                    result[j - 1] = result[j];
                }
                result[result.length - 1] = data[i + checksum.length];

            }
        }

        if (result[0] == 1) {
            for (int j = 0; j < checksum.length; j++) {
                result[j] = (result[j] ^ checksum[j]);
            }
        }

        return(Arrays.copyOfRange(result, 1, checksum.length));

    }

    public static String calculateCRC(Frame frame){

        String data  = "";

        StringBuilder binType = transformLatinToBin(frame.getType() + "");
        String stringType = transformBinToString(binType);
        data += stringType;

        StringBuilder binNum = transformLatinToBin(frame.getNum() + "");
        String stringNum = transformBinToString(binNum);
        data += stringNum;

        StringBuilder binData = transformLatinToBin(frame.getData());
        String stringData = transformBinToString(binData);
        data += stringData;

        data += CCITTString; //Ajout du numéro de zéro de CRC-CCITT

        int[] intData = transformStringToBinArray(data);

        int[] checksum = checkSum(intData, CCITT);

        String crcString = transformBinArrayToString(checksum);

        return(crcString);

    }
    
    public static int[] calculateForCRC(Frame frame){
        String data  = "";

        StringBuilder binType = transformLatinToBin(frame.getType() + "");
        String stringType = transformBinToString(binType);
        data += stringType;

        StringBuilder binNum = transformLatinToBin(frame.getNum() + "");
        String stringNum = transformBinToString(binNum);
        data += stringNum;

        StringBuilder binData = transformLatinToBin(frame.getData());
        String stringData = transformBinToString(binData);
        data += stringData;
        
        data += frame.getCRC();
        
        int[] binArray = transformStringToBinArray(data);
        int[] result = checkSum(binArray, CCITT);
        
        return(result);
    }
    
}
