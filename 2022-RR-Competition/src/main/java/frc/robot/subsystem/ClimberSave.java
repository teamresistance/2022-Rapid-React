package frc.robot.subsystem;

import edu.wpi.first.hal.simulation.RoboRioDataJNI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.io.hdw_io.util.ISolenoid;
import frc.io.hdw_io.util.InvertibleDigitalInput;
import frc.io.joysticks.JS_IO;
import frc.io.joysticks.util.Button;
import frc.util.Timer;
import frc.robot.subsystem.drive.Drive;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class ClimberSave {
  

    // Hardware Definitions
    private static CANSparkMax climberMotor = IO.climbMotor;   //Motor to rotate arm

    private static ISolenoid lockPinAExt = IO.lockPinAExt_SV;   //Extend locking pin A
    private static ISolenoid lockPinARet = IO.lockPinARet_SV;   //Retract locking pin A
    private static ISolenoid lockPinBExt = IO.lockPinBExt_SV;   //Extend locking pin B
    private static ISolenoid sliderExt = IO.sliderExt_SV;       //Extend Arm end A slider
    private static ISolenoid brakeRel = IO.climbBrakeRel_SV;    //Release the climber brake
    // Positive Feedback, FB, for extending the left & right locking pins & sliders
    //private static boolean lockPinAExt_FB(){return IO.lockPinAExt_L_FB.get() && IO.lockPinAExt_R_FB.get();}  //Or of pin A left & right
    //private static boolean lockPinBExt_FB(){return IO.lockPinBExt_L_FB.get() && IO.lockPinBExt_R_FB.get();}  //Or of pin A left & right
    //private static boolean sliderExt_FB(){return IO.sliderExt_L_FB.get() || IO.sliderExt_R_FB.get();}  //Or of pin A left & right

    // Joystick Buttons
    private static Button buttonClimb1 = JS_IO.btnClimb1;   //Both MUST be pressed tp
    private static Button buttonClimb2 = JS_IO.btnClimb2;   //start and continue climb
    private static Button btnClimbSlideRst = JS_IO.btnClimbSlideRst; //Reset Slider, state 0

    //Testing Button
    private static Button btnClimbStep = JS_IO.btnClimbStep; //For testing

    // Climber variables
    private enum eArmDir {
        OFF, // Rotational Motor speed is 0.0
        FWD, // Rotational Motor speed is +ROT_SPD
        REV  // Rotatioanl Motor speed is -ROT_SPD
    }
    private static double ROT_SPD = 0.45; //Arm motor fixed speed. Var for testing
    private static double armDegrees(){ return IO.climbLdMtr_Enc.degrees();}
    private static double drvSpdSaved;    // Used in case 90, for sdb only
    private static double armSpdSaved;    // Used in case 90, for sdb only
    private static eArmDir armDirSaved;   // Used in case 90, for sdb only
    private static boolean lockPinASaved; // Used in case 90, emerg. stop
    private static boolean lockPinBSaved; // Used in case 90, emerg. stop
    private static boolean sliderSaved;   // Used in case 90, emerg. stop
    private static eArmDir prvArmDir = eArmDir.OFF; //Used to delay when switching direction
    private static Timer armDlyTmr = new Timer(0.2);// Delay when switching direction
    private static Timer brakeTmr = new Timer(0.1);
    private static Timer armStartTmr = new Timer(0.1);

    private static boolean eStop = false;

    private static boolean climbEnabled = false; //Both Joysick buttons pressed
    private static int state = 0;       // Climber state machine. 0=Off
    private static boolean slideReset = false;
    private static int prvState = 0;    // Used to return from state 90, eStop.
    private static Timer stateTmr = new Timer(.05); // Timer for state machine
    private static boolean brakeTest = false;

    
    /**
     * RET - both retracted
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
        // sdbInit();
        cmdUpdate(0.0, 0.0, false, false, false);
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
        // sdbUpdate();
    }

    private static double pitch = 0.0;


    /**
     * State Machine update for climber
     */    
    private static void smUpdate() {

        pitch = IO.navX.getRoll();  //roboRIO turned 90.  No we are NOT moving it!
        
        switch (state) {
            case 0: // Turns everything off
                //        drv, arm, pinA,  pinB,  Slide
                cmdUpdate(0.0, 0.0, false, false, false);
                IO.climbLdMtr_Enc.reset();
                IO.climbMotor.setInverted(false);
                stateTmr.clearTimer();
                break;
            //----- Prep arm and move to low bar -----
            case 1: //Postion arm vertical, 'A' up for climb, low bar
                //Reinitialize the motors, bc maybe missing on startup
                IO.climbMotor.restoreFactoryDefaults();
                IO.climbMotor.setInverted(false);
                IO.climbMotor.setIdleMode(IdleMode.kBrake);
        
                IO.climbMotorFollow.restoreFactoryDefaults();
                IO.climbMotorFollow.setInverted(false);
                IO.climbMotorFollow.setIdleMode(IdleMode.kBrake);
                IO.climbMotorFollow.follow(IO.climbMotor);     //Disabled for testing


                //Drive.setHdgHold(180.0);    // Set drive steering to hold 180 heading
                cmdUpdate(0.0, 0.01, false, false, true );
                if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 2: // Move backwards (positive Y cmd) 6' to start climb position
                cmdUpdate(0.4, ROT_SPD, false,false,true);
                IO.coorXY.drvFeetRst();
                if(armDegrees() > 85.0){
                    state++;
                }
                
                // if (IO.coorXY.drvFeet() < -6.0) state++;
                break;
            case 3: // Move backwards 3' until robot pitches 15 degrees fwd/down
                cmdUpdate(0.2, 0.0, false, false, true);
                if(pitch < -10.0){        //USe 15 for comp, 5 for testing.
                    if(stateTmr.hasExpired(0.5, true)) state++;
                }else{
                    stateTmr.clearTimer();
                }
                break;
            //------ In contact with low arm, lock on and start climb. -----
            case 4: // Stop the driving and grab bar with LockPinA
                cmdUpdate(0.0, 0.0, true, false, true);
                if (lockPinA_Sts() == eStsFB.EXT) state++;
                IO.coorXY.drvFeetRst();
                break;
            case 5: // Robot moves forward (negative Y cmd) 3 feet and arm starts rotating forward
                cmdUpdate(-0.2, ROT_SPD * 1.15, true, false, true);
                state++;
                // if (IO.coorXY.drvFeet() > 3.0) state++;
                break;
            case 6: // Stop forward wheel motion
                cmdUpdate(0.0, ROT_SPD, true, false, true);

                // if(stateTmr.hasExpired(5.0, false)) 
                //     state++;

                state++;
                break;
            case 7: // Continue arm rotation until arm degrees GT 250 for 0.5 sec.
                cmdUpdate(0.0, ROT_SPD, true, false, true);
                if(IO.climbLdMtr_Enc.degrees() > 250.0){    //
                    if(stateTmr.hasExpired(0.5, true)) state++;
                    System.out.println("Degrees @7: " + armDegrees());
                }else{
                    stateTmr.clearTimer();
                }

                //Manual btn press by driver
                // if(btnClimbStep.onButtonPressed()) state++;
                
                break;
            //----- Contact Medium bar. Grab it. Release low arm and clear it.
            //----- Climb to transversal bar -----
            case 8: // Latch onto second bar with Pin B. Stop rotation.
                cmdUpdate(0.0, ROT_SPD/1.75, true, true, true);
                if (lockPinB_Sts() == eStsFB.EXT) state++;
                break;
            case 9: // Retract Pin A in order to release the first bar
                cmdUpdate(0.0, ROT_SPD/1.75, false, true, true);
                if (stateTmr.hasExpired(0.25, state)){;
                    if (lockPinA_Sts() == eStsFB.RET) state++;
                }
                break;
            case 10: // Rotating upward/REV to clear slider.  //Do we need this one?  Doesn't hurt.
                cmdUpdate(0.0, ROT_SPD/1.75, false, true, true);
                state++;
                // if (stateTmr.hasExpired(0.5, state)) state++;
                break;
            //=============== Changes here ==============
            case 11: // Retract the slider arm.  Slider will retract when it clears the bar.
                cmdUpdate(0.0, -ROT_SPD, false, true, true);
                // System.out.println("Degrees @11: " + armDegrees());
                //Chg this to use the degrees.
                if (stateTmr.hasExpired(0.5, state)) state++;   //1.0 @ 30%, 
                // if (stateTmr.hasExpired(1.0, state))     //Old
                //     if (!sliderExt_FB()) state++;        //Old
                break;
            case 12: // Motor Union Break Timer.  Stop before chging direction
                cmdUpdate(0.0, -ROT_SPD/2, false, true, false);
                // System.out.println("Degrees @12: " + armDegrees());
                // System.out.println("Amps @12 @12: " + IO.pdp.getCurrent(2) + " " + IO.pdp.getCurrent(3));
                if (slider_Sts() == eStsFB.RET) state++;   //New
                // if (stateTmr.hasExpired(0.5, state)) state++;   //Old
                break;
            //=========== To here ==================    
                case 13: // Go forward towards transversal bar, passed the low bar.
                cmdUpdate(0.0, ROT_SPD, false, true, false);
                //Replace with degrees
                if (stateTmr.hasExpired(2.5, state)) state++;
                break;
            case 14: // Slider cleared low bar, extend the slider.  Keep going forward.
                cmdUpdate(0.0, ROT_SPD, false, true, true);
                if (slider_Sts() == eStsFB.EXT)state++;
                break;
            case 15: // Keep going till arm degrees GT 420 for 0.5 sec.
                cmdUpdate(0.0, ROT_SPD * 1.25, false, true, true);
                if(IO.climbLdMtr_Enc.degrees() > 415.0){    //
                    if(stateTmr.hasExpired(0.5, true)) state++;
                    System.out.println("Degrees @15: " + armDegrees());
                }else{
                    stateTmr.clearTimer();
                }

                //Manual btn press by driver
                // if(btnClimbStep.onButtonPressed()) state++;

                break;
            //----- Contact Traversal bar. Grab it. Release Medium bar.
            //----- Lift off Medium bar then hold.  Done. -----
            case 16: // latch the transversal with pin A.
                cmdUpdate(0.0, ROT_SPD, true, true, true);
                if (lockPinA_Sts() == eStsFB.EXT) state++;
                break;
            case 17: // no more union break
                cmdUpdate(0.0, ROT_SPD, true, true, true);
                state++;
                // if (stateTmr.hasExpired(0.1, state)) state++;
                break;
            case 18: // Release the B pin
                cmdUpdate(0.0, ROT_SPD, true, false, true);
                if (lockPinB_Sts() == eStsFB.RET) state++;
                break;
            case 19: // Rotating Reverse to give leeway
                cmdUpdate(0.0, -ROT_SPD, true, false, true);
                if(IO.climbLdMtr_Enc.degrees() < 330.0){    //
                    if(stateTmr.hasExpired(0.25, true)) state++;
                    System.out.println("Degrees @19: " + armDegrees());
                }else{
                    stateTmr.clearTimer();
                }

                // if (stateTmr.hasExpired(1.7, state)) state++;   //Manually stopped

                break;
            case 20: // Turn arm motor off.  --- DONE! --- 
                     // Degrees was near 25 degrees. Higher is 30 degrees
                cmdUpdate(0.0, 0.0, true, false, true);
                break;

            //------ Resync Slider after DS shutdown when sliders are extended.
            case 80: // Turn on slider & pin A, no motors, wait then next.
                cmdUpdate(0.0, 0.0, true, false, true);
                slideReset = true;
                if (stateTmr.hasExpired(0.5, state)){
                    if (slider_Sts() == eStsFB.EXT && lockPinA_Sts() == eStsFB.EXT) state++;
                }
                break;
            case 81: // Turn off pin A, wait then next.
                cmdUpdate(0.0, 0.0, false, false, true);
                if (stateTmr.hasExpired(0.5, state)){
                    if (lockPinA_Sts() == eStsFB.RET) state++;
                }
                break;
            case 82: // Turn off slider, wait then return to state 0.
                cmdUpdate(0.0, 0.0, false, false, false);
                if ((slider_Sts() == eStsFB.RET) && (lockPinA_Sts() == eStsFB.RET)){
                    state = 0;
                    slideReset = false;
                }
                break;

            //----- Driver released buttoms during climb.  Stop & hold postion!
            case 90: // Emergency stop. Re-issue last commands
                cmdUpdate(0.0, 0.0, lockPinASaved, lockPinBSaved, sliderSaved);
                eStop = true;
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

    private boolean invert = false;

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
        armSpdSaved = rotSpd;
        lockPinASaved = pinA_Ext;
        lockPinBSaved = pinB_Ext;
        sliderSaved = slider_Ext;
        if(state != 90) prvState = state;

        //Drive.cmdUpdate(drvSpd, 0.0, false, 2); //Send cmd to drive system as arcade

        if(armDegrees() < -10.0) rotSpd = 0.0;    //SAFETY!  Stop rot motor if arm moved Rev too far.
        if(armDegrees() < -5.0) state = 1;
        
        if(eStop && (state != 90)){
            if(!armStartTmr.hasExpired(0.3, true)){
                rotSpd = -rotSpd;
            }else{
                eStop = false;
                armStartTmr.clearTimer();
            }
        }

        climberMotor.set(rotSpd);
        brakeTest = climbEnabled || rotSpd != 0.0;
        brakeRel.set(brakeTest); // authorized by Joel
        //System.out.println("CmdUpdateBrake: " + brakeRel.get());
    
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
        SmartDashboard.putBoolean("Climber/SV/6. brakeRel", brakeRel.get());
        SmartDashboard.putBoolean("Climber/SV/7. brakeTest", brakeTest);

        SmartDashboard.putNumber( "Climber/FB/4. Feet", IO.coorXY.drvFeet());

        //SmartDashboard.putBoolean("Climber/Climb/AExt_L_FB", IO.lockPinAExt_L_FB.get());
        //SmartDashboard.putBoolean("Climber/Climb/AExt_R_FB", IO.lockPinAExt_R_FB.get());
        //SmartDashboard.putBoolean("Climber/Climb/BExt_L_FB", IO.lockPinBExt_L_FB.get());
        //SmartDashboard.putBoolean("Climber/Climb/BRet_R_FB", IO.lockPinBExt_R_FB.get());
        //SmartDashboard.putBoolean("Climber/Climb/SExt_L_FB", IO.sliderExt_L_FB.get());
        //SmartDashboard.putBoolean("Climber/Climb/SExt_R_FB", IO.sliderExt_R_FB.get());
        SmartDashboard.putString("Climber/Climb/lockPinA Sts, L && B", lockPinA_Sts().toString());
        SmartDashboard.putString("Climber/Climb/lockPinB Sts, L && B", lockPinB_Sts().toString());
        SmartDashboard.putString("Climber/Climb/SExt Sts, L && B", slider_Sts().toString());


        // ROT_SPD = SmartDashboard.getNumber("Climber/Mtr/1. ROT_SPD", 0.3);
        SmartDashboard.putNumber("Climber/Mtr/2. climbMotor", climberMotor.get());
        SmartDashboard.putNumber("Climber/Mtr/3. Ld enc Ticks", IO.climbLdMtr_Enc.ticks());
        IO.climbLdMtr_Enc.setTPF(SmartDashboard.getNumber("Climber/Mtr/4. climb TPD", 300.0));
        SmartDashboard.putNumber("Climber/Mtr/5. climb Deg", IO.climbLdMtr_Enc.degrees());
        SmartDashboard.putNumber( "Climber/Mtr/6. Arm Ld Mtr Amps", climberMotor.getOutputCurrent());
        SmartDashboard.putNumber( "Climber/Mtr/7. Arm Fl Mtr Amps", IO.climbMotorFollow.getOutputCurrent());
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
