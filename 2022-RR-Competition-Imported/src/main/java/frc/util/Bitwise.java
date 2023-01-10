package frc.util;

public class Bitwise {

    /**Constructor for a number of bitwise methods. */
    public Bitwise(){

    }

    /**
     * @param bit Bit to be check if set.
     * @param num Number to check
     * @return If the bit in num is set.
     */
    public static boolean isBitSet(int num, int bit){
        if(bit < 0 || bit >31) return false;
        return (num & (1 << bit)) > 0;
    }

    /**
     * @param bit Bit to set.
     * @param num Number to be modified
     * @return Num with the bit set.
     */
    public static int setBit(int bit, int num){
        if(bit < 0 || bit >31) return num;
        return (num | (1 << bit));
    }
    
    /**
     * @param bit Bit to clear.
     * @param num Number to be modified
     * @return Num with the bit cleared.
     */
    public static int clrBit(int bit, int num){
        if(bit < 0 || bit >31) return num;
        return (num & (~(1 << bit)));
    }
    
}
