package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.Encoder;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.ISolenoid;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.Button;
import frc.util.Timer;

public class Climber {
    // Variables for states
    private static int state; // Climber state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    // Climber variables
    private static double move = 0;
    private static boolean brake = true;

    /**
     * 0 = Off
     * 1 = On
     * 2 = Reverse
     */
    private enum motorRotations {
        Off,
        Forward,
        Reverse
    }

    private static motorRotations mtrRot = motorRotations.Off;
    
    private static double rotationalSpeed = 70.0;
    private static double mtrSpd = 0;

    private static ISolenoid slider = IO.slider;
    private static ISolenoid lockPinA = IO.lockPinA;
    private static ISolenoid lockPinB = IO.lockPinB;

    // Button Variables
    private static Button buttonClimb1 = JS_IO.btnClimb1;
    private static Button buttonClimb2 = JS_IO.btnClimb2;

    // Statemachine
    private static int stateNum = 0;

    private static boolean climbEnabled = false;

    public static void init() {
        sdbInit();
        cmdUpdate(0.0, motorRotations.Off, false, false, false, false);
        state = 0;
    }

    public static void smUpdate() { // State Machine Update

        switch (state) {
            case 0: // Turns everything off
                cmdUpdate(0.0, motorRotations.Off, false, false, false, false);
                break;
            case 1:
                cmdUpdate(0.0, motorRotations.Forward, false, false, true, false); // make motor rotation a enumaration.
                break;
            default:// Default state
                break;

        }
    }

    public static void sdbInit() {

    }

    private static void update() {
        climbEnabled = (buttonClimb1.isDown() && buttonClimb2.isDown()) ? true : false;
        if (climbEnabled) state = 1;

        // Sets mtrSpd based on the value of mtrRot
        switch (mtrRot) {
            case Off: // Off
                mtrSpd = 0.0;
                break;
            case Forward: // Forward
                mtrSpd = rotationalSpeed;
                break;
            case Reverse: // Reverse
                mtrSpd = -rotationalSpeed;
                break;
            default: //Off
                mtrSpd = 0.0;
                break;
        }

        smUpdate();
        sdbUpdate();
    }

    public static void sdbUpdate() {
        SmartDashboard.putString("Climber/mtrRot", mtrRot.toString());
        SmartDashboard.putBoolean("Climber/brake", brake);
        SmartDashboard.putBoolean("Climber/lockPinA", lockPinA.get());
        SmartDashboard.putBoolean("Climber/lockPinB", lockPinB.get());
        SmartDashboard.putBoolean("Climber/slider", slider.get());
        SmartDashboard.putBoolean("Climber/buttonClimb1", buttonClimb1.isDown());
        SmartDashboard.putBoolean("Climber/buttonClimb2", buttonClimb2.isDown());
        SmartDashboard.putBoolean("Climber/climbEnabled", climbEnabled);

    }

    // COMMAND UPDATE
    public static void cmdUpdate(double moveVar, motorRotations mtrRotVar, boolean brakeVar, boolean lockPinAVar,
            boolean lockPinBVar, boolean sliderVar) {
        move = moveVar;
        mtrRot = mtrRotVar;
        brake = brakeVar;
        lockPinA.set(lockPinAVar);
        lockPinB.set(lockPinBVar);
        slider.set(sliderVar);
        // ----------------- Shooter statuses and misc.
    }

    /**
     * Probably shouldn't use this bc the states can change. Use statuses.
     * 
     * @return - present state of Shooter state machine.
     */
    public static int getState() {
        return state;
    }

}
