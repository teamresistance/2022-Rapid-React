package frc.robot.testing;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;

public class ShootTest {
    public static void init(){

    }

    public static void update(){
        System.out.println("Is running update");
        IO.select_low_SV.set(JS_IO.axGoalSel.get() < 0.1);      //CA 3
        IO.catapult_L_SV.set(JS_IO.btnRejectLeft.isDown());     //CB 4
        IO.catapult_R_SV.set(JS_IO.btnRejectRight.isDown());    //CB 6
        sdbUpdate();
    }

    public static void sdbUpdate(){
        SmartDashboard.putNumber("Shooter/ax 3", JS_IO.axGoalSel.get());
        SmartDashboard.putBoolean("Shooter/cat L cmd", IO.catapult_L_SV.get());
        SmartDashboard.putBoolean("Shooter/cat R cmd", IO.catapult_R_SV.get());
        SmartDashboard.putBoolean("Shooter/select Lo", IO.select_low_SV.get());
    }
    
}
