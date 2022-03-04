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
    private static ISolenoid brakeRel = IO.climbBrakeRel_SV;    //Release the climber brake
    // Positive Feedback, FB, for extending the left & right locking pins & sliders
    private static boolean lockPinAExt_FB(){return IO.lockPinAExt_L_FB.get() & IO.lockPinAExt_R_FB.get();}  //Or of pin A left & right
    private static boolean lockPinBExt_FB(){return IO.lockPinBExt_L_FB.get() & IO.lockPinBExt_R_FB.get();}  //Or of pin A left & right
    private static boolean sliderExt_FB(){return IO.sliderExt_L_FB.get() & IO.sliderExt_R_FB.get();}  //Or of pin A left & right

    // Joystick Buttons
    private static Button buttonClimb1 = JS_IO.btnClimb1;   //Both MUST be pressed tp
    private static Button buttonClimb2 = JS_IO.btnClimb2;   //start and continue climb
    private static Button btnClimbSlideRst = JS_IO.btnClimbSlideRst; //Reset Slider, state 0

    // Climber variables
    private enum eArmDir {
        OFF, // Rotational Motor speed is 0.0
        FWD, // Rotational Motor speed is +ROT_SPD
        REV  // Rotatioanl Motor speed is -ROT_SPD
    }
    private static double ROT_SPD = 0.50; //Arm motor fixed speed. Var for testing
    private static double armDegrees(){ return IO.climbLdMtr_Enc.degrees();}
    private static double drvSpdSaved;    // Used in case 90, for sdb only
    private static double armSpdSaved;    // Used in case 90, for sdb only
    private static eArmDir armDirSaved;   // Used in case 90, for sdb only
    private static boolean lockPinASaved; // Used in case 90, emerg. stop
    private static boolean lockPinBSaved; // Used in case 90, emerg. stop
    private static boolean sliderSaved;   // Used in case 90, emerg. stop
    private static eArmDir prvArmDir = eArmDir.OFF; //Used to delay when switching direction
    private static Timer armDlyTmr = new Timer(0.2);// Delay when switching direction

    private static boolean climbEnabled = false; //Both Joysick buttons pressed
    private static int state = 0;       // Climber state machine. 0=Off
    private static boolean slideReset = false;
    private static int prvState = 0;    // Used to return from state 90, eStop.
    private static Timer stateTmr = new Timer(.05); // Timer for state machine

    /**
     * Initialize Climber stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();
        cmdUpdate(0.0, 0.0, false, false, false);
        state = 0;
        IO.climbLdMtr_Enc.reset();
    }

    /**
     * Update Climber. Called from teleopPeriodic in robot.java.
     * <p>
     * Determine any state that needs to interupt the present state, usually by way
     * of a JS button but can be caused by other events.
     */
    public static void update() {
        climbEnabled = (buttonClimb1.isDown() && buttonClimb2.isDown() && !slideReset) ? true : false;
        if(climbEnabled && state == 0) state = 1;           //Start climb
        if(climbEnabled && state == 90) state = prvState;   //Driver interrupted climb
        if(!climbEnabled && state != 0 && !slideReset) state = 90;         //Driver continue climb
        if(btnClimbSlideRst.onButtonPressed()) state = 80;     //Reset Slider return to state 0

        smUpdate();
        sdbUpdate();
    }

    private static double pitch = 0.0;


    /**
     * State Machine update for climber
     */    
    private static void smUpdate() {

        pitch = IO.navX.getRoll();  //roboRIO turned 90.  No we are NOT moving it!
        
        switch (state) {
            case 0: // Turns everything off
                cmdUpdate(0.0, 0.0, false, false, false );
                stateTmr.clearTimer();
                break;
            //----- Prep arm and move to low bar -----
            case 1: //Postion arm vertical, 'A' up for climb, low bar
                cmdUpdate(0.0, ROT_SPD, false, false, true );
                Drive.setHdgHold(180.0);    // Set drive steering to hold 180 heading
                IO.coorXY.drvFeetRst();
                if(armDegrees() > 85.0){
                    state++;
                    System.out.println("Degrees " + armDegrees());
                }
                // if (stateTmr.hasExpired(1.3, state)) state++;
                break;
            case 2: // Move backwards (positive Y cmd) 6' to start climb position
                cmdUpdate(0.0, 0.0, false,false,true);
                // if (IO.coorXY.drvFeet() < -6.0) state++;
                break;
            case 3: // Move backwards 3' until robot pitches 15 degrees fwd/down
                cmdUpdate(0.4, 0.0, false, false, true);
                // if(pitch < -2.0){        //USe 15 for comp, 5 for testing.
                //     if(stateTmr.hasExpired(0.5, true)) state++;
                // }else{
                //     stateTmr.clearTimer();
                // }
                break;
            //------ In contact with low arm, lock on and start climb. -----
            case 4: // Stop the driving and grab bar with LockPinA
                cmdUpdate(0.0, 0.0, true, false, true);
                // if (lockPinAExt_FB() == true) state++;
                IO.coorXY.drvFeetRst();
                break;
            case 5: // Robot moves forward (negitive Y cmd) 3 feet and arm starts rotating forward
                cmdUpdate(-0.2, ROT_SPD, true, false, true);
                if (IO.coorXY.drvFeet() > 3.0) state++;
                break;
            case 6: // Stop forward wheel motion
                cmdUpdate(0.0, ROT_SPD, true, false, true);
                state++;
                break;
            case 7: // Continue arm rotation until pitch hits x degrees
                cmdUpdate(0.0, ROT_SPD, true, false, true);
                if(pitch < -2.0){       //Use 30 for comp, -5 for testing
                    if(stateTmr.hasExpired(0.5, true)) state++;
                }else{
                    stateTmr.clearTimer();
                }
                break;
            //----- Contact Medium bar. Grab it. Release low arm and clear it.
            //----- Climb to transversal bar -----
            case 8: // Latch onto second bar with Pin B. Stop rotation.
                cmdUpdate(0.0, 0.0, true, true, true);
                if (lockPinBExt_FB() == true) state++;
                break;
            case 9: // Retract Pin A in order to release the first bar
                cmdUpdate(0.0, 0.0, false, true, true);
                if (!lockPinAExt_FB()) state++;
                break;
            case 10: // Rotating upward/REV to clear slider.  //Do we need this one?  Doesn't hurt.
                cmdUpdate(0.0, 0.0, false, true, true);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 11: // Retract the slider arm.  Slider will retract when it clears the bar.
                cmdUpdate(0.0, -ROT_SPD, false, true, false);
                if (!sliderExt_FB()) state++;
                break;
            case 12: // Motor Union Break Timer.  Stop before chging direction
                cmdUpdate(0.0, 0.0, false, true, false);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 13: // Go forward towards transversal bar, passed the low bar.
                cmdUpdate(0.0, ROT_SPD, false, true, false);
                if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            case 14: // Slider cleared low bar, extend the slider.  Keep going forward.
                cmdUpdate(0.0, ROT_SPD, false, true, true);
                if (sliderExt_FB())state++;
                break;
            case 15: // Keep going till we pitch x
                cmdUpdate(0.0, ROT_SPD, false, true, true);
                // TODO: keep going until robot hits pitch x
                if(pitch < -2.0 ){      //Use x for comp, testing -5
                    if(stateTmr.hasExpired(0.5, true)) state++;
                }else{
                    stateTmr.clearTimer();
                }
                state++;
                break;
            //----- Contact Traversal bar. Grab it. Release Medium bar.
            //----- Lift off Medium bar then hold.  Done. -----
            case 16: // latch the transversal.
                cmdUpdate(0.0, ROT_SPD, true, true, true);
                if (lockPinAExt_FB()) state++;
                break;
            case 17: // Motor Union Break
                cmdUpdate(0.0, 0.0, true, true, true);
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 18: // Release the B pin
                cmdUpdate(0.0, 0.0, true, false, true);
                if (!lockPinBExt_FB()) state++;
                break;
            case 19: // Rotating Reverse to give leeway
                cmdUpdate(0.0, -ROT_SPD, true, false, true);
                if (stateTmr.hasExpired(1.3, state)) state++;
                break;
            case 20: // Turn arm motor off.  --- DONE! ---
                cmdUpdate(0.0, 0.0, true, false, true);
                break;

            //------ Resync Slider after DS shutdown when sliders are extended.
            case 80: // Turn on slider & pin A, no motors, wait then next.
                cmdUpdate(0.0, 0.0, true, false, true);
                slideReset = true;
                if (sliderExt_FB() && lockPinAExt_FB()) state++;
                break;
            case 81: // Turn off pin A, wait then next.
                cmdUpdate(0.0, 0.0, false, false, true);
                if (!lockPinAExt_FB()) state++;
                break;
            case 82: // Turn off slider, wait then return to state 0.
                cmdUpdate(0.0, 0.0, false, false, false);
                if (!sliderExt_FB() && !lockPinAExt_FB()){
                    state = 0;
                    slideReset = false;
                }
                break;

            //----- Driver released buttoms during climb.  Stop & hold postion!
            case 90: // Emergency stop. Re-issue last commands
                cmdUpdate(0.0, 0.0, lockPinASaved, lockPinBSaved, sliderSaved);
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

    private static void cmdUpdate(double drvSpd, double rotSpd,
                                  boolean pinA_Ext, boolean pinB_Ext, boolean slider_Ext) {
        // Stop arm motor if pins & slider FB's don't agree with cmds.
        // if (!checkLockPinsOK()){
        //     drvSpd = 0.0;
        //     armDir = eArmDir.OFF;
        // }

        // Save device cmds for state 90, eStop.
        drvSpdSaved = drvSpd;       //sdb only
        // armDirSaved = armDir;    //sdb only
        lockPinASaved = pinA_Ext;
        lockPinBSaved = pinB_Ext;
        sliderSaved = slider_Ext;
        if(state != 90) prvState = state;

        Drive.cmdUpdate(drvSpd, 0.0, false, 2); //Send cmd to drive system as arcade

        if(IO.climbLdMtr_Enc.degrees() < -5.0) rotSpd = 0.0;    //SAFETY!  Stop rot motor if arm moved Rev too far.
        climberMotor.set(rotSpd);
        //TODO: add brake control here.

        //Delay if switching direction without first stopping.
        // if (prvArmRot != eArmDir.REV && prvArmRot != eArmDir.FWD) {      //Don't think this worked
        // if (prvArmDir != armDir) armDlyTmr.startTimer();
        //See if the simple statement above works.  Else use the below.
        // if ((armDir == eArmDir.FWD || armDir == eArmDir.REV) && prvArmDir != eArmDir.OFF) armDlyTmr.startTimer();
        // if( !armDlyTmr.hasExpired() ) armDir = eArmDir.OFF;

        // System.out.println("Climb Here 1 " + armDir + " " + ROT_SPD);
        // switch (armDir) {
        //     case OFF: 
        //         climberMotor.set(0.0);
        //         // if (brakeTimer.hasExpired(0.1, false) || (state == 90)) brakeRel.set(false);
        //         break;
        //     case FWD: 
        //         climberMotor.set(ROT_SPD);
        //         System.out.println("Climb Here 2 " + armDir + " " + ROT_SPD);
        //         // if (brakeTimer.hasExpired(0.1, true)) brakeRel.set(true);
        //         break;
        //     case REV:
        //         // climberMotor.set(-ROT_SPD);
        //         climberMotor.set(0.0);      //For testing Fwd rotation only!
        //         // if (brakeTimer.hasExpired(0.1, true)) brakeRel.set(true);
        //         break;
        // }
        // prvArmDir = armDir;
    
        //Pin A is a dual action SV and requires a signal to extend and another to retract
        lockPinAExt.set(pinA_Ext);
        lockPinARet.set(!pinA_Ext);
        //Pin B & slider (& brake) are single action SV's one signal to activate else deactivate.
        lockPinBExt.set(pinB_Ext);
        sliderExt.set(slider_Ext);
        
    }
 
    /**Put objects on the sdb that values need to be retrieved from the sbd. */
    private static void sdbInit() {
        SmartDashboard.putNumber("Climber/Mtr/1. ROT_SPD", ROT_SPD);
        SmartDashboard.putNumber("Climber/Mtr/4. climb TPD", IO.climbLdMtr_TPD);
    }

    /**Update the sdb */
    private static void sdbUpdate() {
        SmartDashboard.putNumber( "Climber/AC/1. State", state);
        SmartDashboard.putNumber( "Climber/AC/2. Pitch", pitch);
        SmartDashboard.putBoolean("Climber/AC/3. buttonClimb1", buttonClimb1.isDown());
        SmartDashboard.putBoolean("Climber/AC/4. buttonClimb2", buttonClimb2.isDown());
        SmartDashboard.putBoolean("Climber/AC/5. climbEnabled", climbEnabled);
        SmartDashboard.putNumber( "Climber/AC/6. Drive Cmd", drvSpdSaved);
        SmartDashboard.putNumber( "Climber/AC/7. Arm Cmd", armSpdSaved);
        // SmartDashboard.putString("Climber/AC/6. Arm Dir Cmd", armDirSaved.toString());

        // SmartDashboard.putBoolean("Climber/SV/1. brake", brakeRel.get());
        SmartDashboard.putBoolean("Climber/SV/2. lockPinAExt", lockPinAExt.get());
        SmartDashboard.putBoolean("Climber/SV/3. lockPinARet", lockPinARet.get());
        SmartDashboard.putBoolean("Climber/SV/4. lockPinBExt", lockPinBExt.get());
        SmartDashboard.putBoolean("Climber/SV/5. sliderExt", sliderExt.get());

        SmartDashboard.putBoolean("Climber/FB/1. lockPinAExt", lockPinAExt_FB());
        SmartDashboard.putBoolean("Climber/FB/2. lockPinBExt", lockPinBExt_FB());
        SmartDashboard.putBoolean("Climber/FB/3. sliderExt", sliderExt_FB());
        SmartDashboard.putNumber( "Climber/FB/4. Feet", IO.coorXY.drvFeet());

        ROT_SPD = SmartDashboard.getNumber("Climber/Mtr/1. ROT_SPD", 0.3);
        SmartDashboard.putNumber("Climber/Mtr/2. climbMotor", climberMotor.get());
        SmartDashboard.putNumber("Climber/Mtr/3. Ld enc Ticks", IO.climbLdMtr_Enc.ticks());
        IO.climbLdMtr_Enc.setTPF(SmartDashboard.getNumber("Climber/Mtr/4. climb TPD", 300.0));
        SmartDashboard.putNumber("Climber/Mtr/5. climb Deg", IO.climbLdMtr_Enc.degrees());
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
        pin_state = (!(lockPinAExt.get() ^ lockPinAExt_FB() ||
                 lockPinARet.get() ^ !lockPinAExt_FB() ||
                 lockPinBExt.get() ^ (lockPinBExt_FB()) ||
                 sliderExt.get() ^ (sliderExt_FB())));
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
