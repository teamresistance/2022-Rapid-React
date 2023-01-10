package frc.util;
/**
 * Handles 2 dimensional arrays
 */
public class Array2d {

    /**
     * Adds a 2-dimensional array to the 2-d source array.
     * 
     * @param srcArr - Array to be added to.
     * @param addArr - Array to be added to the src
     * @return - combination
     */
    public static double[][] addArr(double[][] srcArr, double[][] addArr) {
        int srcLen = srcArr.length;         //Length of the src
        int addLen = addArr.length;         //Lenght of the addition
        int totLen = srcLen + addLen;       //Total of return array
        int itmLen = Math.max(srcLen == 0 ? 0 : srcArr[0].length, addLen == 0 ? 0 : addArr[0].length);  //Needed for null arrays
        double[][] tmpArr = new double[totLen][itmLen];     //Define array

        for(int i = 0; i < srcLen; i++){                    //Copy src array
            for(int j = 0; j < itmLen; j++){
                tmpArr[i][j] = srcArr[i][j];
            }
        }

        for(int i = 0; i < addLen; i++){                    //Add add array
            for(int j = 0; j < itmLen; j++){
                tmpArr[srcLen + i][j] = addArr[i][j];
            }
        }
        return tmpArr;
    }
}
