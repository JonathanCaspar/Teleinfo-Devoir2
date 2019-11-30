package hdlc;

import java.io.UnsupportedEncodingException;
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
        this.crc = null;
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

    public void computeCRC(String crc) {
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
        String data = Utils.transformBinToString(binData);
        
        
        return this.FLAG + stuffedBinType + stuffedBinNum + data + stuffedBinCrc + this.FLAG;
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
}
