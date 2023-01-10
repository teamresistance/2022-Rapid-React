package frc.robot.testing;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.joysticks.JS_IO;
import frc.util.Timer;

public class TimerTest {
    public static Timer tstTimer = new Timer(1.0);
    public static boolean startIt = false;
    public static boolean stopIt = false;
    

    public static void init(){
        tstTimer.startTimer(5000L);
    }

    public static void update(){
        startIt = tstTimer.hasExpired();
        System.out.println("Is running update");

        sdbUpdate();
    }

    public static void sdbUpdate(){
        SmartDashboard.putBoolean("TimerTst/startIt", startIt);
        SmartDashboard.putNumber("TimerTst/Sec Left", tstTimer.getRemainingSec());
    }
    
}
