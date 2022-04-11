package frc.robot.subsystem.drive;

import java.io.PrintWriter;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.vision.RPI;
import frc.robot.subsystem.drive.trajFunk.*;

public class Trajectories {
    private static double dfltPwr = 0.4;
    private static SendableChooser<String> chsr = new SendableChooser<String>();
    private static String[] chsrDesc = {
        "getEmpty",  "oneCargo", "twoBall_C", "threeBall_BG_Auto", "threeBall_BC_Auto"
    };
    // "ball3_A_B", "ball2_C","getCargo2", "getCargo3", "getCargo4", "getCargo5", 
    //     "getCargo6", "SnorfShootTest", "wayPtTest",

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
            case "ball3_A_B":   //Start at P2, shoot, backup & swing around to A then B, return to P2 & shoot 2.
            return ball3_A_B(pwr);
            case "ball2_C":     //Start at P7, get C, return to P5 and Shoot 2
            return ball2_C(pwr);
            case "ball1_X":     //Start at P?, Shoot 1, backup
            return ball1_X(pwr);
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
            case "wayPtTest":
            return wayPtTest(pwr);
            case "oneCargo":
            return oneBallAuto(pwr);
            case "twoBall_C":
            return twoBall_C(pwr);
            case "threeBall_BG_Auto":
            return threeBall_BG_Auto(pwr);
            case "threeBall_BC_Auto":
            return threeBall_BC_Auto(pwr);
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

    public static ATrajFunction[] getEmpty(double pwr) {
        ATrajFunction[] traj = { 
            // new CoorOffset(24.0, -1.5, -3.5),
            new TurnNMove(0.0, 0.0, 0.0),
        };
        return traj;
    }
    public static ATrajFunction[] oneBallAuto(double pwr){
        pwr = 0.3;
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.5, -3.5),
            new ShootDrvAuto(false, null),
            new TurnNMove(24.0, -4.0, pwr),
        };
        return traj;
    }

    public static ATrajFunction[] twoBall_C(double pwr){
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.5, -3.5),
            new ShootDrvAuto(false, null), // first shot
            new TurnNMove(24.0, -2.5), // backing up
            new TankTurnHdg(-135, -0.7, -0.5), // rotate to the right
            new TrajDelay(0.3), // give it a sec
            new SnorfDrvAuto(true), // snorf
            new MoveOnHdg(-135, 6.0, pwr),
            new TurnNMove(-135, 0.5, pwr), // move and drift towards ball
            new TrajDelay(0.3), // wait for ball pickup
            new SnorfDrvAuto(false), // unsnorf
            new TurnNMove(-135, -6.5, pwr), // go back
            new TankTurnHdg(24.0, 0.6, -0.3),
            new MoveOnHdg(24.0, 1.0, pwr),
            new TrajDelay(0.5),
            new ShootDrvAuto(null, false),
        };
        return traj;
    }

    public static ATrajFunction[] threeBall_BG_Auto(double pwr){
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),
            new ShootDrvAuto(false, null), //Shoots
            new TrajDelay(0.3),
            // new TurnNMove(-66.0, -1.5, pwr),
            new SnorfDrvAuto(true), //Snorfler Down
            new TankTurnHdg(-200.0, -0.7, -0.5), //Turns
            new TurnNMove(-204.0, 18.0, pwr), //Goes forward
            new TurnNMove(-205.0, 1.0, 0.3), //Change heading drop speed
            new TrajDelay(0.7),             //Coast
            new TurnNMove(-193.0, -18.1, pwr), //Goes back
            new SnorfDrvAuto(false), //Snorf off
            new TankTimed(0.2, pwr, pwr), //Brake
            // new TankTurnHdg(270.0, 0.2, -0.6),
            new TankTurnHdg(-66, 0.5, -0.2), //Turns
            new TurnNMove(-66.0, 1.0, pwr), //Forward
            new TankTimed(0.5, 0.3, 0.3),
            new TrajDelay(0.2),
            new ShootDrvAuto(false, false), //Shoot
            new TrajDelay(0.2),
            new TurnNMove(-66.0, -1.5),
            new TankTurnHdg(90, pwr, -pwr),
            // new TankTurnHdg(0.0, -0.4, -0.6), //Turn
            // new TankTurnHdg(90.0, 0.4,-0.6),


            // new Waypt(-10.0, 0.0, pwr),
            // new TankTurnHdg(-66.0, 0.5, 0.3),
            // new TurnNMove(-66.0, 2.5, pwr),
            // new ShootDrvAuto(false, false),
        };
        return traj;
    }

    public static ATrajFunction[] threeBall_BC_Auto(double pwr){
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),
            new ShootDrvAuto(false, null), //Shoots
            new TrajDelay(0.3), //Delay
            new TurnNMove(-66.0, -2.0, pwr), //Backs up
            new TankTurnHdg(15.0, -0.3, -0.7), //Turns
            new TurnNMove(15.0, -8.0, pwr),
            new SnorfDrvAuto(true),
            new TurnNMove(45.0, 5.0, pwr),
            new TurnNMove(25.0, 9.5, pwr),
            new TurnNMove(26.0, -3.3, pwr),
            new SnorfDrvAuto(false),
            new TankTurnHdg(-66.0, -0.9, 0.4),
            new TurnNMove(-66.0, 5.0),
            new TankTimed(0.4, 0.3, 0.3),
            new TrajDelay(0.6),
            new ShootDrvAuto(false, false),

        };
        return traj;
    }

    
    public static ATrajFunction[] oneBallReject_F(double pwr){
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(-156.0, -1.5, -3.5),
            new SnorfDrvAuto(true),
            new TurnNMove(-156.0, 3.0, pwr),
            new TankTurnHdg(-120.0, 0.7, -0.3),
            new SnorfDrvAuto(false),
            new TurnNMove(-120.0, 4.0, pwr),
            new TankTurnHdg(150.0, pwr, -pwr),
        };
        return traj;
    }


    public static ATrajFunction[] snorfShootTest(double pwr) {
        ATrajFunction[] traj = {
            // new CoorOffset(0.0, 0.0, 0.0),
            // new SnorfDrvAuto(true),
            // new Waypt(5.0, 5.0, 0.4),
            // new TankTurnHdg(90.0, 0.3, -0.5),
            // new SnorfDrvAuto(false),

            // new TurnNMove(0.0, 0.0, 0.3),
            // new TurnNMove(0.0, 10.0, 0.3),
            // new TankTimed(0.2, -0.2, -0.2)

            new ShootDrvAuto(true, null),
            new TurnNMove(0.0, -2.0, 0.5),
            // new ShootDrvAuto(true, null),
            // new TurnNMove(-45.0,74.0, 0.5),
            new Waypt(-3.0, 0.0, 0.5),
            new TankTurnHdg(0.0, 0.5, -0.2)

            // new MoveOnHdg(0.0, 5.0, 0.5),
            // new TankTimed(0.3, -0.3, -0.3), //brake, -pwr is bkwd, +pwr fwd
            // new TankTurnHdg(80.0, 0.5, -0.5),   //-pwr is bkwd, +pwr fwd
            // new SnorfDrvAuto(true),
            // new TurnNMove(90.0, 5.0, 0.5),
            // new SnorfDrvAuto(false),
            // new MoveOnHdg(90.0, -1.5),
            // new TankTimed(0.3, 0.3, 0.3), //brake, +pwr is bkwd, -pwr fwd
            // new TankTurnHdg(20.0, -0.5, 0.2),   //-pwr is bkwd, +pwr fwd
            // new TurnNMove(20.0, 3.0, 0.5),
            // new ShootDrvAuto(false), //Shoots high setting
            // // new TurnNMove(90.0, -5.0, 0.5),
            // // new TankTimed(0.2, 0.3, 0.3), //brake, +pwr is bkwd, -pwr fwd
            // // new MoveOnHdg(180.0, 5.0, 0.5),

            // // new TrajDelay(3.0),
            // // new SnorfDrvAuto(true),
            // // new TrajDelay(3.0),
            // // new SnorfDrvAuto(false),
            // // new ShootDrvAuto(false), //Shoots high setting
        };
        return traj;
    }

    /**
     * 3 ball auto.  Starting at P2 position against right fender and go for balls A & B.
     * @param pwr
     * @return
     */
    public static ATrajFunction[] ball3_A_B(double pwr) { //was getCargo1
        pwr = 1.0;
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),   //Adj offsets for P2 position
            new ShootDrvAuto(null, false),      //Shoot right ball high
            new TurnNMove(-66.0, -4.0, pwr),    //already at -66 degrees, go back 4 feet
            new TurnNMove(-66.0, 0.5, 0.5),     //brake
            new TankTurnHdg(90, 0.8, -0.2),     //Pivot right to 90 deg hdg, A ball
            new SnorfDrvAuto(true),             //Drop snorfler
            new TurnNMove(90.0, 2.0, 0.5),      //Fwd 2' along 90 deg.  Pick up A ball
            new TurnNMove(90.0, -1.5, 0.5),     //Back out
            new SnorfDrvAuto(false),            //Stops snorfler
            new TankTurnHdg(190.0, 1.0, -0.3),  //Pivot towards B ball at 195 deg hdg
            new SnorfDrvAuto(true),             //Start snorfle
            // new TurnNMove(190.0, 8.0, pwr),     //Move forward to B Ball
            new Waypt(7.0, -10.0, 0.8, 4),      //Move forward to B Ball
            new TurnNMove(190.0, -0.2, 0.3),    //Brake
            new SnorfDrvAuto(false),            //Stop snorfle
            new TurnNMove(190.0, -7.0, pwr),    //Move Back
            new TurnNMove(190.0, 0.2, 0.4),     //Brake
            new TankTurnHdg(-62.0, 1.0, -1.0),  //Turns, with adjustment for Fender (starting point)
            new TurnNMove(-66.0, 2.5, 0.5),     //Move to Fender
            new ShootDrvAuto(false),            //Shoot both
        };
        return traj;
    }

    /**
     * 2 ball auto.  Start at P7, get C, return to P5 and Shoot 2.
     * @param pwr
     * @return
     */
    public static ATrajFunction[] ball2_C(double pwr) {
        pwr = 1.0;
        ATrajFunction traj[] = {
            new CoorOffset(-165.0, -5.0, -5.0), //Adj offsets for P7 position
            new SnorfDrvAuto(true),             //Drop snorfler
            new TrajDelay(0.5),                 //Wait 0.5 sec for Snorfe to settle.
            new TurnNMove(-165.0, 7.0, pwr),    //already at -165 degrees, go fwd 7 feet
            new TurnNMove(-165.0, -0.5, -0.5),  //brake
            new SnorfDrvAuto(false),            //Stops snorfler
            new TankTurnHdg(24, 0.8, -0.2),     //Pivot right to 24 deg hdg, towards P5
            new TurnNMove(24.0, 10.0, pwr),     //Fwd 10' back to P5
            new TrajDelay(0.5),                 //Wait 0.5 sec for bot to settle.
            new ShootDrvAuto(false),            //Shoot both
        };
        return traj;
    }

    /**
     * 1 ball auto.  Start at P1-6, Shoot right, backup passed line.
     * @param pwr
     * @return
     */
    public static ATrajFunction[] ball1_X(double pwr) {
        pwr = 1.0;
        ATrajFunction traj[] = {
            // new CoorOffset(-66.0, 4.5, -0.5),   //Adj offsets for P1 position
            new CoorOffset(-66.0, 4.0, -1.5),   //Adj offsets for P2 position
            // new CoorOffset(-66.0, 3.5, -2.0),   //Adj offsets for P3 position
            // new CoorOffset(24.0, -2.8, -3.4),   //Adj offsets for P4 position
            // new CoorOffset(24.0, -1.8, -3.8),   //Adj offsets for P5 position
            // new CoorOffset(24.0, -0.8, -4.2),   //Adj offsets for P6 position
            new ShootDrvAuto(null, false),      //Shoot right
            new TankTimed(1.5, -pwr, -pwr)      //Backup ~10'
        };
        return traj;
    }

    public static ATrajFunction[] getCargo2(double pwr) { //3 Ball Auto Bu
        ATrajFunction traj[] = {
            new CoorOffset(-66.0, 4.0, -1.5),
            // new ShootDrvAuto(false),
            new ShootDrvAutoR(),
            new TurnNMove(-66.0, -4.0, 1.0), // already at -66 degrees, go back 4 feet
            new TurnNMove(-66.0, 0.5, 1.0),
            new TankTurnHdg(90, 1.0, -1.0), //Turns
            new SnorfDrvAuto(true), //Drop snorfler
            new TurnNMove(90.0, 3.0, 1.0), //Pick up first ball
            new TurnNMove(90.0, -2.5, 1.0), //Backs out
            new SnorfDrvAuto(false), //Stops snorfler
            new TankTurnHdg(-66.0, 1.0, -1.0), //Turn towards hub
            new TurnNMove(-66.0, 5.0, 1.0), //Move forward
            new TurnNMove(-66.0, -0.1, 1.0), //Brake
            new ShootDrvAuto(false),
        };
        return traj;
    }
    
    public static ATrajFunction[] getCargo3(double pwr) { //3 ball auto v2
        ATrajFunction[] traj = {
            new CoorOffset(-66.0, 4.0, -1.5),
            new ShootDrvAuto(false),
            new TankTurnHdg(0.0, -0.6, -pwr),
            new TurnNMove(0.0, -8.0),
            new TankTurnHdg(65.0, -0.6, -pwr),
            new SnorfDrvAuto(true), //Start snorfle
            new TurnNMove(65.0, 9.0, pwr), //Move forward
            new TurnNMove(65.0, -0.3, pwr), //Brake
            new SnorfDrvAuto(false), //Stop snorfle
            // new TurnNMove(220.0, -3.0), //Move Back
            // new TurnNMove(220.0, 0.1), //Brake
            // new TankTurnHdg(-62.0, 0.3, -0.3), //Turns, with adjustment for 
            // new TurnNMove(-62.0, 4.0),
            // new ShootDrvAuto(false),
        };
        return traj;
    }
    
    public static ATrajFunction[] getCargo4(double pwr) { //3 Ball Auto v1
        pwr = 0.5;
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.5, -3.5),
            // new ShootDrvAuto(false),
            new ShootDrvAuto(false, null),
            // new TrajDelay(0.2),
            new TurnNMove(24.0, -7.0, pwr), // already at 24 degrees, go back 7.4 feet
            new TurnNMove(24.0, 0.5, pwr), // braking
            new TankTurnHdg(-47, -1.0, 1.0), //Turns
            new SnorfDrvAuto(true),
            new TurnNMove(-47, 4.0, pwr),
            // // new Waypt(-7, -11, 0.3), // C Ball
            new SnorfDrvAuto(false), //Snorfler up
            new TurnNMove(-47, -2.0, pwr),
            new TurnNMove(-47, 0.2, pwr), //brake
            new TankTurnHdg(24.0, 1.0, -1.0), //turns
            new TurnNMove(24.0, 8.0, pwr), //Goes back
            new TurnNMove(24.0, 1.2, 0.2),
            new TrajDelay(1.0),
            new ShootDrvAuto(false),
            // // new Waypt(-2, -5), // back in front of shoot area
            // // new TurnNMove(24.0, 0.5), //forward a little
        };
        return traj;
    }

    public static ATrajFunction[] getCargo5(double pwr) { //RR: P6, Shoot Hi , C,( G,) P5, Shoot Hi.
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -1.5, -3.5),
            new ShootDrvAutoR(),
            new TurnNMove(24.0, -3, 0.6),
        };
        return traj;
    }    

    public static ATrajFunction[] getCargo6(double pwr) { //RL: P4, Shoot Hi , C,( G,) P5, Shoot Hi.
        ATrajFunction traj[] = {
            new CoorOffset(24.0, -0.5, -4.0),
            new Waypt(-3, -12, 1.0),
            new SnorfDrvAuto(true),
            new Waypt(-9, -10, 0.8),
            new SnorfDrvAuto(false),
            new Waypt(-3, -5, 0.7),
            new TankTurnHdg(-24, -0.3, 1.0),
        };
        return traj;
    }

    public static ATrajFunction[] wayPtTest(double pwr) { //RL: P4, Shoot Hi , C,( G,) P5, Shoot Hi.
        pwr = 0.3;
        ATrajFunction traj[] = {
            new CoorOffset(0.0, 0.0, 0.0),
            new Waypt(0.0, 5.0, pwr),
            // new TurnNMove(0.0,-0.7, 0.6) 
            new Waypt(5.0, 5.0, pwr),
        };
        return traj;
    }

    

}
