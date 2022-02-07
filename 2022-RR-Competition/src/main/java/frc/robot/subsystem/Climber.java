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
  

    // Hardware Definitions
    private static eMtrRotDir mtrRot = eMtrRotDir.OFF; 
    
    private static double rotationalSpeed = 0.0;
    private static double mtrSpd = 0;
    private static WPI_TalonSRX climberMotor = IO.climbMotor;

    private static ISolenoid slider = IO.slider;
    private static ISolenoid lockPinAExt = IO.lockPinAExt_SV;
    private static ISolenoid lockPinARet = IO.lockPinARet_SV;
    private static ISolenoid lockPinB = IO.lockPinB;
    private static ISolenoid brakeRel;

    private static InvertibleDigitalInput lockPinAExt_FB = IO.lockPinAExt_FB;
    private static InvertibleDigitalInput lockPinARet_FB = IO.lockPinARet_FB; 
    private static InvertibleDigitalInput lockPinBExt_FB = IO.lockPinBExt_FB;
    private static InvertibleDigitalInput lockPinBRet_FB = IO.lockPinBRet_FB;
    private static InvertibleDigitalInput sliderExt_FB   = IO.sliderExt_FB; 
    private static InvertibleDigitalInput sliderRet_FB   = IO.sliderRet_FB;
    

    // Joystick Buttons
    private static Button buttonClimb1 = JS_IO.btnClimb1;
    private static Button buttonClimb2 = JS_IO.btnClimb2;

    // Climber variables
    private enum eMtrRotDir {
        OFF, // Rotational Motor speed is 0.0
        FWD, // Rotational Motor speed is +
        REV  // Rotatioanl Motor speed is -
    }
    private static boolean lockPinASaved; // Used in case 90, emerg.-stop
    private static boolean lockPinBSaved; // Used in case 90, emerg.-stop
    private static boolean sliderSaved;  // Used in case 90, emerg.-stop
    private static int state; // Climber state machine. 0=Off by pct, 1=On by velocity, RPM
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    // Statemachine
    private static int SavedState;

    private static boolean climbEnabled = false; //Both Joysick buttons pressed

    public static void init() {
        sdbInit();
        rotationalSpeed = 70.0;
        cmdUpdate(0.0, eMtrRotDir.OFF, false, false, false);
        state = 0;
    }

    /**
     * State Machine for climber
     */
    public static void smUpdate() { // State Machine Update
        
        switch (state) {
            case 0: // Turns everything off
                cmdUpdate(0.0, eMtrRotDir.OFF, false, false, false );
                stateTmr.hasExpired(0, state);
                break;
            case 1:
                cmdUpdate(0.0, eMtrRotDir.REV, false, false, true ); 
                IO.drvFeetRst();
                break;
            case 2: // Move backwards 6' to start climb position
                cmdUpdate(-0.7, eMtrRotDir.OFF, false,false,true);
                if (IO.drvFeet() < -6.0) state++;
                break;
            case 3: // Move backwards 3' until robot pitches 15 degrees fwd/down
                cmdUpdate(-0.4, eMtrRotDir.OFF, false, false, true); 
                // TODO: Robot pitch >15, state++
                break;
            case 4: // Stop the rotation of the motor and grab bar with LockPinA
                cmdUpdate(0.0, eMtrRotDir.OFF, true, false, true);
                if (lockPinAExt_FB.get() == true) state++;
                IO.drvFeetRst();
                break;
            case 5: // Robot moves forward 3 feet and arm starts rotating forward
                cmdUpdate(0.2, eMtrRotDir.FWD, true, false, true); 
                if (IO.drvFeet() > 3.0) state++;
                break;
            case 6: // Stop forward wheel motion
                cmdUpdate(0.0, eMtrRotDir.FWD, true, false, true);
                state++;
                break;
            case 7: // Continue arm rotation until pitch hits x degrees
                cmdUpdate(0.0, eMtrRotDir.FWD, true, false, true);
                // TODO: Robot pitch stuff
                state++;
                break;
            case 8: // Latch onto second bar with Pin B. Stop rotation.
                cmdUpdate(0.0, eMtrRotDir.OFF, true, true, true);
                if (lockPinBExt_FB.get() == true) state++;
                break;
            case 9: // Open Pin A in order to release the first bar
                cmdUpdate(0.0, eMtrRotDir.OFF, false, true, true);
                if (lockPinARet_FB.get() == true) state++;
                break;
            case 10: // Rotating upward/REV
                cmdUpdate(0.0, eMtrRotDir.REV, false, true, true);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 11: // Retract the slider arm
                cmdUpdate(0.0, eMtrRotDir.REV, false, true, false);
                if (sliderRet_FB.get() == true) state++;
                break;
            case 12: // Motor Union Break Timer
                cmdUpdate(0.0, eMtrRotDir.OFF, false, true, false);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 13: // Go forward towards transversal bar
                cmdUpdate(0.0, eMtrRotDir.FWD, false, true, false);
                if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            case 14: // Keep going forward, extend the slider
                cmdUpdate(0.0, eMtrRotDir.FWD, false, true, true);
                state++;
                break;
            case 15: // Keep going till we pitch x
                cmdUpdate(0.0, eMtrRotDir.FWD, false, true, true);
                // TODO: keep going until robot hits pitch x
                state++;
                break;
            case 16: // latch the transversal stop rotating.
                cmdUpdate(0.0, eMtrRotDir.FWD, true, true, true);
                if (lockPinAExt_FB.get() == true) state++;
                break;
            case 17: // Motor Union Break
                cmdUpdate(0.0, eMtrRotDir.OFF, true, true, true);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 18: // Release the B pin
                cmdUpdate(0.0, eMtrRotDir.OFF, true, false, true);
                if (lockPinBRet_FB.get() == true) state++;
                break;
            case 19: // Rotating Reverse to give leeway
                cmdUpdate(0.0, eMtrRotDir.REV, true, false, true);
                if (stateTmr.hasExpired(0.3, state)) state++;
                break;
            case 20: // Turn arm motor off
                cmdUpdate(0.0, eMtrRotDir.OFF, true, false, true);
                break;
            case 90: // Emergency stop. Re-issue last commands
                cmdUpdate(0.0, eMtrRotDir.OFF, lockPinASaved, lockPinBSaved, sliderSaved);
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
        SmartDashboard.putBoolean("Climber/SV/brake", brakeRel.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinAExt", lockPinAExt.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinARet", lockPinARet.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinB", lockPinB.get());
        SmartDashboard.putBoolean("Climber/SV/slider", slider.get());
        SmartDashboard.putBoolean("Climber/buttonClimb1", buttonClimb1.isDown());
        SmartDashboard.putBoolean("Climber/buttonClimb2", buttonClimb2.isDown());
        SmartDashboard.putBoolean("Climber/climbEnabled", climbEnabled);
        SmartDashboard.putBoolean("Climber/FB/lockPinAExt", lockPinAExt_FB.get());
        SmartDashboard.putBoolean("Climber/FB/lockPinARet", lockPinARet_FB.get());
        SmartDashboard.putBoolean("Climber/FB/lockPinBExt", lockPinBExt_FB.get());
        SmartDashboard.putBoolean("Climber/FB/lockPinBRet", lockPinBRet_FB.get());
        SmartDashboard.putBoolean("Climber/FB/sliderExt", sliderExt_FB.get());
        SmartDashboard.putBoolean("Climber/FB/sliderRet", sliderRet_FB.get());
    }

    // COMMAND UPDATE
    /**
     * @param drvSpd      - Speed of drive motors
     * @param mtrRotDir    - Motor rotation Off | Forward| Reverse
     * @param pinA_Ext  - True = LockPinAExtend | False = LockPinARetract
     * @param pinB_Ext  - True = LockPinBExtend | False = LockPinBRetract
     * @param slider_Ext    - True = SliderExtend | False = SliderRetract

     */
    private static void cmdUpdate(double drvSpd, eMtrRotDir mtrRotDir, boolean pinA_Ext,
            boolean pinB_Ext, boolean slider_Ext) {
        lockPinASaved = pinA_Ext;
        lockPinBSaved = pinB_Ext;
        sliderSaved = slider_Ext;
        if (checkLockPinsOK() == false) mtrRotDir = eMtrRotDir.OFF;
        // ----------------- Shooter statuses and misc.

        Drive.cmdUpdate(0.0, 0.0, false, 2);
        
        switch (mtrRot) {
            case OFF: 
                climberMotor.set(0.0);
                brakeRel.set(false);
                break;
            case FWD: 
                climberMotor.set(rotationalSpeed);
                brakeRel.set(true);
                break;
            case REV:
                climberMotor.set(-rotationalSpeed);
                brakeRel.set(true);
                break;
        }

        lockPinAExt.set(pinA_Ext);
        lockPinARet.set(!pinA_Ext);
        lockPinB.set(pinB_Ext);
        slider.set(slider_Ext);
        
    }
 
    private static Timer safePinTmr = new Timer(0.2);
    /**
     * @return true if lockingPins and slider are all matching
     */
    private static boolean checkLockPinsOK(){
      return  safePinTmr.hasExpired(0.2, (!(lockPinAExt.get() ^ lockPinAExt_FB.get() ||
                 lockPinARet.get() ^ lockPinARet_FB.get() ||
                 lockPinB.get() ^ (lockPinBExt_FB.get() && !lockPinBRet_FB.get()) ||
                 slider.get() ^ (sliderExt_FB.get() && !sliderRet_FB.get()))));
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
