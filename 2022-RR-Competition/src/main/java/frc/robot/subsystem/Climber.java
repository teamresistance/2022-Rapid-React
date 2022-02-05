package frc.robot.subsystem;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.Encoder;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.ISolenoid;
import frc.io.hdw_io.InvertibleDigitalInput;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.Button;
import frc.util.Timer;
import frc.robot.subsystem.drive.Drive;

public class Climber {
    // Variables for states
    private static int state; // Climber state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    // Climber variables
    private static double move = 0;

    private enum motorRotationsDirection {
        Off,
        Forward,
        Reverse
    }
    private static boolean lockPinASaved;
    private static boolean lockPinBSaved;
    private static boolean sliderSaved; 
    private static motorRotationsDirection mtrRot = motorRotationsDirection.Off;
    
    private static double rotationalSpeed = 0.0;
    private static double mtrSpd = 0;
    private static WPI_TalonSRX climberMotor = IO.climbMotor;

    private static ISolenoid slider = IO.slider;
    private static ISolenoid lockPinA = IO.lockPinA;
    private static ISolenoid lockPinB = IO.lockPinB;
    private static ISolenoid brakeRel;
    private static InvertibleDigitalInput lockPinAExt_FB = IO.lockPinAExt_FB;
    private static InvertibleDigitalInput lockPinARet_FB = IO.lockPinARet_FB; 
    private static InvertibleDigitalInput lockPinBExt_FB = IO.lockPinBExt_FB;
    private static InvertibleDigitalInput lockPinBRet_FB = IO.lockPinBRet_FB;
    private static InvertibleDigitalInput sliderExt_FB   = IO.sliderExt_FB; 
    private static InvertibleDigitalInput sliderRet_FB   = IO.sliderRet_FB;
    

    // Button Variable
    private static Button buttonClimb1 = JS_IO.btnClimb1;
    private static Button buttonClimb2 = JS_IO.btnClimb2;

    // Statemachine
    private static int stateNum = 0;

    private static boolean climbEnabled = false;

    public static void init() {
        sdbInit();
        rotationalSpeed = 70.0;
        cmdUpdate(0.0, motorRotationsDirection.Off, false, false, false);
        state = 0;
    }

    public static void smUpdate() { // State Machine Update
        
        switch (state) {
            case 0: // Turns everything off
                cmdUpdate(0.0, motorRotationsDirection.Off, false, false, false );
                break;
            case 1:
                cmdUpdate(0.0, motorRotationsDirection.Reverse, false, false, true ); // make motor rotation a enumaration.
                break;
            case 2:
                cmdUpdate(-rotationalSpeed, motorRotationsDirection.Off, false,false,true);
                break;
            case 3:
                cmdUpdate(-rotationalSpeed * 4/7, motorRotationsDirection.Forward, false, false, true); // make motor rotation a enumaration.
                break;
            case 4:
                cmdUpdate(0.0, motorRotationsDirection.Off, false, true, false);
                break;
            case 5:
                cmdUpdate(0.0, motorRotationsDirection.Forward, false, false, true); // make motor rotation a enumaration.
                break;
            case 90:
                cmdUpdate(0.0, motorRotationsDirection.Off, lockPinASaved, lockPinBSaved, sliderSaved);
            default:// Default state
                break;
        }

    }

    public static void sdbInit() {

    }

    private static void update() {
        climbEnabled = (buttonClimb1.isDown() && buttonClimb2.isDown()) ? true : false;
        if (climbEnabled) state = 1;

        smUpdate();
        sdbUpdate();
    }

    public static void sdbUpdate() {
        SmartDashboard.putString("Climber/mtrRot", mtrRot.toString());
        SmartDashboard.putBoolean("Climber/brake", brakeRel.get());
        SmartDashboard.putBoolean("Climber/lockPinA", lockPinA.get());
        SmartDashboard.putBoolean("Climber/lockPinB", lockPinB.get());
        SmartDashboard.putBoolean("Climber/slider", slider.get());
        SmartDashboard.putBoolean("Climber/buttonClimb1", buttonClimb1.isDown());
        SmartDashboard.putBoolean("Climber/buttonClimb2", buttonClimb2.isDown());
        SmartDashboard.putBoolean("Climber/climbEnabled", climbEnabled);
    }

    // COMMAND UPDATE
    /**
     * @param moveVar      - Speed of drive motors
     * @param mtrRotVar    - Motor rotation Off | Forward| Reverse
     * @param lockPinAVar  - False = LockpPinAExtend | True = LockPinARetract
     * @param lockPinBVar  - False = LockpPinBExtend | True = LockPinBRetract
     * @param sliderVar    - False = Extend | True = Retract

     */
    public static void cmdUpdate(double moveVar, motorRotationsDirection mtrRotVar, boolean lockPinAVar,
            boolean lockPinBVar, boolean sliderVar) {
        boolean lockPinASaved = lockPinAVar;
        boolean lockPinBSaved = lockPinBVar;
        boolean sliderSaved = sliderVar;
        if (checkLockPinsOK() == false) mtrRotVar = motorRotationsDirection.Off;
        // ----------------- Shooter statuses and misc.

        Drive.cmdUpdate(0.0, 0.0, false, 2);
        
        switch (mtrRot) {
            case Off: 
                climberMotor.set(0.0);
                brakeRel.set(false);
                break;
            case Forward: 
                climberMotor.set(rotationalSpeed);
                brakeRel.set(true);
                break;
            case Reverse:
                climberMotor.set(-rotationalSpeed);
                brakeRel.set(true);
                break;
        }

        lockPinA.set(lockPinAVar);
        lockPinB.set(lockPinBVar);
        slider.set(sliderVar);
        
    }
 

     /**
      * @return true if lockingPins and slider are all matching
      */
    private static boolean checkLockPinsOK(){
        return !(lockPinA.get() ^ lockPinAExt_FB.get() ||
                 lockPinB.get() ^ lockPinBExt_FB.get() ||
                 slider.get() ^ sliderExt_FB.get());
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
