/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hdlc;

/**
 *
 * @author Jonathan
 */
public class Frame {
    
    final private   String      flag = "01111110";
    private         FrameType    type;
    private         int         num;
    private         String      data;
    private         String      crc;
    
    public Frame(FrameType type, int num, String data, String crc) {
        this.type = type;
        this.num = num;
        this.data = data;
        this.crc = crc;
    }
    
    public FrameType getType(){
        return (this.type != null) ? this.type : null;
    }
    
    public void computeCRC(){
    }
    
    // Retourne une version affichable de la trame
    public String toString(){
        return null;
    }
    
     // Retourne une chaine de bits (en String) représentant la trame
    public String encodeFrame(){
        return null;
    }
    
    // Convertit une chaine de bits (en String) en un objet Frame
    public static Frame parseFrame(String rawFrame){
        return null;
    }
}

