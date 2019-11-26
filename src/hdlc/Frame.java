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
    public String encodeFrame() {
        //  Utils.bitStuff( Integer.toBinaryString(FrameType.valueOf("F").ordinal()) )valeur binaire de FrameType
        return null;
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
}
