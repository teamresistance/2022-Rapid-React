package frc.io.joysticks;
/*
Original Author: Joey & Anthony
Rewite Author: Jim Hofmann
History:
J&A - 11/6/2019 - Original Release
JCH - 11/6/2019 - Original rework
TODO: Exception for bad or unattached devices.
      Auto config based on attached devices and position?
      Add enum for jsID & BtnID?  Button(eLJS, eBtn6) or Button(eGP, eBtnA)
Desc: Reads joystick (gamePad) values.  Can be used for different stick configurations
    based on feedback from Smartdashboard.  Various feedbacks from a joystick are
    implemented in classes, Button, Axis & Pov.
    This version is using named joysticks to istantiate axis, buttons & axis
*/

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.io.joysticks.Axis;
import frc.io.joysticks.Button;
import frc.io.joysticks.Pov;

//TODO: ASSIGN BUTTON PORTS FOR EACH BUTTON INITIALIZED !!!

//Declares all joysticks, buttons, axis & pov's.
public class JS_IO {
    public static int jsConfig = 0; // 0=Joysticks, 1=gamePad only, 2=left Joystick only
                                    // 3=Mixed LJS & GP, 4=Nintendo Pad
    // Declare all possible Joysticks
    public static Joystick leftJoystick = new Joystick(0); // Left JS
    public static Joystick rightJoystick = new Joystick(1); // Right JS
    public static Joystick coJoystick = new Joystick(2); // Co-Dvr JS
    public static Joystick gamePad = new Joystick(3); // Normal mode only (not Dual Trigger mode)
    // public static Joystick neoPad = new Joystick(4); // Nintendo style gamepad
    // public static Joystick arJS[] = { leftJoystick, rightJoystick, coJoystick,
    // gamePad };
    // Declare all stick control

    // Drive
    public static Axis axLeftDrive = new Axis();    // Left Drive
    public static Axis axRightDrive = new Axis();   // Right Drive
    
    public static Axis axLeftY = new Axis();        // Left JS Y - Added for testing in Drive3
    public static Axis axLeftX = new Axis();        // Left JS X
    public static Axis axRightY = new Axis();       // Right JS Y
    public static Axis axRightX = new Axis();       // Right JS X

    public static Button btnScaledDrive = new Button();     // scale the drive
    public static Button btnInvOrientation = new Button();  // invert the orientation of the robot (joystick: forwards
                                                            // becomes backwards for robot and same for backwards)

    public static Button btnSelDrv = new Button();          //Added for testing in Drive3
    public static Button btnHoldZero = new Button();
    public static Button btnHold180 = new Button();

    // Shooter
    public static Button btn_high_fire = new Button();
    public static Button btn_low_fire = new Button() ;

    // Snorfler
    public static Button btnTglSnorArmDn = new Button();
    public static Button btnReverseSnorfler = new Button();

    // Climb
    public static Axis axClimb = new Axis();
    public static Button btnClimb = new Button();
    public static Button btnClimbOFF = new Button();
    public static Button btnClimb1 = new Button();
    public static Button btnClimb2 = new Button();

    // LimeLight - AS
    public static Button limeLightOnOff = new Button();

    // All
    public static Button allStop = new Button(); // stops all parts of the shooter sequence
    public static Button btnStop = new Button();
    // Misc
    public static Button btnRstGyro = new Button();
    public static Button record = new Button();

    // // Auto
    // public static Button drive2Off = new Button();
    // public static Button drive2Tank = new Button();
    // public static Button drive2Arcade = new Button();
    // public static Button drive2AutoTest = new Button();
    // public static Button resetGyro = new Button();
    // public static Button resetDist = new Button();
    // public static Pov pov_SP = new Pov();
    // public static Axis axRightX = new Axis();

    // Constructor
    public JS_IO() {
        init();
    }

    public static void init() {
        SmartDashboard.putNumber("JS/JS_Config", jsConfig);
        chsrInit();
        configJS();
    }

    private static SendableChooser<Integer> chsr = new SendableChooser<Integer>();
    private static String[] chsrDesc = {"3-Joysticks", "2-Joysticks", "Gamepad"};
    private static int[] chsrNum = {0, 1, 2};

    public static void chsrInit(){
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrNum[i]);
        }
        chsr.setDefaultOption(chsrDesc[2] + " (Default)", chsrNum[2]);   //Default MUST have a different name
        SmartDashboard.putData("JS/Choice", chsr);
        sdbUpdChsr();
    }

    public static void sdbUpdChsr(){
        SmartDashboard.putString("JS/Choosen", chsrDesc[chsr.getSelected()]);   //Put selected on sdb
    }

    // // can put this under a button press
    // public static void update() { // Chk for Joystick configuration
    //     if (jsConfig != SmartDashboard.getNumber("JS/JS_Config", 0)) {
    //     jsConfig = (int) SmartDashboard.getNumber("JS/JS_Config", 0);
    //     caseDefault();
    //     configJS();
    //     }
    // }

    // can put this under a button press
    public static void update() { // Chk for Joystick configuration
        //chsr.setDefaultOption doesn't appear to work.  Shouldn't need to trap null.
        //Default MUST have a different name
        if (jsConfig != (chsr.getSelected() == null ? 0 : chsr.getSelected())) {
            sdbUpdChsr();
            caseDefault();      //Clear exisitng jsConfig
            System.out.println("JS Chsn: " + chsr.getSelected());
            configJS();         //then assign new jsConfig
        }
    }

    public static void configJS() { // Default Joystick else as gamepad
        // jsConfig = (int) SmartDashboard.getNumber("JS_Config", 0);
        jsConfig = chsr.getSelected() == null ? 0 : chsr.getSelected();
        SmartDashboard.putNumber("JS/JS_Config", jsConfig);

        switch (jsConfig) {
            case 0: // Normal 3 joystick config
                norm3JS();
                break;

            case 1: // Normal 2 joystick config No CoDrvr
                norm2JS();
                break;

            case 2: // Gamepad only
                a_GP();
                break;

            default: // Bad assignment
                System.out.println("Bad JS choice - " + jsConfig);
                caseDefault();
                break;

        }
    }

    // ================ Controller actions ================

    // ----------- Normal 3 Joysticks -------------
    private static void norm3JS() {
        System.out.println("JS assigned to 3JS");

        // All stick axisesssss
        axLeftDrive.setAxis(leftJoystick, 1);
        axRightDrive.setAxis(rightJoystick, 1);
        // axClimb.setAxis(coJoystick, 1);

        axLeftX.setAxis(leftJoystick, 0);       //Added to test drive3
        axLeftY.setAxis(leftJoystick, 1);
        axRightX.setAxis(rightJoystick, 0);
        axRightY.setAxis(rightJoystick, 1);

        // Drive buttons
        btnSelDrv.setButton(rightJoystick, 4);     //Added to test drive3
        btnHoldZero.setButton(rightJoystick, 5);
        btnHold180.setButton(rightJoystick, 6);

        btnScaledDrive.setButton(rightJoystick, 3);
        btnInvOrientation.setButton(rightJoystick, 1);

        // snorfler buttons
        btnReverseSnorfler.setButton(coJoystick, 5);
        btnTglSnorArmDn.setButton(coJoystick, 3);

        
        // shooting buttons
        btn_high_fire.setButton(coJoystick, 1);
        btn_low_fire.setButton(coJoystick, 10);

        // climbing buttons
        btnClimb1.setButton(coJoystick, 6);
        btnClimb2.setButton(coJoystick, 7);


        // drive2Off.setButton(leftJoystick, 10);
        // drive2Tank.setButton(leftJoystick, 9);
        // drive2Arcade.setButton(leftJoystick, 11);
        // drive2AutoTest.setButton(leftJoystick, 12);

        // resetDist.setButton(leftJoystick, 5);
        // resetGyro.setButton(leftJoystick, 3);

        // pov_SP.setPov(coJoystick, 1);
    }

    // ----- gamePad only --------
    private static void a_GP() {
        System.out.println("JS assigned to GP");

        // All stick axisesssss
        axLeftDrive.setAxis(gamePad, 1); // left stick Y
        axRightDrive.setAxis(gamePad, 5); // right stick Y
      
        axLeftX.setAxis( gamePad, 0);       //Added to test drive3
        axLeftY.setAxis( gamePad, 1);
        axRightX.setAxis(gamePad, 4);
        axRightY.setAxis(gamePad, 5);

        // Drive buttons
        btnScaledDrive.setButton(gamePad, 4); // Y
        // btnHoldZero.setButton(gamePad, 5);  // LB
        // btnHold180.setButton(gamePad, 6);   // RB
        // btnInvOrientation.setButton(gamePad, 10); // r-stick push

        // snorfler buttons
        btnReverseSnorfler.setButton(gamePad, 9); // l-stick push
        btnTglSnorArmDn.setButton(gamePad, 1); // A

        // turret buttons
        // limeLightOnOff.setButton(gamePad, 5); // LB
        // btnLimeSearch.setButton(gamePad, 6); // RB
        // btnLimeAim.setButton(gamePad, 4); // Y

        // shooting buttons
        btn_high_fire.setButton(gamePad, 5);
        btn_low_fire.setButton(gamePad, 4);

        // btnRstGyro.setButton(leftJoystick, 3);

        // drive2Off.setButton(gamePad, 1);
        // drive2Tank.setButton(gamePad, 2);
        // drive2Arcade.setButton(gamePad, 3);
        // drive2AutoTest.setButton(gamePad, 4);
        // resetDist.setButton(gamePad, 5);
        // resetGyro.setButton(gamePad, 6);

        // pov_SP.setPov(gamePad, 0);
    }

    // ----------- Normal 2 Joysticks -------------
    private static void norm2JS() {
        System.out.println("JS assigned to 2JS");

        // All stick axisesssss
        axLeftDrive.setAxis(leftJoystick, 1);
        axRightDrive.setAxis(rightJoystick, 1);


        axLeftX.setAxis(leftJoystick, 0);       //Added to test drive3
        axLeftY.setAxis(leftJoystick, 1);
        axRightX.setAxis(rightJoystick, 0);
        axRightY.setAxis(rightJoystick, 1);

        // axClimb.setAxis(coJoystick, 1);

        // Drive buttons
        btnScaledDrive.setButton(leftJoystick, 3);
        btnInvOrientation.setButton(leftJoystick, 1);

        // snorfler buttons
        btnReverseSnorfler.setButton(rightJoystick, 5);
        btnTglSnorArmDn.setButton(rightJoystick, 3);

        // shooting buttons
        btn_high_fire.setButton(leftJoystick, 1);
        btn_low_fire.setButton(rightJoystick, 10);

        //Climbing Buttons
        btnClimb1.setButton(rightJoystick, 6);
        btnClimb2.setButton(rightJoystick, 7);


    }

    // ----------- Case Default -----------------
    private static void caseDefault() {
        // All stick axisesssss
        axLeftDrive.setAxis(null, 0);
        axRightDrive.setAxis(null, 0);

        axLeftX.setAxis(null, 0);       //Added to test drive3
        axLeftY.setAxis(null, 1);
        axRightX.setAxis(null, 0);
        axRightY.setAxis(null, 1);

        btnSelDrv.setButton(null, 4);     //Added to test drive3

        btnScaledDrive.setButton(null, 0); // scale the drive
        btnInvOrientation.setButton(null, 0); // invert the orientation of the robot (joystick: forwards becomes
                                              // backwards for robot and same for backwards)
        btnHoldZero.setButton(null, 0);
        btnHold180.setButton(null, 0);
        btn_high_fire.setButton(null, 0);
        btn_low_fire.setButton(null, 0);
        btnTglSnorArmDn.setButton(null, 0);
        btnReverseSnorfler.setButton(null, 0);
        btnClimb.setButton(null, 0);
        btnClimbOFF.setButton(null, 0);
        limeLightOnOff.setButton(null, 0);
        btnRstGyro.setButton(null, 0);
    }
}