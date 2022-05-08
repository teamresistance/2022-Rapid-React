package frc.robot.subsystem.drive;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystem.drive.trajFunk.*;

public class Trajectories {
    private static double dfltPwr = 0.4;
    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static String[] chsrDesc = {
        "getEmpty",  "oneBall_X", "twoBall_C_Old", "twoBall_C", "threeBall_BG", "threeBall_AB", "getTesting", "oneBall_X_Test"
    };
    // "ball3_A_B", "ball2_C","getCargo2", "getCargo3", "getCargo4", "getCargo5", 
    //     "getCargo6", "SnorfShootTest", "wayPtTest",

    /** Optional position for 'some' Trajectories.  */
    private static SendableChooser<Integer> chsrAutoPos = new SendableChooser<Integer>();

    /**Initialize Traj chooser */
    public static void chsrInit(){
        //Initialize Auto Trajectory to run
        for(int i = 0; i < chsrDesc.length; i++){
            chsr.addOption(chsrDesc[i], chsrDesc[i]);
        }
        chsr.setDefaultOption(chsrDesc[0] + " (Default)", chsrDesc[0]);   //Default MUST have a different name
        SmartDashboard.putData("Drv/Traj/Traj Choice", chsr);

        //IF USED: Initialize position trajectory to start at.  --- Testing ---
        for(int i = 0; i <= 7; i++){
            chsrAutoPos.addOption("P" + i, i);
        }
        chsrAutoPos.setDefaultOption("P0 - not used", 0);
        SmartDashboard.putData("Drv/Traj/Position Choice", chsrAutoPos);

        //IF USED: Initialize position trajectory to start at.  --- Testing ---
        SmartDashboard.putNumber("Drv/Traj/Position Number", 0);    //Set default to 0 (which defaults to 2)
    }

    /**Show on sdb traj chooser info.  Called from robotPeriodic  */
    public static void chsrUpdate(){
        SmartDashboard.putString("Drv/Traj/Traj Chosen", chsr.getSelected());
        SmartDashboard.putNumber("Drv/Traj/Position Chosen", chsrAutoPos.getSelected());
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
            case "oneBall_X":
            return oneBall_X(pwr);
            case "twoBall_C":
            return twoBall_C(pwr);
            case "twoBall_C_Old":
            return twoBall_C_Old(pwr);
            case "threeBall_BG":
            return threeBall_BG(pwr);
            case "threeBall_AB":
            return threeBall_AB(pwr);
            case "getTesting":
            return getTesting(pwr);
            case "oneBall_X_Test":
            // return oneBall_X_Test(pwr, chsrAutoPos.getSelected());       //As a choser
            return oneBall_X_Test(pwr, 
                    (int)SmartDashboard.getNumber("Drv/Traj/Position Number", 0));  //As a number
            default:
            System.out.println("Traj/Bad Traj Desc - " + chsr.getSelected());
            return getEmpty(pwr);
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

    /**
     * No action auto.
     * @param pwr - default power to apply to trajectories
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] getEmpty(double pwr) {
        ATrajFunction[] traj = { 
            // new CoorOffset(24.0, -1.5, -3.5),
            new TurnNMove(0.0, 0.0, 0.0)            // Needed to prevent error in Drv_Auto, something to chew on.
        };
        return traj;
    }

    /**
     * 1 ball auto.  Start at P1 - 6, Shoot left then back up passed line.
     * @param pwr - default power to apply to trajectories
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] oneBall_X(double pwr){
        pwr = 0.3;
        ATrajFunction traj[] = {
            // new CoorOffset(-66.0, 4.5, -0.5),   //Adj offsets, P1, Right Fender right position
            new CoorOffset(-66.0, 4.0, -1.5),   //Adj offsets, P2, Right Fender center position
            // new CoorOffset(-66.0, 3.5, -2.0),   //Adj offsets, P3, Right Fender left position
            // new CoorOffset(24.0, -2.8, -3.4),   //Adj offsets, P4, Left Fender left position
            // new CoorOffset(24.0, -1.8, -3.8),   //Adj offsets, P5, Left Fender center position
            // new CoorOffset(24.0, -0.8, -4.2),   //Adj offsets, P6, Left Fender right position
            new ShootDrvAuto(false, null),      //Shoot left
            new TankTimed(1.5, -pwr, -pwr)      //Backup ~10', Used time so we can start in any position.
            // new TurnNMove(24.0, -4.0, pwr),     //Need to chg to match starting position.
        };
        return traj;
    }

    /**
     * 2 ball auto.  Start at P5, shoot, swing around to C, return to P5 & shoot right (single ball fills right first).
     * @param pwr - default power to apply to trajectories
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] twoBall_C(double pwr){
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.8, -3.8),   //Adj offsets, P5, Left Fender center position
            // new CoorOffset(24.0, -1.5, -3.5),
            new ShootDrvAuto(false, null),      // first shot
            new TurnNMove(24.0, -2.5),          // backing up
            new TankTurnHdg(-130, -0.7, -0.5),  // rotate to the right
            new TrajDelay(0.3),                 // give it a sec
            new SnorfDrvAuto(true),             // snorf
            new TurnNMove(-130, 6.0, pwr),
            new TurnNMove(-130, 0.5, pwr),      // move and drift towards ball
            new TrajDelay(0.3),                 // wait for ball pickup
            new TurnNMove(-130, -5.0, pwr),     // go back
            new SnorfDrvAuto(false),            // unsnorf
            new TankTurnHdg(24.0, 0.7, -0.5),   // turn to P5
            new MoveOnHdg(24.0, 3.0, pwr),      // move to fender
            new TrajDelay(0.5),                 // wait to settle time
            new ShootDrvAuto(null, false),      // shoot right
        };
        return traj;
    }

    public static ATrajFunction[] twoBall_C_Old(double pwr) { //2 Ball Auto v1
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.5, -3.5),
            new ShootDrvAuto(false, null),
            // new TrajDelay(0.2),
            new TurnNMove(24.0, -8.5, pwr), // already at 24 degrees, go back 7.4 feet
            new TurnNMove(24.0, 0.5, pwr), // braking
            new TankTurnHdg(-47, -1.0, 1.0), //Turns
            new SnorfDrvAuto(true),
            new TurnNMove(-47, 4.0, pwr),
            // // new Waypt(-7, -11, 0.3), // C Ball
            new TurnNMove(-47, -2.0, pwr),
            new TurnNMove(-47, 0.2, pwr), //brake
            new SnorfDrvAuto(false), //Snorfler up
            new TankTurnHdg(24.0, 1.0, -1.0), //turns
            new TurnNMove(28.0, 7.5, pwr), //Goes back
            new TurnNMove(28.0, 1.2, 0.2),
            new TrajDelay(1.0),
            new ShootDrvAuto(null, false),
        };
        return traj;
    }
    /**
     * 3 ball auto.  Start position P2, right fender center, and go for balls B & G.
     * @param pwr - default power to apply to trajectories
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] threeBall_BG(double pwr){
        pwr = 0.7;
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),   // Adj offsets, P2, Right Fender center position
            new ShootDrvAuto(false, null),      // Shoot Left
            new TrajDelay(0.3),                 // wait
            // new TurnNMove(-66.0, -1.5, pwr),
            new SnorfDrvAuto(true),             // Snorfler Down
            new TankTurnHdg(-200.0, -0.9, -0.7),// Turn towards B ball
            new TurnNMove(-204.0, 18.0, pwr),   // Goes forward
            new TurnNMove(-205.0, 1.0, 0.5),    // Change heading drop speed
            new TrajDelay(0.7),                 // Coast, close to human station
            new TurnNMove(-193.0, -18.1, pwr),  // Goes back towards goal
            new SnorfDrvAuto(false),            // Snorf off
            new TankTimed(0.2, pwr, pwr),       // Brake
            // new TankTurnHdg(270.0, 0.2, -0.6),
            new TankTurnHdg(-66, 0.7, -0.4),    // Turn towards goal
            new TurnNMove(-66.0, 1.0, pwr),     // Forward
            new TankTimed(0.5, 0.3, 0.3),       // Brake
            new TrajDelay(0.2),                 // Bot settle time
            // new ShootDrvAuto(false, false),     //Shoot both
            new ShootDrvAuto(false),            // Shoot both
            new TrajDelay(0.2),
            new TurnNMove(-66.0, -1.5),         // Back up for teleop position, as time allows
            new TankTurnHdg(90, pwr, -pwr),     // Turn to A ball
            // new TankTurnHdg(0.0, -0.4, -0.6),  //Turn
            // new TankTurnHdg(90.0, 0.4,-0.6),


            // new Waypt(-10.0, 0.0, pwr),
            // new TankTurnHdg(-66.0, 0.5, 0.3),
            // new TurnNMove(-66.0, 2.5, pwr),
            // new ShootDrvAuto(false, false),
        };
        return traj;
    }

    /**
     * 3 ball auto.  Start position P2, right fender center, and go for balls A & B.
     * @param pwr - default power to apply to trajectories
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] threeBall_AB(double pwr){
        pwr = 0.7;
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),   // Adj offsets, P2, Right Fender center position
            new ShootDrvAuto(false, null),      // Shoots
            new TrajDelay(0.3),                 // Delay
            new TurnNMove(-66.0, -1.5, 1.0),    // Backs up
            new TankTurnHdg(15.0, -0.5, -0.9),  // Turn passed facing A ball
            new TurnNMove(5.0, -8.0, pwr),     // backup passed B ball
            new SnorfDrvAuto(true),             // Drop Snorfler
            new TankTurnHdg(40.0, 0.7, -0.7),
            new TurnNMove(45.0, 5.0, pwr),      // Turn to B ball and snorfle it
            new TurnNMove(25.0, 6.0, 0.6),      // Move to A ball
            new TurnNMove(26.0, -1.3, pwr),     // Snorfle A ball
            new SnorfDrvAuto(false),            // Usnorfle
            new TankTurnHdg(-66.0, -0.9, 0.6),  // Turn towards Fender p2
            new TurnNMove(-66.0, 5.0),          // Move to Fender
            new TankTimed(0.4, 0.3, 0.3),       // Tight to fender
            new TrajDelay(0.6),                 // wait settle time
            new ShootDrvAuto(false),            // shoot both
            // new ShootDrvAuto(false, false),     // shoot both

        };
        return traj;
    }

    /** Sonorflw Test */
    public static ATrajFunction[] getTesting(double pwr) {
        ATrajFunction[] traj = {
            // MOH_Shoot test
            new CoorOffset(0.0, 0.0, 0.0),          //No offsets
            new MOH_Shoot(0.0, 10.0, 22.0, 0.5, 10, 7),     //0.0 hdg for 10', shoot at 8'
            // new MOH_Shoot(0.0, 8.0, 10.0, 0.7),     //0.0 hdg for 8', drift to 10' then shoot
            // new MOH_Shoot(0.0, 8.0, 10.0, 0.7, 180, 6), //0.0 hdg for 8', drift to 10' then shoot right only
            new TrajDelay(1.0),                     //time to shoot needed if drifting
            new TankTimed(0.2, -0.2, -0.2),          //brake

            // //Reverse shot test
            // new CoorOffset(0.0, 0.0, 0.0),          //No offsets
            // new SnorfDrvAuto(true),
            // new TrajDelay(0.5),
            // new MoveOnHdg(0.0, 3.0, 0.5),     //0.0 hdg for 10', shoot at 8'
            // new MOH_Shoot(10.0, -10.0, -6.8, 1.0, 5, 7),
            // new TurnNMove(10.0, -7.0, 1.0),     //0.0 hdg for 10', shoot at 8'
            // new ShootDrvAuto(false, false),
            // new TrajDelay(1.0),                     //time to shoot needed if drifting
            // new TankTimed(0.2, -0.2, -0.2)          //brake
            
        };
        return traj;
    }

    /**
     * 1 ball auto.  Start at P1 - 6, Shoot left then back up passed line.
     * Testing if we can pass the starting position.
     * Don't know how useful but might be interesting.
     * @param pwr - default power to apply to trajectories
     * @param startPos - starting position, P1 - 7.  Default P2  
     * @return An array of Traj Functions, commands to control the robot autonomously.
     */
    public static ATrajFunction[] oneBall_X_Test(double pwr, int startPos){
        pwr = 0.3;
        ATrajFunction traj[] = {
            startPosition(startPos),
            new ShootDrvAuto(false, null),      //Shoot left
            new TankTimed(1.5, -pwr, -pwr)      //Backup ~10', Used time so we can start in any position.
            // new TurnNMove(24.0, -4.0, pwr),     //Need to chg to match starting position.
        };
        return traj;
    }

    /**
     * 
     * @param startPos Starting position, P1 - 7.  -1 clears all offsets.  Else use P2.
     * @return CoorOffset() for selected starting position.
     */
    private static CoorOffset startPosition(int startPos){
        switch(startPos){
            case -1: return new CoorOffset(0.0, 0.0, 0.0);      //Adj offsets to 0, clear offsets.
            case 1: return new CoorOffset(-66.0, 4.5, -0.5);    //Adj offsets, P1, Right Fender right position
            case 2: return new CoorOffset(-66.0, 4.0, -1.5);    //Adj offsets, P2, Right Fender center position
            case 3: return new CoorOffset(-66.0, 3.5, -2.0);    //Adj offsets, P3, Right Fender left position
            case 4: return new CoorOffset(24.0, -2.8, -3.4);    //Adj offsets, P4, Left Fender left position
            case 5: return new CoorOffset(24.0, -1.8, -3.8);    //Adj offsets, P5, Left Fender center position
            case 6: return new CoorOffset(24.0, -0.8, -4.2);    //Adj offsets, P6, Left Fender right position
            case 7: return new CoorOffset(-165.0, -5.0, -5.0);  //Adj offsets, P7, Near C ball
            default:
                System.out.println("Bad Start position requested - " + startPos);
                return new CoorOffset(-66.0, 4.0, -1.5);        //Adj offsets, P2, Right Fender center position
        }
    }
}
