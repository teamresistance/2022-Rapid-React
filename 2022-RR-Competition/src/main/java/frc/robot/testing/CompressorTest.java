package frc.robot.testing;

import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Relay;


public class CompressorTest {

    public static void init(){
       
    }

    public static void update(){
        // Pneu. system has leak. Dont need it when testing drive.
        IO.compressorRelay.set(JS_IO.btnFire.isDown() ? Relay.Value.kForward : Relay.Value.kOff);
    }

    public static void sdbUpdate(){
        SmartDashboard.putBoolean("Cmpr/Enable", IO.compressor1.enabled());
        SmartDashboard.putBoolean("Cmpr/Relay", 
            (IO.compressorRelay.get() == Relay.Value.kForward ? true : false));
    }

}
