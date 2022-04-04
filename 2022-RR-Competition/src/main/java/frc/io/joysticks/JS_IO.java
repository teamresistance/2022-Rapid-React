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
    public static Joystick neoPad = new Joystick(4);        // Nintendo pamepad

    // Drive
    public static Axis axLeftDrive = new Axis();    // Left Drive
    public static Axis axRightDrive = new Axis();   // Right Drive
    
    public static Axis axLeftY = new Axis();        // Left JS Y - Added for testing in Drive3
    public static Axis axLeftX = new Axis();        // Left JS X
    public static Axis axRightY = new Axis();       // Right JS Y
    public static Axis axRightX = new Axis();       // Right JS X
    public static Axis axCoDrvY = new Axis();       // Co Drvr JS Y
    public static Axis axCoDrvX = new Axis();       // Co Drvr JS X

    public static Button btnScaledDrive = new Button();     // scale the drive
    public static Button btnInvOrientation = new Button();  // invert the orientation of the robot (joystick: forwards
                                                            // becomes backwards for robot and same for backwards)
    public static Button btnHoldZero = new Button();        //Rotate to 0 hdg and only apply fwd/rev
    public static Button btnHold180 = new Button();         //Rotate to 180 hdg and only apply fwd/rev

    // Snorfler
    public static Button btnSnorfle = new Button();         //Toggle snorfling
    public static Button btnRejectSnorfle = new Button();   //Reject ball, reverse motors
    public static Button btnBadColor = new Button();        //For testing auto reject wrong color ball

    // Shooter
    public static Axis axGoalSel = new Axis();          //Slider to select goal, dn is low, up is hi
    public static Button btnFire = new Button();        //Catapult at selected goal
    public static Button btnRejectLeft = new Button();  //Reject the Left cargo as low shot
    public static Button btnRejectRight = new Button(); //Reject the Right cargo as low shot

    // Climb
    public static Button btnClimb1 = new Button();  //1 of 2 buttons needed to enable climb
    public static Button btnClimb2 = new Button();  //The other
    public static Button btnClimbSlideRst = new Button();   //Reset slider to state 0
    public static Button btnClimbStep = new Button();   //Used to run a selected Trajectory for testing.

    // Misc
    public static Button btnRst = new Button();     //Reset Gyro, gyro offset, dist & coorXY
    public static Button btnAuto = new Button();    //Used to test Auto Trajectories

    // public static Button btnRstGyro = new Button(); //Reset Gyro to 0
    // public static Button btnRstFeet = new Button(); //Reset feet to 0
    // public static Button btnRstCoorXY = new Button(); //Reset coorXY to 0's

    // Constructor not needed, bc
    public JS_IO() {
        init();
    }

    public static void init() {
        chsrInit(); //Setup JS chooser and set JS assignments to default.
    }

    //---- Joystick controller chooser ----
    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static final String[] chsrDesc = {"3-Joysticks", "2-Joysticks", "Gamepad", "Nintendo"};

    /** Setup the JS Chooser */
    public static void chsrInit(){
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        chsr.setDefaultOption(chsrDesc[0], chsrDesc[0]);    //Chg index to select chsrDesc[] for default
        SmartDashboard.putData("JS/Choice", chsr);
        update();   //Update the JS assignments
    }

    /** Chk if chsr changed, update Joystick configuration */
    public static void update() {
        if (prvJSAssign != (chsr.getSelected())){   //If chsr chgd update JS assignments
            prvJSAssign = chsr.getSelected();
            configJS();         //Assign new jsConfig
            SmartDashboard.putString("JS/Choosen", chsr.getSelected());   //Put selected on sdb
            System.out.println("JS Chsn: " + chsr.getSelected());
        }
    }

    /**Configure a new JS assignment */
    public static void configJS() { // Configure JS controller assignments
        caseDefault();          //Clear exisitng jsConfig

        // // Convert selected to a integer, index
        // for(jsConfig = 0; jsConfig < chsrDesc.length; jsConfig++){
        //     if(prvJSAssign == chsrDesc[jsConfig]) break;
        // }

        // switch (jsConfig) { //then assign new assignments
        //     case 0: // Normal 3 joystick config
        //         norm3JS();
        //         break;
        //     case 1: // Normal 2 joystick config No CoDrvr
        //         norm2JS();
        //         break;
        //     case 2: // Gamepad only
        //         a_GP();
        //         break;
        //     default: // Bad assignment
        //         System.out.println("Bad JS choice - " + prvJSAssign);
        //         break;
        // }

        switch (prvJSAssign) {  //then assign new assignments
            case "3-Joysticks": // Normal 3 joystick config
                norm3JS();
                break;
            case "2-Joysticks": // Normal 2 joystick config No CoDrvr
                norm2JS();
                break;
            case "Gamepad":     // Gamepad only
                a_GP();
                break;
            case "Nintendo":    // Nintendo only
                a_NP();
                break;
            default:            // Bad assignment
                System.out.println("Bad JS choice - " + prvJSAssign);
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

        axLeftX.setAxis(leftJoystick, 0);       //Common call for each JS x & Y
        axLeftY.setAxis(leftJoystick, 1);
        axRightX.setAxis(rightJoystick, 0);
        axRightY.setAxis(rightJoystick, 1);
        axCoDrvX.setAxis(coJoystick, 0);
        axCoDrvY.setAxis(coJoystick, 1);

        // Drive buttons
        btnScaledDrive.setButton(rightJoystick, 3);
        btnInvOrientation.setButton(rightJoystick, 1);
        btnHoldZero.setButton(leftJoystick, 5);    //Rotate to 0 hdg and only apply fwd/rev
        btnHold180.setButton(leftJoystick, 6);     //Rotate to 180 hdg and only apply fwd/rev

        // snorfler buttons
        btnSnorfle.setButton(coJoystick, 3);
        btnRejectSnorfle.setButton(coJoystick, 5);
        // btnBadColor.setButton(coJoystick, 2);       //For Testing ball reject snorfler
        
        // shooting buttons
        axGoalSel.setAxis(coJoystick, 3);
        btnFire.setButton(coJoystick, 1);
        btnRejectLeft.setButton(coJoystick, 4);
        btnRejectRight.setButton(coJoystick, 6);

        // climbing buttons
        btnClimb1.setButton(coJoystick, 11);
        btnClimb2.setButton(coJoystick, 11);    //----------- Temporary one button ----------
        // btnClimb2.setButton(coJoystick, 12);
        btnClimbSlideRst.setButton(coJoystick, 7);

        // Misc
        btnRst = new Button(leftJoystick, 3);
        btnAuto = new Button(coJoystick, 9);
        // btnRstGyro = new Button(leftJoystick, 7);
        // btnRstFeet = new Button(leftJoystick, 8);
        // btnRstCoorXY = new Button(leftJoystick, 9);

    }

    // ----- gamePad only --------
    private static void a_GP() {
        System.out.println("JS assigned to GP");

        // All stick axisesssss
        axLeftDrive.setAxis(gamePad, 1);
        axRightDrive.setAxis(gamePad, 5);

        axLeftX.setAxis(gamePad, 0);       //Added to test drive3
        axLeftY.setAxis(gamePad, 1);
        axRightX.setAxis(gamePad, 4);
        axRightY.setAxis(gamePad, 5);
        // axCoDrvX.setAxis(gamePad, 2);
        // axCoDrvY.setAxis(gamePad, 3);

        // Drive buttons
        btnScaledDrive.setButton(gamePad, 7);       //7 (Back)
        // btnInvOrientation.setButton(gamePad, 1);    //??
        btnHoldZero.setButton(gamePad, 10);     //10 (RJB) Rotate to 0 hdg and only apply fwd/rev
        btnHold180.setButton(gamePad, 9);       //9  (LJB) Rotate to 180 hdg and only apply fwd/rev

        // snorfler buttons
        btnSnorfle.setButton(gamePad, 5);       //5 (RB)
        // btnRejectSnorfle.setButton(gamePad, 5); //??
        
        // shooting buttons
        axGoalSel.setAxis(gamePad, 3);          //3 (RTgr)
        btnFire.setButton(gamePad, 6);          //6 (RB)
        btnRejectLeft.setButton(gamePad, 3);    //3 (X)
        btnRejectRight.setButton(gamePad, 2);   //2 (B)

        // climbing buttons
        btnClimb1.setButton(gamePad, 8);        //8 (Start)
        btnClimb2.setButton(gamePad, 8);        //8 (Start) one button, not 2
        btnClimbSlideRst.setButton(gamePad, 4); //4 (Y)

        // Misc
        btnRst = new Button(gamePad, 1);        //1 (A)
        // btnAuto = new Button(gamePad, 9);

        // btnRstGyro = new Button(gamePad, 7);
        // btnRstFeet = new Button(gamePad, 8);
        // btnRstCoorXY = new Button(gamePad, 9);

    }

    // ----------- Normal 2 Joysticks -------------
    private static void norm2JS() {
    }

    // ----------- Nintendo gamepad -------------
    private static void a_NP() {
        // Drive buttons
        btnScaledDrive.setButton(neoPad, 2);    //(A)
        // btnInvOrientation.setButton(gamePad, 1);    //??
        btnHoldZero.setButton(neoPad, 3);       //(B)
        btnHold180.setButton(neoPad, 4);        //(Y)

        // Misc
        btnRst = new Button(neoPad, 1);        //(X)
        // btnAuto = new Button(gamePad, 9);

    }

    // ----------- Case Default -----------------
    private static void caseDefault() {
        // All stick axises
        axLeftDrive.setAxis(null, 0);
        axRightDrive.setAxis(null, 0);

        axLeftX.setAxis(null, 0);
        axLeftY.setAxis(null, 0);
        axRightX.setAxis(null, 0);
        axRightY.setAxis(null, 0);
        axCoDrvY.setAxis(null, 0);
        axCoDrvX.setAxis(null, 0);

        btnScaledDrive.setButton(null, 0);
        btnInvOrientation.setButton(null, 0);

        btnHoldZero.setButton(null, 0);
        btnHold180.setButton(null, 0);

        // snorfler buttons
        btnSnorfle.setButton(null, 0);
        btnRejectSnorfle.setButton(null, 0);
        btnBadColor.setButton(null, 0);
        
        // shooting buttons
        axGoalSel.setAxis(null, 0);
        btnFire.setButton(null, 0);
        btnRejectLeft.setButton(null, 0);
        btnRejectRight.setButton(null, 0);

        // climbing buttons
        btnClimb1.setButton(null, 0);
        btnClimb2.setButton(null, 0);
        btnClimbSlideRst.setButton(null, 0);
        btnClimbStep.setButton(null, 0);

    // Misc
        btnAuto = new Button(null, 0);
        btnAuto = new Button(null, 0);

        // btnRstGyro = new Button(null, 0);
        // btnRstFeet = new Button(null, 0);
        // btnRstCoorXY = new Button(null, 0);

    }
}