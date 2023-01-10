package frc.robot.testing;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


public class SnorfTest {
    public static void init(){
       
    }

    public static void update(){
        IO.snorfFeed_Mtr.set(-JS_IO.axLeftY.get());
        IO.snorfElv_Mtrs.set(-JS_IO.axRightY.get());

        IO.snorflerExt_SV.set(JS_IO.btnSnorfle.isDown());   //CB 3
    }

}
