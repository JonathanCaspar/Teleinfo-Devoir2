package hdlc;

import static hdlc.Utils.CCITT;
import static hdlc.Utils.CCITTString;
import static hdlc.Utils.transformBinArrayToString;
import static hdlc.Utils.transformBinToString;
import static hdlc.Utils.transformLatinToBin;
import static hdlc.Utils.transformStringToBinArray;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Frame {

    final public static int MAX_SEQ_NUM = 8; // car 3 bits
    final public static int DATA_MAX_SIZE = 2; // 10 caractères
    
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
    
    public int getNum() {
        return (this.num);
    }
    
    public String getData() {
        return (this.data);
    }
    
    public String getCRC() {
        return (this.crc);
    }

    public void setCRC(String crc) {
        this.crc = crc;
    }
    
    public void binData(String dataLatin){
        this.data = dataLatin;
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
        StringBuilder binData = Utils.transformLatinToBin(this.data);
        
        String binDataStr = Utils.transformBinToString(binData);
        //System.out.println(this.data + " devient : " + binDataStr);
        
        
        return this.FLAG + stuffedBinType + stuffedBinNum + binDataStr + stuffedBinCrc + this.FLAG;
    }
    
    public boolean isClosureFrame(){
        return this.type == FrameType.F;
    }
    
    public static Frame createInfoFrame(int num, String data){
        return new Frame(FrameType.I, num, data);
    }
    
    public static Frame createConnectionFrame(int protocol){
        return new Frame(FrameType.C, protocol,"0");
    }
    
    public static Frame createClosureFrame(){
        return new Frame(FrameType.F, 0,"0");
    }


    // Convertit une chaine de bits (en String) en un objet Frame
    public static Frame parseFrame(String rawFrame) throws UnsupportedEncodingException {
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
        String latinData_ = Utils.transformBinToLatin(data_);
        
        //-- Extraction de Crc
        String crc_ = rawFrame.substring(frameLength - 24, frameLength - 8);

        return new Frame(type_, num_, latinData_, crc_);

    }
    
    
    private String computeCRC() {

        String array = "";

        StringBuilder binType = transformLatinToBin(this.getType() + "");
        String stringType = transformBinToString(binType);
        array += stringType;

        StringBuilder binNum = transformLatinToBin(this.getNum() + "");
        String stringNum = transformBinToString(binNum);
        array += stringNum;

        StringBuilder binData = transformLatinToBin(this.getData());
        String stringData = transformBinToString(binData);
        array += stringData;

        array += CCITTString; //Ajout du numéro de zéro de CRC-CCITT

        int[] intArray = transformStringToBinArray(array);

        int[] remainder = polynomialDivision(intArray, CCITT);

        String crcString = transformBinArrayToString(remainder);

        return crcString;

    }

    //Permet de vérifier s'il y a une erreur dans la trame avec le résultat de la division polynomiale
    public boolean checkValidity() {
        String data = "";

        StringBuilder binType = transformLatinToBin(this.getType() + "");
        String stringType = transformBinToString(binType);
        data += stringType;

        StringBuilder binNum = transformLatinToBin(this.getNum() + "");
        String stringNum = transformBinToString(binNum);
        data += stringNum;

        StringBuilder binData = transformLatinToBin(this.getData());
        String stringData = transformBinToString(binData);
        data += stringData;
        
        data += this.getCRC();

        int[] binArray = transformStringToBinArray(data);
        int[] result = polynomialDivision(binArray, CCITT);

        // Si le résultat contient uniquement des 0 alors la trame est valide
        return Utils.checkIntArrayOnlyZero(result);
    }
    
    //Permet d'obtenir le résultat d'une division polynomiale entre deux nombres binaires
    //Utilisé pour vérifier si des erreurs de transmission se sont introduites dans une transmission
    public static int[] polynomialDivision(int[] array, int[] checksum) {

        int[] result = Arrays.copyOfRange(array, 0, checksum.length);

        for (int i = 0; i < (array.length - checksum.length); i++) {

            if (result[0] == 1) {
                for (int j = 1; j < checksum.length; j++) {
                    result[j - 1] = (result[j] ^ checksum[j]);
                }
                result[result.length - 1] = array[i + checksum.length];

            } else {
                for (int j = 1; j < result.length; j++) {
                    result[j - 1] = result[j];
                }
                result[result.length - 1] = array[i + checksum.length];

            }
        }

        if (result[0] == 1) {
            for (int j = 0; j < checksum.length; j++) {
                result[j] = (result[j] ^ checksum[j]);
            }
        }

        return (Arrays.copyOfRange(result, 1, checksum.length));

    }
}
