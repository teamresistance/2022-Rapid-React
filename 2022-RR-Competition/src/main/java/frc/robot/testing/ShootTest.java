package frc.robot.testing;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;

public class ShootTest {
    public static void init(){

    }

    public static void update(){
        IO.select_low_SV.set(JS_IO.axGoalSel.get() < 0.1);      //CA 3
        IO.catapult_L_SV.set(JS_IO.btnRejectLeft.isDown());     //CB 4
        IO.catapult_R_SV.set(JS_IO.btnRejectRight.isDown());    //CB 6
    }
    
}
