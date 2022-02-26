package frc.io.joysticks;
/*
Original Author: Joey & Anthony
Rewite Author: Jim Hofmann
History:
J&A - 11/6/2019 - Original Release
JCH - 11/6/2019 - Original rework
JCH - 2/13/2022 - Got rid of jsConfig num.  Use chooser
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
import frc.io.joysticks.util.Axis;
import frc.io.joysticks.util.Button;
import frc.io.joysticks.util.Pov;

//TODO: ASSIGN BUTTON PORTS FOR EACH BUTTON INITIALIZED !!!

//Declares all joysticks, buttons, axis & pov's.
public class JS_IO {
    private static int jsConfig;
    private static String prvJSAssign;

    // Declare all possible Joysticks
    public static Joystick leftJoystick = new Joystick(0);  // Left JS
    public static Joystick rightJoystick = new Joystick(1); // Right JS
    public static Joystick coJoystick = new Joystick(2);    // Co-Dvr JS
    public static Joystick gamePad = new Joystick(3);       // Normal mode only (not Dual Trigger mode)

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
    public static Button btnHoldZero = new Button();    //Rotate to 0 hdg and only apply fwd/rev
    public static Button btnHold180 = new Button();     //Rotate to 180 hdg and only apply fwd/rev

    // Snorfler
    public static Button btnSnorfle = new Button();    //Toggle snorfling
    public static Button btnRejectSnorfle = new Button();
    public static Button btnBadColor = new Button(); 

    // Shooter
    public static Axis axGoalSel = new Axis();          //Slider to select goal, dn is low, up is hi
    public static Button btnFire = new Button();        //Catapult at selected goal
    public static Button btnRejectLeft = new Button();  //Reject the Left cargo as low shot
    public static Button btnRejectRight = new Button(); //Reject the Right cargo as low shot

    // Climb
    public static Button btnClimb1 = new Button();  //1 of 2 buttons needed to enable climb
    public static Button btnClimb2 = new Button();  //The other

    // Misc
    public static Button btnRstGyro = new Button();
    public static Button btnRstFeet = new Button();
    public static Button btnRstCoorXY = new Button();

    // Constructor not needed, bc
    public JS_IO() {
        init();
    }

    public static void init() {
        chsrInit();
        configJS();
    }

    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static final String[] chsrDesc = {"3-Joysticks", "2-Joysticks", "Gamepad"};

    public static void chsrInit(){
        prvJSAssign = chsrDesc[0];
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        chsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]);   //Default MUST have a different name
        SmartDashboard.putData("JS/Choice", chsr);
        sdbUpdChsr();
    }

    public static void sdbUpdChsr(){
        SmartDashboard.putString("JS/Choosen", chsr.getSelected());   //Put selected on sdb
    }

    public static void update() { // Chk for Joystick configuration
        if (prvJSAssign != (chsr.getSelected() == null ? chsrDesc[0] : chsr.getSelected())) {
            sdbUpdChsr();
            caseDefault();      //Clear exisitng jsConfig
            System.out.println("JS Chsn: " + chsr.getSelected());
            configJS();         //then assign new jsConfig
            prvJSAssign = chsr.getSelected();
        }
    }

    public static void configJS() { // Default Joystick else as gamepad
        for(jsConfig = 0; jsConfig < chsrDesc.length; jsConfig++){
            if(prvJSAssign == chsrDesc[jsConfig]) break;
        }
        switch (jsConfig) {
            // case chsrDesc[0]: // Normal 3 joystick config
            // case "3-Joysticks": // Normal 3 joystick config
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
                System.out.println("Bad JS choice - " + prvJSAssign);
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
        btnHoldZero.setButton(rightJoystick, 5);
        btnHold180.setButton(rightJoystick, 6);

        btnScaledDrive.setButton(rightJoystick, 3);
        btnInvOrientation.setButton(rightJoystick, 1);
        btnHoldZero = new Button(leftJoystick, 8);    //Rotate to 0 hdg and only apply fwd/rev
        btnHold180 = new Button(leftJoystick, 9);     //Rotate to 180 hdg and only apply fwd/rev

        // snorfler buttons
        btnSnorfle.setButton(coJoystick, 3);
        btnRejectSnorfle.setButton(coJoystick, 3);
        btnBadColor.setButton(coJoystick, 3);
        
        // shooting buttons
        axGoalSel.setAxis(coJoystick, 3);
        btnFire.setButton(coJoystick, 1);
        btnRejectLeft.setButton(coJoystick, 6);
        btnRejectRight.setButton(coJoystick, 7);
        btnRejectSnorfle.setButton(coJoystick, 6);

        // climbing buttons
        btnClimb1.setButton(coJoystick, 6);
        btnClimb2.setButton(coJoystick, 7);

    // Misc
        btnRstGyro = new Button(rightJoystick, 6);
        btnRstFeet = new Button(rightJoystick, 7);
        btnRstCoorXY = new Button(rightJoystick, 8);

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
        btnSnorfle.setButton(gamePad, 1); // A


        // shooting buttons
        
    }

    // ----------- Normal 2 Joysticks -------------
    private static void norm2JS() {
    }

    // ----------- Case Default -----------------
    private static void caseDefault() {
        // All stick axisesssss
        axLeftDrive.setAxis(null, 0);
        axRightDrive.setAxis(null, 0);

        axLeftX.setAxis(null, 0);       //Added to test drive3
        axLeftY.setAxis(null, 0);
        axRightX.setAxis(null, 0);
        axRightY.setAxis(null, 0);

        btnScaledDrive.setButton(null, 0); // scale the drive
        btnInvOrientation.setButton(null, 0); // invert the orientation of the robot (joystick: forwards becomes
        btnHoldZero.setButton(null, 0);
        btnHold180.setButton(null, 0);

        // snorfler buttons
        btnSnorfle.setButton(null, 0);
        
        // shooting buttons
        axGoalSel.setAxis(null, 0);
        btnFire.setButton(null, 0);
        btnRejectSnorfle.setButton(null, 0);


        // climbing buttons
        btnClimb1.setButton(null, 0);
        btnClimb2.setButton(null, 0);

    // Misc
        btnRstGyro = new Button(null, 0);
        btnRstFeet = new Button(null, 0);
        btnRstCoorXY = new Button(null, 0);

    }
}