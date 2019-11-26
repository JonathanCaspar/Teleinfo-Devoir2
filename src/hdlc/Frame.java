package hdlc;

public class Frame {

    final private static int numMaxBit = 3;
    final private static String binaryRegex = "^\\b[01]+\\b$";
    
    final private String flag = "01111110";
    private FrameType type;
    private int num;
    private String data;
    private String crc;

    public Frame(FrameType type, int num, String data, String crc) {
        this.type = type;
        this.num = num;
        this.data = data;
        this.crc = crc;
    }

    public FrameType getType() {
        return (this.type != null) ? this.type : null;
    }

    public void computeCRC() {
    }

    // Retourne une version affichable de la trame
    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s", flag, type, num, data, crc, flag);
    }

    // Retourne une chaine de bits (en String) représentant la trame
    public String encode() {
        String stuffedBinType = Utils.bitStuff( Integer.toBinaryString(this.type.ordinal()), 8); // valeur binaire de FrameType
        String stuffedBinNum  = Utils.bitStuff( Integer.toBinaryString(this.num), 8);
        String stuffedBinCrc  = Utils.bitStuff( this.crc, 16);
        
        return this.flag + stuffedBinType + stuffedBinNum + this.data + stuffedBinCrc + this.flag;
    }



    // Convertit une chaine de bits (en String) en un objet Frame
    public static Frame parseFrame(String rawFrame) {
        // Vérification
        int frameLength = rawFrame.length();
        if (!rawFrame.matches(binaryRegex)) throw new IllegalArgumentException("Frame string must be binary numbers ONLY!");
        if (frameLength < 48) {
            throw new IllegalArgumentException("Frame string is too short!"); //taille minimale requise (4 octets + 2 octets crc)
        }
        if (!rawFrame.substring(0, 8).equals(rawFrame.substring(frameLength - 8, frameLength))) {
            throw new IllegalArgumentException("Beginning and end flags are not equals!");
        }

        //-- Extraction du type et conversion vers Enum FrameType
        int typeBinaryNum = Integer.parseInt(rawFrame.substring(8, 16).trim(), 2);

        FrameType type_ = null;
        if (typeBinaryNum < FrameType.values().length) {
            // Index du type existe
            type_ = FrameType.values()[typeBinaryNum];
        } else {
            throw new IllegalArgumentException("Given type doesn't exist!");
        }

        //-- Extraction de Num
        int num_ = Integer.parseInt(rawFrame.substring(16, 24), 2);

        // On vérifie que le numéro ne dépasse pas la limite (3 bits dans l'énoncé)
        if (num_ <= 0 || num_ >= Math.pow(2, numMaxBit)) {
            throw new IllegalArgumentException("Frame number cannot exceed " + numMaxBit + " bits capacity!");
        }

        //-- Extraction de Data
        String data_ = rawFrame.substring(24, frameLength - 24);

        //-- Extraction de Crc
        String crc_ = rawFrame.substring(frameLength - 24, frameLength - 8);

        return new Frame(type_, num_, data_, crc_);

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

        return(result);

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
}
