/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hdlc;
import java.util.Random;

/**
 *
 * @author Utilisateur
 */
public class Test {
    
    //Modification de bit dans la section data du frame
    public String corruptData(String stringFrame){
        Random r = new Random();
        int random = 25 + (int)(Math.random() * ((stringFrame.length()-25 - 25) + 1));
        char[] charFrame = stringFrame.toCharArray();
        if(charFrame[random] == '1')
            charFrame[random] = '0';
        
        else
            charFrame[random] = '1';
       
        return(String.valueOf(charFrame));
    }
    
    //Pour simuler la destruction d'une frame, on change le num de la frame pour la frame suivante.
    public String destroyFrame(String frame){
        
        String number = frame.substring(16,24);
        int num = Integer.parseInt(number, 2);
        num++;
        String newNumber = Integer.toBinaryString(num);
        newNumber = Utils.bitStuff(newNumber, 8);
        System.out.println(newNumber);
        String newFrame = frame.substring(0, 16) + newNumber + frame.substring(24, frame.length());
        return (newFrame);
        
    
    }
    
    public static void main(String[] args){
        Test t = new Test();
        String frame = "011111101100011000000010110001010110011000001110110001110101010110011000101001";

        System.out.println(frame);
        System.out.println(t.destroyFrame(frame));

        
    }    
}
