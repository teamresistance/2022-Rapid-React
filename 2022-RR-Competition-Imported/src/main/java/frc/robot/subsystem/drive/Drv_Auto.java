package frc.robot.subsystem.drive;

import org.opencv.features2d.FlannBasedMatcher;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.robot.subsystem.drive.trajFunk.*;
import frc.util.PIDXController;

public class Drv_Auto extends Drive {

    private static int autoStep = 0;        //State for Drv_Auto
    private static ATrajFunction[] traj;    //Choosen array of trajectories, legs.
    private static int idx = 0;             //Index to choosen traj
    private static boolean allDone = false; //All legs done, path is done

    //Constructor.  Called with the path array
    public Drv_Auto() {
    }

    /**Get the choosen Trajectories and initialize indexes.
     * <p>Reset Heading & Distance to 0.
     */
    public static void init() {
        traj = Trajectories.getTraj(1.0);
        autoStep = 0;
        idx = 0;
        allDone = false;
        hdgRst();
        distRst();
        IO.coorXY.reset();
        IO.coorXY.drvFeetRst();

        //                      PIDX,   SP,      PB,     DB,   Mn,  Mx, Exp, Clamp
        PIDXController.setExt(pidDist, 0.0, (-1.0/0.5), 0.3, 0.35, 1.0, 2.0, true);
        PIDXController.setExt(pidHdg,  0.0,   (1.0/30), 2.0, 0.40, 1.0, 2.0, true);
        pidHdg.enableContinuousInput(-180.0, 180.0);    //All angles are converted to -180 to 180 (opps)

        System.out.println("Auto - Init");
    }

    /**
     * Called from Robot AutonomusPeriodic.
     * <p>Steps thru and executes the list of trajectoies retrieved in init.
     */
    public static void update() {
        sdbUpdate();
        switch (autoStep) {
        case 0:                 //Initialize the traj funk
            System.out.println("Auto - 0");
            ATrajFunction.initTraj(); // Sets traj[x] state to 0
            autoStep++;
            System.out.println("Traj Index: " + idx);
        case 1:                 //Run a leg of the path
            traj[idx].execute();
            if (ATrajFunction.getDone()) autoStep++;
            break;
        case 2:                 //Closeout Leg
            System.out.print("DONE Auto: ");
            System.out.println("\tCoorX: " + IO.coorXY.getX() + "\tCoorY " + IO.coorXY.getY() + 
                                "\tHdg " + hdgFB() + "\tDist: " + IO.coorXY.drvFeet());

            // cmdUpdate();        //Stop motors --added to stop after each leg
            setDriveCmds(0.0, 0.0, false, 0);   //Stop motors --added to stop after each leg
            idx++;
            // autoStep = idx < traj.length ? 0 : autoStep++;
            if( idx < traj.length ) {
                autoStep = 0;   //Go do next traj, leg.
                break;
            }
            autoStep++;
        case 3:                 //Done path
            System.out.println("---------- ALL DONE Auto: ");
            setAllDone();       //Flag allDone (not sure who we're flagging)
            // cmdUpdate();        //Stop Drive
            setDriveCmds(0.0, 0.0, false, 0);   //Stop motors --added to stop after each leg
            autoStep++;
        case 4:     //Loop here
            // cmdUpdate();        //Stop Drive
            setDriveCmds(0.0, 0.0, false, 0);   //Stop motors --added to stop after each leg
            break;
        }
    }

    private static void setAllDone() {
        allDone = true;
    }

    public static boolean getAllDone() {
        return allDone;
    }

    public static void disable() {
        // cmdUpdate();
        setDriveCmds(0.0, 0.0, false, 0);
    }

    public static void sdbInit() {
    }

    public static void sdbUpdate() {
        SmartDashboard.putNumber("Drv/Auto/Auto Step", autoStep);
        SmartDashboard.putNumber("Drv/Auto/Current Traj Idx", idx);
        SmartDashboard.putNumber("Drv/Auto/hdgFB", hdgFB());
        SmartDashboard.putNumber("Drv/Auto/distFB", distFB());
        SmartDashboard.putNumber("Drv/Auto/coorX", IO.coorXY.getX());
        SmartDashboard.putNumber("Drv/Auto/coorY", IO.coorXY.getY());
    }
}
