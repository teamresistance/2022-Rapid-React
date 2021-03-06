package frc.robot.subsystem;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.util.ISolenoid;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.util.Timer;
import com.revrobotics.CANSparkMax;

/**
 * End game involves climbing up and/or across 4 cross pipes.
 * Low bar is at 4’ height.  The other 3 bars are 2’ apart and 
 * at 5.0’, 6.25’ and 7.6’ high.  The normal robot height is 
 * limited to 4.33’, except when the robot is touching the 
 * Hanger zone, then it is 5.5’.  SO, only the low bar and 
 * the first of the 3-bar combination, medium bar, can be reached 
 * from the floor,  Upper bars must be "swung" to.  
 * Significant points are awarded for upper bars.
 */
public class Climber {
      // Hardware Definitions
    private static CANSparkMax climberMotor = IO.climbMotor;   //Motor to rotate arm

    private static ISolenoid lockPinAExt = IO.lockPinAExt_SV;   //Extend locking pin A
    private static ISolenoid lockPinARet = IO.lockPinARet_SV;   //Retract locking pin A
    private static ISolenoid lockPinBExt = IO.lockPinBExt_SV;   //Extend locking pin B
    private static ISolenoid sliderExt = IO.sliderExt_SV;       //Extend Arm end A slider
    private static ISolenoid brakeRel = IO.climbBrakeRel_SV;    //Release the climber brake

    // Joystick Buttons
    private static Button buttonClimb1 = JS_IO.btnClimb1;   //Both MUST be pressed tp
    private static Button buttonClimb2 = JS_IO.btnClimb2;   //start and continue climb
    private static Button btnClimbSlideRst = JS_IO.btnClimbSlideRst; //Reset Slider, state 0

    // Climber variables
    private static double ROT_SPD = 0.45; //Arm motor fixed speed. Var for testing
    private static double armDegrees(){ return IO.climbLdMtr_Enc.degrees();}    //Arm degrees of rotation
    private static double pitch(){ return IO.navX.getRoll();}    //roboRIO turned 90.  No we are NOT moving it!

    private static double drvSpdSaved;    // Used in case 90, for sdb only
    private static double armSpdSaved;    // Used in case 90, for sdb only
    private static boolean lockPinASaved; // Used in case 90, emerg. stop
    private static boolean lockPinBSaved; // Used in case 90, emerg. stop
    private static boolean sliderSaved;   // Used in case 90, emerg. stop
    private static Timer armStartTmr = new Timer(0.1);

    private static boolean eStop = false;   //Emergency stop, buttons released before end

    private static boolean climbEnabled = false;//Both Joysick buttons pressed
    private static int state = 0;               // Climber state machine. 0=Off
    private static boolean slideReset = false;
    private static int prvState = 0;            // Used to return from state 90, eStop.
    private static Timer stateTmr = new Timer(.05); // Timer for state machine
    private static boolean climbDone = false;
    private static boolean brakeTest = false;

    //---- Combined status of left & right extended feedbacks. ----
    /**
     * enum for status of lockPinsA & B and slider
     * <p>RET - both retracted
     * <p>LEFT - Left extended, Right retracted
     * <p>RIGHT - Left retracted, Right extended
     * <p>EXT - both extended
     */
    private static enum eStsFB {RET, LEFT, RIGHT, EXT};

    /**
     * 
     * @param in1 - Left extended feedback
     * @param in2 - Right extended feedback
     * @return status of combined Left & Right extended feedbacks.
     * <p>RET - both retracted
     * <p>LEFT - Left extended, Right retracted
     * <p>RIGHT - Left retracted, Right extended
     * <p>EXT - both extended
     */
    private static eStsFB e2DIStatus(boolean in1, boolean in2){
        int tmp = in1 ? 1 : 0;
        tmp += in2 ? 2 : 0;
        switch(tmp){
            case 0: return eStsFB.RET;
            case 3: return eStsFB.EXT;
            case 1: return eStsFB.LEFT;
            default: return eStsFB.RIGHT;
        }
    }

    /**Combined status of left & right locking pins A. */
    private static eStsFB lockPinA_Sts(){
        return e2DIStatus(IO.lockPinAExt_L_FB.get(), IO.lockPinAExt_R_FB.get());
    }

    /**Combined status of left & right locking pins B. */
    private static eStsFB lockPinB_Sts(){
        return e2DIStatus(IO.lockPinBExt_L_FB.get(), IO.lockPinBExt_R_FB.get());
    }

    /**Combined status of left & right slider feedbacks. */
    private static eStsFB slider_Sts(){
        return e2DIStatus(IO.sliderExt_L_FB.get(), IO.sliderExt_R_FB.get());
    }

    /**
     * Initialize Climber stuff. Called from telopInit (maybe robotInit(?)) in
     * Robot.java
     */
    public static void init() {
        sdbInit();  //CAN objects commented out, unless troubleshooting.
        cmdUpdate(0.0, false, false, false);
        state = 0;
        IO.climbLdMtr_Enc.reset();
        brakeRel.set(false);
        // System.out.println("Init: " + brakeRel.get());
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
        sdbUpdate();    //CAN objects commented out, unless troubleshooting.
    }

    /** State Machine update for climber  */    
    private static void smUpdate() {
        switch (state) {
            case 0: // Turns everything off
                //        arm, pinA,  pinB,  Slide
                cmdUpdate(0.0, false, false, false);
                climbDone = false;
                stateTmr.clearTimer();
                break;
            //----- Prep arm and move to low bar -----
            case 1: //Due to issues with the CAN, reinitialize the motors.  Set arm degrees to 0.
                cmdUpdate(0.0, false, false, false );
                IO.climberMtrsInit();   //Reinitialize the motors, bc maybe missing on startup
                IO.climbLdMtr_Enc.reset();  //Set starting position as 0 degrees rotation.
                state++;
                break;
            case 2: //Position arm to 90 degrees and extend slider
                cmdUpdate(ROT_SPD, false,false,true);
                if(armDegrees() > 85.0) state++;
                break;
            case 3: // Driver moves robot backward until robot contacts low bar and pitches 10 degrees fwd/down
                cmdUpdate(0.0, false, false, true);
                if(pitch() < -10.0){
                    if(stateTmr.hasExpired(0.5, true)) state++; //Avoid a bump, wait 0.5 sec
                }else{
                    stateTmr.clearTimer();
                }
                break;
            //------ In contact with low arm, lock on and start climb. -----
            case 4: // Grab bar with LockPinA.  Driver stops driving.
                cmdUpdate(0.0, true, false, true);
                if (lockPinA_Sts() == eStsFB.EXT) state++;  //Wait for both pins confirmed extended
                break;
            case 5: // Arm starts rotating forward.
                cmdUpdate(ROT_SPD * 1.15, true, false, true);
                state++;
                break;
            case 6: // Holder, continue action.
                cmdUpdate(ROT_SPD * 1.15, true, false, true);
                state++;
                break;
            case 7: // Continue arm rotation until arm degrees GT 250 for 0.5 sec.
                cmdUpdate(ROT_SPD, true, false, true);
                if(armDegrees() > 250.0){
                    if(stateTmr.hasExpired(0.5, true)){
                        state++;
                        System.out.println("Degrees @7: " + armDegrees());
                    }
                }else{
                    stateTmr.clearTimer();
                }
                break;
            //----- Contact Medium bar. Grab it. Release low arm and clear it.
            //----- Climb to transversal bar -----
            case 8: // Latch onto second bar with Pin B. Stop rotation.
                cmdUpdate(ROT_SPD/1.75, true, true, true);
                if (lockPinB_Sts() == eStsFB.EXT) state++;  //Confirm both lockPinB Extended
                break;
            case 9: // Retract Pin A in order to release the first bar
                cmdUpdate(ROT_SPD/1.75, false, true, true);
                if (stateTmr.hasExpired(0.25, state)){
                    if (lockPinA_Sts() == eStsFB.RET) state++;  //Confirm both A pins retracted.
                }
                break;
            case 10: // Holder, continue action.
                cmdUpdate(ROT_SPD/1.75, false, true, true);
                state++;
                break;
            case 11: // Reverse direction to lift off low bar
                cmdUpdate(-ROT_SPD, false, true, true);
                if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            case 12: // Retract the slider arm.  Slider will retract when it clears the bar.
                cmdUpdate(-ROT_SPD/2, false, true, false);
                if (slider_Sts() == eStsFB.RET) state++;   //Confirm both sliders retracted
                break;
            case 13: // Go forward towards transversal bar, passed the low bar.
                cmdUpdate(ROT_SPD, false, true, false);
                if (stateTmr.hasExpired(2.5, state)) state++;   //Chg to degrees???
                break;
            case 14: // Slider cleared low bar, extend the slider.  Keep going forward.
                cmdUpdate(ROT_SPD, false, true, true);
                if (slider_Sts() == eStsFB.EXT) state++;    //Confirm both sliders extended
                break;
            case 15: // Keep going till arm degrees GT 415 for 0.5 sec.
                cmdUpdate(ROT_SPD * 1.25, false, true, true);
                if(armDegrees() > 415.0){
                    if(stateTmr.hasExpired(0.5, true)) state++;
                    System.out.println("Degrees @15: " + armDegrees());
                }else{
                    stateTmr.clearTimer();
                }
                break;
            //----- Contact Traversal bar. Grab it. Release Medium bar.
            //----- Lift off Medium bar then hold.  Done. -----
            case 16: // latch the transversal with pin A.
                cmdUpdate(ROT_SPD, true, true, true);
                if (lockPinA_Sts() == eStsFB.EXT) state++;  //Confirm both A pins extended
                break;
            case 17: // Hold, continue action.
                cmdUpdate(ROT_SPD, true, true, true);
                state++;
                break;
            case 18: // Release the B pin
                cmdUpdate(ROT_SPD, true, false, true);
                if (lockPinB_Sts() == eStsFB.RET) state++;  //Confirm both B pins retracted
                break;
            case 19: // Rotating Reverse to give leeway from middle bar
                cmdUpdate(-ROT_SPD, true, false, true);
                if(IO.climbLdMtr_Enc.degrees() < 330.0){
                    if(stateTmr.hasExpired(0.25, true)) state++;
                    System.out.println("Degrees @19: " + armDegrees());
                }else{
                    stateTmr.clearTimer();
                }
                break;
            case 20: // Turn arm motor off.  --- DONE! ---
                cmdUpdate(0.0, true, false, true);
                climbDone = true;
                break;

            //------ Resync Slider after DS shutdown when sliders are extended.
            case 80: // Turn on slider & pin A, no motors, wait then next.
                cmdUpdate(0.0, true, false, true);
                slideReset = true;
                if (stateTmr.hasExpired(0.5, state)){
                    if (slider_Sts() == eStsFB.EXT && lockPinA_Sts() == eStsFB.EXT) state++;
                }
                break;
            case 81: // Turn off pin A, wait then next.
                cmdUpdate(0.0, false, false, true);
                if (stateTmr.hasExpired(0.5, state)){
                    if (lockPinA_Sts() == eStsFB.RET) state++;
                }
                break;
            case 82: // Turn off slider, wait then return to state 0.
                cmdUpdate(0.0, false, false, false);
                if ((slider_Sts() == eStsFB.RET) && (lockPinA_Sts() == eStsFB.RET)){
                    state = 0;
                    slideReset = false;
                }
                break;

            //----- Driver released buttoms during climb.  Stop & hold postion!
            case 90: // Emergency stop. Re-issue last commands
                cmdUpdate(0.0, lockPinASaved, lockPinBSaved, sliderSaved);
                eStop = true;
            default:// Default state
                break;
        }
    }

    /**
     * Command update - issue ALL commands from here.  Implement safeties here.
     * @param rotSpd     - Climber motor rotational command.  + is forard, climbing up.
     * @param pinA_Ext   - True = LockPinAExtend | False = LockPinARetract
     * @param pinB_Ext   - True = LockPinBExtend | False = LockPinBRetract
     * @param slider_Ext - True = SliderExtend | False = SliderRetract
     */
    private static void cmdUpdate(double rotSpd,
                                  boolean pinA_Ext, boolean pinB_Ext, boolean slider_Ext) {
        // Save device cmds for state 90, eStop.
        armSpdSaved = rotSpd;
        lockPinASaved = pinA_Ext;
        lockPinBSaved = pinB_Ext;
        sliderSaved = slider_Ext;
        if(state != 90) prvState = state;

        if(armDegrees() < -10.0) rotSpd = 0.0;  //SAFETY!  Stop rot motor if arm moved Rev too far.
        if(armDegrees() < -5.0) state = 1;      //Try to re-initialize one more time.
        
        //Motor restart after eStop.  Start in opposite direction of cmd than move in cmd direction.
        if(eStop && (state != 90)){
            if(!armStartTmr.hasExpired(0.3, true)){
                rotSpd = -rotSpd;
            }else{
                eStop = false;
                armStartTmr.clearTimer();
            }
        }

        if(climbDone){              //If climber is done (20), to set brake, regardless of passed cmds,
            rotSpd = 0.0;           //set rotation to 0.0 and 
            climbEnabled = false;   //climbEnable false.
        }

        climberMotor.set(rotSpd);   //Issue rotation cmd.

        //Release the brake if buttons are pressed or the rotational speed is not 0.
        brakeTest = climbEnabled || rotSpd != 0.0;
        brakeRel.set(brakeTest); // authorized by Joel
    
        //Pin A is a dual action SV and requires a signal to extend and another to retract
        lockPinAExt.set(pinA_Ext);
        lockPinARet.set(!pinA_Ext);
        //Pin B & slider (& brake) are single action SV's one signal to activate else deactivate.
        lockPinBExt.set(pinB_Ext);
        sliderExt.set(slider_Ext);
    }

    /**Put objects on the sdb that values need to be retrieved from the sbd.
     * <p>--- Note: due to CAN buss issues, any CAN object is commented out
     * unless troubleshooting. ---
     */
    private static void sdbInit() {
        SmartDashboard.putNumber("Climber/Mtr/1. ROT_SPD", ROT_SPD);
        SmartDashboard.putNumber("Climber/Mtr/4. climb TPD", IO.climbLdMtr_TPD);
    }

    /**Update the sdb
     * <p>--- Note: due to CAN buss issues, any CAN object is commented out
     * unless troubleshooting. ---
     */
    private static void sdbUpdate() {
        SmartDashboard.putNumber( "Climber/AC/1. State", state);
        SmartDashboard.putNumber( "Climber/AC/2. Pitch", pitch());  //CAN
        SmartDashboard.putBoolean("Climber/AC/3. buttonClimb1", buttonClimb1.isDown());
        SmartDashboard.putBoolean("Climber/AC/4. buttonClimb2", buttonClimb2.isDown());
        SmartDashboard.putBoolean("Climber/AC/5. climbEnabled", climbEnabled);
        SmartDashboard.putNumber( "Climber/AC/6. Drive Cmd", drvSpdSaved);
        SmartDashboard.putNumber( "Climber/AC/7. Arm Cmd", armSpdSaved);
        SmartDashboard.putBoolean("Climber/AC/8. btnRst ", JS_IO.btnRst.isDown());

        // SmartDashboard.putBoolean("Climber/SV/1. brake", brakeRel.get());           //CAN
        // SmartDashboard.putBoolean("Climber/SV/2. lockPinAExt", lockPinAExt.get());  //CAN
        // SmartDashboard.putBoolean("Climber/SV/3. lockPinARet", lockPinARet.get());  //CAN
        // SmartDashboard.putBoolean("Climber/SV/4. lockPinBExt", lockPinBExt.get());  //CAN
        // SmartDashboard.putBoolean("Climber/SV/5. sliderExt", sliderExt.get());      //CAN
        // SmartDashboard.putBoolean("Climber/SV/6. brakeRel", brakeRel.get());        //CAN
        // SmartDashboard.putBoolean("Climber/SV/7. climbBrakeRel_SV", climbBrakeRel_SV.get()); //CAN
        SmartDashboard.putBoolean("Climber/SV/8. brakeTest", brakeTest);

        SmartDashboard.putBoolean("Climber/FB/1. AExt_L_FB", IO.lockPinAExt_L_FB.get());
        SmartDashboard.putBoolean("Climber/FB/2. AExt_R_FB", IO.lockPinAExt_R_FB.get());
        SmartDashboard.putBoolean("Climber/FB/3. BExt_L_FB", IO.lockPinBExt_L_FB.get());
        SmartDashboard.putBoolean("Climber/FB/4. BRet_R_FB", IO.lockPinBExt_R_FB.get());
        SmartDashboard.putBoolean("Climber/FB/5. SExt_L_FB", IO.sliderExt_L_FB.get());
        SmartDashboard.putBoolean("Climber/FB/6. SExt_R_FB", IO.sliderExt_R_FB.get());
        SmartDashboard.putString("Climber/FB/7. lockPinA Sts, L && B", lockPinA_Sts().toString());
        SmartDashboard.putString("Climber/FB/8. lockPinB Sts, L && B", lockPinB_Sts().toString());
        SmartDashboard.putString("Climber/FB/9. SExt Sts, L && B", slider_Sts().toString());
        SmartDashboard.putNumber("Climber/FB/10. Climber arm ddegrees", IO.climbLdMtr_Enc.degrees());



        ROT_SPD = SmartDashboard.getNumber("Climber/Mtr/1. ROT_SPD", 0.45);          
        // SmartDashboard.putNumber("Climber/Mtr/2. climbMotor", climberMotor.get());              //CAN
        SmartDashboard.putNumber("Climber/Mtr/3. Ld enc Ticks", IO.climbLdMtr_Enc.ticks());     //CAN
        IO.climbLdMtr_Enc.setTPF(SmartDashboard.getNumber("Climber/Mtr/4. climb TPD", 0.504));
        // SmartDashboard.putNumber("Climber/Mtr/5. climb Deg", IO.climbLdMtr_Enc.degrees());      //CAN
        // SmartDashboard.putNumber( "Climber/Mtr/6. Arm Ld Mtr Amps", climberMotor.getOutputCurrent());   //CAN
        // SmartDashboard.putNumber( "Climber/Mtr/7. Arm Fl Mtr Amps", IO.climbMotorFollow.getOutputCurrent());    //CAN


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
