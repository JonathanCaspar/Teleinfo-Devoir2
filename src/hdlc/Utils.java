package hdlc;

public class Utils {
    
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
}
