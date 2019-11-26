package hdlc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Utils {
    final public static String binaryRegex = "^\\b[01]+\\b$";
    
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
    public static int[] concatenate2Array(int[] array1, int[] array2) {

        int[] bothArray = new int[array1.length + array2.length];

        for (int i = 0; i < array1.length; i++) {
            bothArray[i] = array1[i];

        }

        for (int i = 0; i < array2.length; i++) {
            bothArray[i + array1.length] = array2[i];

        }

        return (bothArray);
    }

    //Transforme un string de données en un string binaire
    public static byte[] transformLatinToBin(String data) {

        Charset iso88591charset = Charset.forName("ISO-8859-1");

        byte[] bytes = data.getBytes(iso88591charset);

        StringBuilder binary = new StringBuilder();

        for (byte b : bytes){

            int val = b;
            for (int i = 0; i < 8; i++){
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');
          }


        String text = new String(bytes, iso88591charset);
       
    return(bytes);

    }

    public static String transformBinToLatin(byte[] bin) throws UnsupportedEncodingException{
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        String text = new String(bin,iso88591charset);
        return(text);
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

    //Tranform un string binaire en un tableau contenant le nombre binaire
    //Retourne un tableau de int contenant les bits
    public static int[] binStringToArray(String data) {

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
}
