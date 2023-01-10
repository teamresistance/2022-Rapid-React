package frc.io.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.joysticks.JS_IO;

public class LimeLight {
    private static NetworkTable limeTable = NetworkTableInstance.getDefault().getTable("limelight");
    private static double ledmode = 0, cammode = 0, pipeline = 1;
    private static boolean limeLightToggle = true; // Turns green light on and off so Robert is not blinded - AS
    public static int state = 0;

    public static void init() {
        limeTable = NetworkTableInstance.getDefault().getTable("limelight");
        SmartDashboard.putNumber("LL/led mode", ledmode);
        SmartDashboard.putNumber("LL/cam mode", cammode);
        SmartDashboard.putNumber("LL/pipeline", pipeline);
        state = 0;
    }

    public static boolean llHasTarget() {
        double valid = limeTable.getEntry("tv").getDouble(0);

        if (valid == 1.0) {
            return true;
        } else {
            return false;
        }
    }

    // if hastarget, -1=right, 1=left, 0=on target else 999=no target
    public static Integer llOnTarget(double db) {
        double tmpD = getLLX();
        if (llHasTarget()) {
            if (Math.abs(tmpD) > db) {
                return tmpD < 0.0 ? -1 : 1;
            }
            return 0;
        }
        return 999;
    }

    public static Integer llOnTarget() {
        return llOnTarget(2);
    }

    public static double getLLX() {
        return limeTable.getEntry("tx").getDouble(999);
    }

    public static double getLLY() {
        return limeTable.getEntry("ty").getDouble(0);
    }

    public static double getLLArea() {
        return limeTable.getEntry("ta").getDouble(0);
    }

    // default of current pipeline (0), off (1), blinking? (2), on (3)
    public static void setLED() {
        limeTable.getEntry("ledMode").setNumber(ledmode);
    }

    public static void setLEDOff(){
        limeTable.getEntry("ledMode").setNumber(1);
    }

    // set vision (0) or driver mode (1)
    public static void setCamMode() {
        limeTable.getEntry("camMode").setNumber(cammode);
    }

    public static void setPipeline() {
        limeTable.getEntry("pipeline").setNumber(pipeline);
    }

    public static void determ() {
        // if (JS_IO.limeLightOnOff.onButtonPressed()) {
            // if (limeLightToggle) {
            //     state = 1;
            //     limeLightToggle = !limeLightToggle;
            // } else {
            //     state = 0;
            //     limeLightToggle = !limeLightToggle;
            // }

        //     state = limeLightToggle ? 1 : 0;
        //     limeLightToggle = !limeLightToggle;
        // }
    }

    public static void update() {
        determ();
        switch (state) {
            // LED's are off
            case 0:
                ledmode = 0;
                setLEDOff();
                sdbUpdate();
                break;
            // LED's are on and LimeLight is doing its thing
            case 1:
                ledmode = SmartDashboard.getNumber("led mode", ledmode);
                setLED();
                sdbUpdate();
                break;
            default:
                sdbUpdate();
                break;
        }
    }

    public static void sdbUpdate() {
        getLLX();
        SmartDashboard.putBoolean("LL/has target", llHasTarget());
        SmartDashboard.putNumber("LL/x offset", getLLX());
        SmartDashboard.putNumber("LL/y offset", getLLY());
        SmartDashboard.putNumber("LL/percent area", getLLArea() * 100);
        SmartDashboard.putNumber("LL/state", state);

        cammode = SmartDashboard.getNumber("LL/cam mode", cammode);
        setCamMode();
        pipeline = SmartDashboard.getNumber("LL/pipeline", pipeline);
        setPipeline();

    }
}
