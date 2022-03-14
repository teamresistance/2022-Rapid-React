package frc.robot.subsystem.drive;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.IO;
import frc.robot.subsystem.drive.trajFunk.*;
import frc.io.hdw_io.util.Encoder_Pwf;
import frc.io.hdw_io.util.NavX;
import frc.io.joysticks.JS_IO;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.SPI;

public class PathWeaverTest extends Drive {

    private static int autoStep = 0;        //State for Drv_Auto
    private static ATrajFunction[] traj;    //Choosen array of trajectories, legs.
    private static int idx = 0;             //Index to choosen traj
    private static boolean allDone = false; //All legs done, path is done

    //PathWeaver Test
    private static AHRS gyro = new AHRS(SPI.Port.kMXP); //Refrences gyro again because i dont know where to look
    private static DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(0.2); //TODO: Check wheel width
    private static DifferentialDriveOdometry odometry = new DifferentialDriveOdometry(getHeading());

    private static SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(0.0, 0.0, 0.0); //TODO: Enter values

    private static PIDController leftPIDController = new PIDController(0, 0, 0); //TODO: Enter values
    private static PIDController rightPIDController = new PIDController(0, 0, 0); //TODO: Enter values

    private static Pose2d pose;

    public static Rotation2d getHeading(){
        return Rotation2d.fromDegrees(-gyro.getAngle()); //Gets gyroheading, but negative
    }
    // public static DifferentialDriveWheelSpeeds getSpeeds(){
    //     return new DifferentialDriveWheelSpeeds(
    //         //Gets wheel velocity, and converts it to meters per second
    //         IO.drvLead_L.getSpeed() * 2 * Math.PI * Units.inchesToMeters(4) /60, //TODO: Change wheel radius
    //         IO.drvLead_R.getSpeed() * 2 * Math.PI * Units.inchesToMeters(4) /60
    //     );
    //}
    
    public static void update() {
        sdbUpdate();        
        pose = odometry.update(getHeading(), IO.drvLead_L.getPosition(), IO.drvLead_R.getPosition());
    }

    public SimpleMotorFeedforward getFeedforward(){
        return feedforward;
    }

    public PIDController getRightPIDController(){
        return rightPIDController;
    }
    
    public PIDController getLeftPIDController(){
        return leftPIDController;
    }

    //Constructor.  Called with the path array
    public PathWeaverTest() {
    }

    /**Get the choosen Trajectories and initialize indexes.
     * <p>Reset Heading & Distance to 0.
     */
    public static void init() {
        // traj = Trajectories.getTraj(0.70);
        // autoStep = 0;
        // idx = 0;
        allDone = false;
        hdgRst();
        distRst();
        IO.coorXY.reset();
        IO.coorXY.drvFeetRst();

        System.out.println("Auto - Init");
    }

    /**
     * Called from Robot AutonomusPeriodic.
     * <p>Steps thru and executes the list of trajectoies retrieved in init.
     */

    private static void setAllDone() {
        
    }

    public static boolean getAllDone() {
        return allDone;
    }

    public static void disable() {
        cmdUpdate();
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
