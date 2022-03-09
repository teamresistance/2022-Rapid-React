package frc.robot.subsystem.drive;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.vision.RPI;
import frc.robot.subsystem.drive.trajFunk.*;

public class Trajectories {
    private static double dfltPwr = 0.4;
    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static String[] chsrDesc = {
        "getEmpty", "getCargo1", "getCargo2", "getCargo3", "getCargo4", "getCargo5", "getCargo6", "SnorfShootTest",
    };

    /**Initialize Traj chooser */
    public static void chsrInit(){
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        chsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]);   //Default MUST have a different name
        SmartDashboard.putData("Drv/Traj/Choice", chsr);
        
    }

    /**Show on sdb traj chooser info.  Called from robotPeriodic  */
    public static void chsrUpdate(){
        SmartDashboard.putString("Drv/Traj/Choosen", chsr.getSelected());

    }

    /**
     * Get the trajectory array that is selected in the chooser Traj/Choice.
     * @param pwr - default pwr to be usedin trajectories
     * @return The active, selected, Chooser Trajectory for use by AutoSelector
     */
    public static ATrajFunction[] getTraj(double pwr){
        switch(chsr.getSelected()){
            case "getEmpty":
            return getEmpty(pwr);
            case "SnorfShootTest":
            return snorfShootTest(pwr);
            case "getCargo1":
            return getCargo1(pwr);            
            case "getCargo2":
            return getCargo2(pwr);
            case "getCargo3":
            return getCargo3(pwr);
            case "getCargo4":
            return getCargo4(pwr);
            case "getCargo5":
            return getCargo5(pwr);
            case "getCargo6":
            return getCargo6(pwr);
            default:
            System.out.println("Traj/Bad Traj Desc - " + chsr.getSelected());
            return getEmpty(0);
        }
    }

    /**
     * Get the trajectory array that is selected in the chooser Traj/Choice.
     * <p>Use a default power, 0.9.
     * 
     * @return The active, selected, Chooser Trajectory for use by AutoSelector
     */
    public static ATrajFunction[] getTraj(){
        return getTraj(dfltPwr);
    }


    public static String getChsrDesc(){
        return chsr.getSelected();
    }

    //------------------ Trajectories -------------------------------
    // each trajectory/path/automode is stored in each method
    // name each method by the path its doing

    public static ATrajFunction[] getEmpty(double pwr) {
        ATrajFunction[] traj = { 
            // new CoorOffset(24.0, -1.5, -3.5),
            // new TurnNMove(0.0, 0.0, 0.0),
        };
        return traj;
    }

    public static ATrajFunction[] snorfShootTest(double pwr) {
        ATrajFunction[] traj = {
            new MoveOnHdg(0.0, 5.0, 0.5),
            new TankTimed(0.3, -0.3, -0.3), //brake, -pwr is bkwd, +pwr fwd
            new TankTurnHdg(80.0, 0.5, -0.5),   //-pwr is bkwd, +pwr fwd
            new SnorfDrvAuto(true),
            new TurnNMove(90.0, 5.0, 0.5),
            new SnorfDrvAuto(false),
            new MoveOnHdg(90.0, -1.5),
            new TankTimed(0.3, 0.3, 0.3), //brake, +pwr is bkwd, -pwr fwd
            new TankTurnHdg(20.0, -0.5, 0.2),   //-pwr is bkwd, +pwr fwd
            new TurnNMove(20.0, 3.0, 0.5),
            new ShootDrvAuto(false), //Shoots high setting
            // new TurnNMove(90.0, -5.0, 0.5),
            // new TankTimed(0.2, 0.3, 0.3), //brake, +pwr is bkwd, -pwr fwd
            // new MoveOnHdg(180.0, 5.0, 0.5),

            // new TrajDelay(3.0),
            // new SnorfDrvAuto(true),
            // new TrajDelay(3.0),
            // new SnorfDrvAuto(false),
            // new ShootDrvAuto(false), //Shoots high setting
        };
        return traj;
    }

    public static ATrajFunction[] getCargo1(double pwr) { //LM
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),
            new ShootDrvAuto(false),
            new TurnNMove(-66.0, 0),
            new SnorfDrvAuto(true),
            // new Waypt(13.0, -2.0), // B Ball
            // new Waypt(7.0, -10.0), // A Ball
            new SnorfDrvAuto(false),
            // new Waypt(4, -1.0),
            new TurnNMove(-66.0, 0.5),
            new ShootDrvAuto(false),
        };
        return traj;
    }

    public static ATrajFunction[] getCargo2(double pwr) { //LR
        ATrajFunction traj[] = {
            // new CoorOffset(-66.0, 3.0, -2.5),
            // new ShootDrvAuto(false),
            // new TurnNMove(-66.0, 0),
            // new SnorfDrvAuto(true),
            // new Waypt(13.0, -2.0), // B Ball
            // new Waypt(7.0, -10.0), // A Ball
            // new SnorfDrvAuto(false),
            // new Waypt(2, -4.5),
            // new TurnNMove(-66.0, 0),
            // new ShootDrvAuto(false),
        };
        return traj;
    }
    
    public static ATrajFunction[] getCargo3(double pwr) { //LL
        ATrajFunction[] traj = {
            // new CoorOffset(-66.0, 4.5, 1.0),
            // new ShootDrvAuto(false),
            // new TurnNMove(-66.0, 0),
            // new SnorfDrvAuto(true),
            // new Waypt(13.0, -2.0), // A Ball
            // new Waypt(7.0, -10.0), // B Ball
            // new SnorfDrvAuto(false),
            // new Waypt(4.5, 1.0),
            // new TurnNMove(-66.0, 0),
            // new ShootDrvAuto(false),
        };
        return traj;
    }
    
    public static ATrajFunction[] getCargo4(double pwr) { //RM: P5, Shoot Hi , C,( G,) P5, Shoot Hi.
        pwr = 0.5;
        ATrajFunction traj[] = {
            // new CoorOffset(24.0, -1.5, -3.5),
            // new ShootDrvAuto(false),
            // // new TrajDelay(2.0),
            // new TurnNMove(24.0, -6.4, pwr), // already at 24 degrees, go back 7.4 feet
            // new TurnNMove(24.0, 0.5, pwr), // braking
            // new TankTurnHdg(-38, -0.1, 0.5), //Turns
            // new SnorfDrvAuto(true),
            // new TurnNMove(-38, 9.0, pwr),
            // //new Waypt(-7, -11, 0.3), // C Ball
            // new SnorfDrvAuto(false), //Snorfler up
            // new TurnNMove(-38, -11.0, pwr),
            // new TurnNMove(-38, 0.1, pwr), //brake
            // new TankTurnHdg(24.0, 0.5, -0.1), //turns
            // new TurnNMove(24.0, 5.5, pwr),
            // new TurnNMove(24.0, 1.5, 0.2),
            // //new TurnNMove()
            // // new Waypt(-2, -5), // back in front of shoot area
            // // new TurnNMove(24.0, 0.5), //forward a little
            // new ShootDrvAuto(false),
        };
        return traj;
    }

    public static ATrajFunction[] getCargo5(double pwr) { //RR: P6, Shoot Hi , C,( G,) P5, Shoot Hi.
        ATrajFunction traj[] = {
            // new CoorOffset(24.0, -1.5, -3.5),
            // new ShootDrvAuto(false),
            // new TurnNMove(24.0, -7.4),
            // new SnorfDrvAuto(true),
            // new Waypt(-7, -11), // C Ball
            // new SnorfDrvAuto(false),
            // new Waypt(-2, -5),
            // new TurnNMove(24.0, 0.5),
            // new ShootDrvAuto(false),
        };
        return traj;
    }    

    public static ATrajFunction[] getCargo6(double pwr) { //RL: P4, Shoot Hi , C,( G,) P5, Shoot Hi.
        ATrajFunction traj[] = {
            // new CoorOffset(24.0, -0.5, -4.0),
            // new ShootDrvAuto(false),
            // new TurnNMove(24.0, -7.4),
            // new SnorfDrvAuto(true),
            // new Waypt(-7, -11), // C Ball
            // new SnorfDrvAuto(false),
            // new Waypt(-2, -5),
            // new TurnNMove(24.0, 0.5),
            // new ShootDrvAuto(false),
        };
        return traj;
    }
    /*

    public static ATrajFunction[] blue1(double pwr) {
        ATrajFunction[] traj = {
            new CoorOffset(-66, -5, -5),
            new Waypt(-20, -20, pwr),

        };
        return traj;
    }*/

    // public static ATrajFunction[] getBounce(double pwr) {
    //     ATrajFunction[] traj = {
    //         new TurnNMove(0,   3.0, pwr), //2.8
    //         new TurnNMove(270, 3.4, pwr), //3.2
    //         new TurnNMove(270,-2.5, pwr),
    //         new TurnNMove(230,-5.5, pwr), //6.3
    //         new TurnNMove(315, 1.8, pwr), 
    //         new TurnNMove(270, 7.3, pwr), //6.8
    //         new TurnNMove(270,-6.2, pwr), //6.8
    //         new TurnNMove(0,   5.7, pwr), //6
    //         new TurnNMove(270, 8.2, pwr),
    //         new TurnNMove(270,-1.8, pwr), //2.4
    //         new TurnNMove(-10, 4.0, pwr)
    //      };
    //     return traj;
    // }

    // /**Runs a figure 8 pattern using various trajectory functions. First turn to the right.
    //  * Uses MoveOnHdg for straight runs.
    //  * <p>Est. for various radii
    //  * <p> 3.5' right 0.72.0, 0.0
    //  */
    // public static ATrajFunction[] getFigure8R(double pwr) {
    //     ATrajFunction traj[] = {
    //     new MoveOnHdg(0.0, 5.0),
    //     new CirToHdgTank(3.5, 5.5, 3.5, 135.0, -0.71, 0.0 ), //Turn right half circle
    //     // new CirToHdgTank(3.5, 0.0, 3.5, 270.0, 0.8, -0.2 ), //Turn right half circle
    //     // new CirToHdgTank(3.5, 0.0, 3.5, 45.0, 0.8, -0.2 ), //Turn right half circle
    //     // new MoveOnHdg(45, 6.4, 1.0),

    //     // new CirToHdgTank(-3.5, 0.0, 3.5, -135.0, -0.2, 0.8 ), //Turn left half circle
    //     };
    //     return traj;
    // }

    // /**Runs a figure 8 pattern using various trajectory functions. First turn to the left.
    //  * Uses MoveOnHdg for straight runs.
    //  */
    // public static ATrajFunction[] getFigure8L(double pwr) {
    //     // System.out.println("---------- Made it here: Traj Sq " + pwr + " ----------------");
    //     ATrajFunction traj[] = {
    //     new TankTurnHdg(180, 0.1, 0.85), //Turn left half circle
    //     new TankTurnHdg(-30, 0.1, 0.85),  //continue circle to 30, more then 360.
    //     new MoveOnHdg(-30.0, 11.0, 1.0),
    //     new TankTurnHdg(-180, 0.85, 0.1), //Turn right half circle
    //     new TankTurnHdg(-150, 0.85, 0.1),  //continue circle to 30, more then 360.
    //     new MoveOnHdg(-150, 11.0, 1.0),
    //     new TankTurnHdg(10, 0.1, 0.85), //Turn left half circle
    //     };
    //     return traj;
    // }

    // /**Runs a figure 8 pattern using various trajectory functions. First turn to the right.
    //  * Uses Waypoints for straight runs.
    //  */
    // public static ATrajFunction[] getFigure8WPT(double pwr) {
    //     pwr = 0.8;
    //     ATrajFunction traj[] = {
    //         new Waypt( 0.0, 9.0, 0.6),
    //         new Waypt( 9.0, 9.0, 1.0, 10),
    //         new Waypt( 9.0, 0.0, 1.0, 10),
    //         new Waypt( 0.0, 0.0, 1.0, 10),
    //         new TankTurnHdg(5, -0.8, 0.8)
    //     };
    //     return traj;
    // }

    // /**Runs a square pattern using TurnNMove trajectory function. */
    // public static ATrajFunction[] getSquare_TNM(double pwr) {
    //     // System.out.println("---------- Made it here: Traj Sq " + pwr + " ----------------");
    //     ATrajFunction traj[] = {
    //     new TurnNMove(0,   6, pwr),
    //     new TurnNMove(90,  6, pwr),
    //     new TurnNMove(180, 6, pwr),
    //     new TurnNMove(270, 6, pwr),
    //     new TurnNMove(360, 0, pwr)
    //     };
    //     return traj;
    // }

    // /**Runs a square pattern using MoveOnHdg trajectory function. */
    // public static ATrajFunction[] getSquare_MOH(double pwr) {
    //     // System.out.println("---------- Made it here: Traj Sq " + pwr + " ----------------");
    //     ATrajFunction traj[] = {
    //         new MoveOnHdg(   0, 6, 1.0, 30),
    //         new MoveOnHdg(  90, 6, 1.0, 30),
    //         new MoveOnHdg( 180, 6, 1.0, 30),
    //         new MoveOnHdg( 270, 6, 1.0, 30),
    //         new MoveOnHdg( 350, 0, 1.0, 30)
    //         // new MoveOnHdg(  90, 6, 1.0, 30),
    //         // new MoveOnHdg(   0, 6, 1.0, 30),
    //         // new MoveOnHdg( -90, 6, 1.0, 30),
    //         // new MoveOnHdg(-180, 6, 1.0, 30),
    //         // new MoveOnHdg(-350, 0, 1.0, 30)
    //     };
    //     return traj;
    // }

    // //-------------- Galaxtic Search ------------------------
    // //------------- Path in name open Snorfler --------------
    // public static ATrajFunction[] getPathRedA(double pwr) {
    //     ATrajFunction[] traj = {
    //         new SnorfDrvAuto(true),
    //         new TurnNMove(0,   3.0, pwr),
    //         new TurnNMove(25,  5.3, pwr),
    //         new TurnNMove(-85, 6.2, pwr),
    //         new TurnNMove(0,  10.3, pwr),
    //         new SnorfDrvAuto(false)
    //      };
    //     return traj;
    // }

    // public static ATrajFunction[] getPathBluA(double pwr) {
    //     ATrajFunction[] traj = { 
    //         new SnorfDrvAuto(true),
    //         new TurnNMove(22,  9.3, pwr),
    //         new TurnNMove(-70, 6.5, pwr),
    //         new TurnNMove(27,  4.0, pwr),
    //         new TurnNMove(10,  4.0, pwr),
    //         new SnorfDrvAuto(false)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getPathRedB(double pwr) {
    //     ATrajFunction[] traj = {
    //         new SnorfDrvAuto(true),
    //         new TurnNMove(-33, 3.5, 65),
    //         new TurnNMove(40,  7.0, pwr),
    //         new TurnNMove(-60, 7.1, pwr),
    //         new TurnNMove(-10, 6.5, pwr),
    //         new SnorfDrvAuto(false)
    //      };
    //     return traj;
    // }

    // public static ATrajFunction[] getPathBluB(double pwr) {
    //     ATrajFunction[] traj = {
    //         new SnorfDrvAuto(true),
    //         new TurnNMove(11,  1.0, pwr),
    //         new TurnNMove(-60, 5.0, pwr),
    //         new TurnNMove(40,  4.0, pwr),
    //         new TurnNMove(20,  4.0, pwr),
    //         new SnorfDrvAuto(false)
    //      };
    //     return traj;
    // }

    // public static ATrajFunction[] getPathBlue(double pwr) {
    //     ATrajFunction[] traj = {
    //         new SnorfDrvAuto(true),
    //         new TurnNMove(21,  9.5, pwr),  //Move to BlueA ball1
    //         new TurnNMove(-72, 8.7, pwr),  //Move to B6 thru BlueB ball1
    //         new TurnNMove(48, 10.0, pwr),   //Move to BlueAB ball3
    //         new SnorfDrvAuto(false)
    //      };
    //     return traj;
    // }

    // /**
    //  * Establishes the Trajectory array from the Raspberry Pi
    //  * @param pwr - applied default power to turns and runs
    //  * @return the Trajectoy array for the path assigned by the Raspberry Pi
    //  */
    // public static ATrajFunction[] getPathGalaxtic(double pwr) {
    //     switch (RPI.galacticShooter()) {
    //         case 1:
    //             return getPathRedA(pwr);
    //         case 2:
    //         return getPathBlue(pwr);   //inside Blue
    //         // return getPathBluB(70);   //inside Blue
    //         case 3:
    //             return getPathRedB(pwr);
    //         case 4:
    //             return getPathBlue(pwr);   //outside Blue
    //             // return getPathBluA(70);   //outside Blue
    //         default:
    //             System.out.println("Bad Galaxtic path - " + RPI.galacticShooter());
    //             return getEmpty(0);
    //     }
    // }

    // public static ATrajFunction[] getCurveTry(double fwd) {
    //     ATrajFunction traj[] = {
    //         new MoveOnHdg(0.0, 3.5, 1.0),
    //         new CirToHdgCurve(2.5, 3.5, 2.5, 135, 1.0, 0.5),
    //         // new CurveTurn(1.0, 0.9, 1.0),
    //         // new TurnNMove(0.0, 1.0, 4.0),
    //         // new CurveTurn(1.0, 1.0, 1.0),
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve1_1(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(1.0, 1.0, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve1_7(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(1.0, 0.75, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve1_5(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(1.0, 0.5, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve7_1(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.75, 1.0, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve7_7(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.75, 0.75, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve7_5(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.75, 0.5, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve5_1(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.5, 1.0, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve5_7(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.5, 0.75, 3.0)
    //     };
    //     return traj;
    // }

    // public static ATrajFunction[] getCurve5_5(double fwd) {
    //     ATrajFunction traj[] = {
    //         new CurveTurnTm(0.5, 0.5, 3.0)
    //     };
    //     return traj;
    // }

}
