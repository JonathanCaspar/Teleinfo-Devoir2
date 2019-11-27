package hdlc;

import java.util.Arrays;

public class Frame {

    final public static int MAX_SEQ_NUM = 8; // car 3 bits
    final public static int DATA_MAX_SIZE = 20; // 10 caractères
    
    final private String FLAG = "01111110";
    private FrameType type;
    private int num;
    private String data;
    private String crc;

    public Frame(FrameType type, int num, String data) {
        this.type = type;
        this.num = num;
        this.data = data;
        this.crc = this.computeCRC();
    }
    
    public Frame(FrameType type, int num, String data, String crc) {
        this.type = type;
        this.num = num;
        this.data = data;
        this.crc = crc;
    }

    public FrameType getType() {
        return (this.type != null) ? this.type : null;
    }

    public String computeCRC() {
        return "0";
    }

    // Retourne une version affichable de la trame
    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s", FLAG, type, num, data, crc, FLAG);
    }

    // Retourne une chaine de bits (en String) représentant la trame
    public String encode() {
        String stuffedBinType = Utils.bitStuff( Integer.toBinaryString(this.type.ordinal()), 8); // valeur binaire de FrameType
        String stuffedBinNum  = Utils.bitStuff( Integer.toBinaryString(this.num), 8);
        String stuffedBinCrc  = Utils.bitStuff( this.crc, 16);
        
        return this.FLAG + stuffedBinType + stuffedBinNum + this.data + stuffedBinCrc + this.FLAG;
    }
    
    public boolean isClosureFrame(){
        return this.type == FrameType.F;
    }
    
    public static Frame createConnectionFrame(int protocol){
        return new Frame(FrameType.C, protocol,"0","0");
    }
    
    public static Frame createClosureFrame(){
        return new Frame(FrameType.F, 0,"0","0");
    }


    // Convertit une chaine de bits (en String) en un objet Frame
    public static Frame parseFrame(String rawFrame) {
        // Vérification
        int frameLength = rawFrame.length();
        if (!rawFrame.matches(Utils.binaryRegex)) throw new IllegalArgumentException("Frame string must be binary numbers ONLY!");
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
        if (num_ < 0 || num_ >= MAX_SEQ_NUM) {
            throw new IllegalArgumentException("Frame number cannot exceed " + MAX_SEQ_NUM + " !");
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

}
