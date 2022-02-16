package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.util.ISolenoid;
import frc.io.hdw_io.util.InvertibleDigitalInput;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.util.Timer;
import frc.robot.subsystem.drive.Drive;
import com.revrobotics.CANSparkMax;

public class Climber {
  

    // Hardware Definitions
    private static CANSparkMax climberMotor = IO.climbMotor;   //Motor to rotate arm

    private static ISolenoid lockPinAExt = IO.lockPinAExt_SV;   //Extend locking pin A
    private static ISolenoid lockPinARet = IO.lockPinARet_SV;   //Retract locking pin A
    private static ISolenoid lockPinBExt = IO.lockPinBExt_SV;   //Extend locking pin B
    private static ISolenoid sliderExt = IO.sliderExt_SV;       //Extend Arm end A slider
    private static ISolenoid brakeRel;                          //Release the brake
    // Positive Feedback, FB, for locking pins & slider
    private static InvertibleDigitalInput lockPinAExt_FB = IO.lockPinAExt_FB;
    private static InvertibleDigitalInput lockPinARet_FB = IO.lockPinARet_FB; 
    private static InvertibleDigitalInput lockPinBExt_FB = IO.lockPinBExt_FB;
    private static InvertibleDigitalInput lockPinBRet_FB = IO.lockPinBRet_FB;
    private static InvertibleDigitalInput sliderExt_FB   = IO.sliderExt_FB; 
    private static InvertibleDigitalInput sliderRet_FB   = IO.sliderRet_FB;

    // Joystick Buttons
    private static Button buttonClimb1 = JS_IO.btnClimb1;   //Both MUST be pressed tp
    private static Button buttonClimb2 = JS_IO.btnClimb2;   //start and continue climb
    private static Button btnClimbReset;    //Reset climb, state 0

    // Climber variables
    private enum eMtrRotDir {
        OFF, // Rotational Motor speed is 0.0
        FWD, // Rotational Motor speed is +ROT_SPD
        REV  // Rotatioanl Motor speed is -ROT_SPD
    }
    private static eMtrRotDir mtrRot = eMtrRotDir.OFF; 
    private static final double ROT_SPD = 0.70; //Arm motor fixed speed.

    private static boolean lockPinASaved; // Used in case 90, emerg.-stop
    private static boolean lockPinBSaved; // Used in case 90, emerg.-stop
    private static boolean sliderSaved;   // Used in case 90, emerg.-stop

    private static boolean climbEnabled = false; //Both Joysick buttons pressed
    private static int state = 0;       // Climber state machine. 0=Off
    private static int prvState = 0;    // Used to return from state 90, eStop.
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    /**
     * Initialize Climber stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(0.0, eMtrRotDir.OFF, false, false, false);
        state = 0;
    }

    /**
     * Update Climber. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        climbEnabled = (buttonClimb1.isDown() && buttonClimb2.isDown()) ? true : false;
        if(climbEnabled && state == 0) state = 1;           //Start climb
        if(climbEnabled && state == 90) state = prvState;   //Driver interrupted climb
        if(!climbEnabled && state != 0) state = 90;         //Driver continue climb

        if(btnClimbReset.isDown()) state = 0;   //bc? Start angle bad or fall?

        smUpdate();
        sdbUpdate();
    }

    private static double pitch = 0.0;


    /**
     * State Machine update for climber
     */    
    private static void smUpdate() {

        pitch = IO.navX.getPitch();
        
        switch (state) {
            case 0: // Turns everything off
                cmdUpdate(0.0, eMtrRotDir.OFF, false, false, false );
                stateTmr.hasExpired(0, state);
                break;
            //----- Prep arm and move to low bar -----
            case 1: //Postion arm vertical, 'A' up for climb, low bar
                cmdUpdate(0.0, eMtrRotDir.REV, false, false, true );
                Drive.setHdgHold(180.0);    // Set drive steering to hold 180 heading
                if (stateTmr.hasExpired(0.3, state)) state++;
                IO.coorXY.drvFeetRst();
                break;
            case 2: // Move backwards 6' to start climb position
                cmdUpdate(-0.7, eMtrRotDir.OFF, false,false,true);
                if (IO.coorXY.drvFeet() < -6.0) state++;
                break;
            case 3: // Move backwards 3' until robot pitches 15 degrees fwd/down
                cmdUpdate(-0.4, eMtrRotDir.OFF, false, false, true);
                
                if (stateTmr.hasExpired(0.5, (pitch > 15 && pitch < 19.31))) state++;
                break;
            //------ In contact with low arm, lock on and start climb. -----
            case 4: // Stop the driving and grab bar with LockPinA
                cmdUpdate(0.0, eMtrRotDir.OFF, true, false, true);
                if (lockPinAExt_FB.get() == true) state++;
                IO.coorXY.drvFeetRst();
                break;
            case 5: // Robot moves forward 3 feet and arm starts rotating forward
                cmdUpdate(0.2, eMtrRotDir.FWD, true, false, true); 
                if (IO.coorXY.drvFeet() > 3.0) state++;
                break;
            case 6: // Stop forward wheel motion
                cmdUpdate(0.0, eMtrRotDir.FWD, true, false, true);
                state++;
                break;
            case 7: // Continue arm rotation until pitch hits x degrees
                cmdUpdate(0.0, eMtrRotDir.FWD, true, false, true);
                if (stateTmr.hasExpired(0.5, (pitch > 30 && pitch < 33.69420))) state++; 
                state++;
                break;
            //----- Contact Medium bar. Grab it. Release low arm and clear it.
            //----- Climb to transversal bar -----
            case 8: // Latch onto second bar with Pin B. Stop rotation.
                cmdUpdate(0.0, eMtrRotDir.OFF, true, true, true);
                if (lockPinBExt_FB.get() == true) state++;
                break;
            case 9: // Open Pin A in order to release the first bar
                cmdUpdate(0.0, eMtrRotDir.OFF, false, true, true);
                if (lockPinARet_FB.get() == true) state++;
                break;
            case 10: // Rotating upward/REV to clear slider.
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
            case 14: // Slider cleared low bar, extend the slider.  Keep going forward.
                cmdUpdate(0.0, eMtrRotDir.FWD, false, true, true);
                if (sliderExt_FB.get() == true)state++;
                break;
            case 15: // Keep going till we pitch x
                cmdUpdate(0.0, eMtrRotDir.FWD, false, true, true);
                // TODO: keep going until robot hits pitch x
                if (stateTmr.hasExpired(0.5, (pitch > 15 && pitch < 19.31))) state++;
                state++;
                break;
            //----- Contact Traversal bar. Grab it. Release Medium bar.
            //----- Lift off Medium bar then hold.  Done. -----
            case 16: // latch the transversal.
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
            //----- Driver released buttoms during climb.  Stop & hold postion!
            case 90: // Emergency stop. Re-issue last commands
                cmdUpdate(0.0, eMtrRotDir.OFF, lockPinASaved, lockPinBSaved, sliderSaved);
            default:// Default state
                break;
        }
    }
    private static Timer brakeTimer = new Timer(0.1);
    
    // COMMAND UPDATE
    /**
     * 
     * @param drvSpd     - Speed of drive motors
     * @param mtrRotDir  - Motor rotation Off | Forward | Reverse
     * @param pinA_Ext   - True = LockPinAExtend | False = LockPinARetract
     * @param pinB_Ext   - True = LockPinBExtend | False = LockPinBRetract
     * @param slider_Ext - True = SliderExtend | False = SliderRetract

     */

    private static eMtrRotDir previousMtrRotDir = eMtrRotDir.OFF;

    private static void cmdUpdate(double drvSpd, eMtrRotDir mtrRotDir,
                                  boolean pinA_Ext, boolean pinB_Ext, boolean slider_Ext) {
        // Save device cmds for state 90, eStop.
        lockPinASaved = pinA_Ext;
        lockPinBSaved = pinB_Ext;
        sliderSaved = slider_Ext;
        if(state != 90) prvState = state;

        // Stop arm motor if pins & slider FB's don't agree with cmds.
        if (checkLockPinsOK() == false) mtrRotDir = eMtrRotDir.OFF;

        Drive.cmdUpdate(drvSpd, 0.0, false, 2); //Send cmd to drive system as arcade

        if (previousMtrRotDir != eMtrRotDir.REV && previousMtrRotDir != eMtrRotDir.FWD) {
            switch (mtrRot) {
                case OFF: 
                    climberMotor.set(0.0);
                    if (brakeTimer.hasExpired(0.1, false) || (state == 90)) brakeRel.set(false);
                    break;
                case FWD: 
                    climberMotor.set(ROT_SPD);
                    if (brakeTimer.hasExpired(0.1, true)) brakeRel.set(true);
                    break;
                case REV:
                    climberMotor.set(-ROT_SPD);
                    if (brakeTimer.hasExpired(0.1, true)) brakeRel.set(true);
                    break;
            }
            previousMtrRotDir = mtrRot;
        }
    
        //Pin A is a dual action SV and requires a signal to extend and another to retract
        lockPinAExt.set(pinA_Ext);
        lockPinARet.set(!pinA_Ext);
        //Pin B & slider (& brake) are single action SV's one signal to activate else deactivate.
        lockPinBExt.set(pinB_Ext);
        sliderExt.set(slider_Ext);
        
    }
 
    private static void sdbInit() {

    }

    private static void sdbUpdate() {
        SmartDashboard.putString("Climber/mtrRot", mtrRot.toString());
        SmartDashboard.putBoolean("Climber/SV/brake", brakeRel.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinAExt", lockPinAExt.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinARet", lockPinARet.get());
        SmartDashboard.putBoolean("Climber/SV/lockPinB", lockPinBExt.get());
        SmartDashboard.putBoolean("Climber/SV/slider", sliderExt.get());
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

    // ----------------- Shooter statuses and misc. ---------------

    private static Timer safePinTmr = new Timer(0.2);   // Debounce Timer for checkLockPinsOK
    /**
     * @return true if lockingPins and slider are all matching for minimum time
     * <p> false if not matching for minimum time.
     */
    private static boolean pin_state = true;
    private static boolean prv_pin_state = true;
    
    private static boolean checkLockPinsOK(){
      // TODO: Fix logic 
        pin_state = (!(lockPinAExt.get() ^ lockPinAExt_FB.get() ||
                 lockPinARet.get() ^ lockPinARet_FB.get() ||
                 lockPinBExt.get() ^ (lockPinBExt_FB.get() && !lockPinBRet_FB.get()) ||
                 sliderExt.get() ^ (sliderExt_FB.get() && !sliderRet_FB.get())));
        if (safePinTmr.hasExpired(0.2,pin_state) == true) prv_pin_state = pin_state;
        return prv_pin_state;
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
