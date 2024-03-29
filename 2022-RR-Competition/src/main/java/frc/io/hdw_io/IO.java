package frc.io.hdw_io;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.motorcontrol.Victor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.io.hdw_io.util.*;
import frc.io.joysticks.JS_IO;
import frc.robot.subsystem.drive.Drive;
import edu.wpi.first.wpilibj.PneumaticsModuleType;

import com.playingwithfusion.CANVenom;
import com.playingwithfusion.CANVenom.BrakeCoastMode;
import com.playingwithfusion.CANVenom.ControlMode;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
// import com.revrobotics.ColorSensorV3;
// import edu.wpi.first.wpilibj.I2C;


public class IO {
    // navX
    public static NavX navX = new NavX(SPI.Port.kMXP);

    // PDP
    public static PowerDistribution pdp = new PowerDistribution(0,ModuleType.kCTRE);

    // Air
    public static Compressor compressor1 = new Compressor(1,PneumaticsModuleType.CTREPCM);
    public static Compressor compressor2 = new Compressor(2,PneumaticsModuleType.CTREPCM);
    public static Relay compressorRelay = new Relay(0);

    // Drive
    public static CANVenom drvLead_R = new CANVenom(11); // Cmds left wheels. Includes encoders
    public static CANVenom drvFollower_R = new CANVenom(12); // Resrvd 7 & 8 maybe
    public static CANVenom drvLead_L = new CANVenom(15); // Cmds right wheels. Includes encoders
    public static CANVenom drvFollower_L = new CANVenom(16); // Resrvd 3 & 4 maybe
    //As of 2022 DifferentialDrive no longer inverts the right motor.  Do this in the motor controller.
    // public static DifferentialDrive diffDrv_M = new DifferentialDrive(IO.drvLead_L, IO.drvLead_R);
                                        // 3.191
    public static double tpfAll = 4.17;
    public static double drvLeadTPF_L = -tpfAll;            // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static double drvFollowerTPF_L = -tpfAll;        // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static double drvLeadTPF_R = tpfAll * 0.87;      // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static double drvFollowerTPF_R = -tpfAll * 0.87; // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static Encoder_Pwf drvLdEnc_L = new Encoder_Pwf(drvLead_L, drvLeadTPF_L);  //Interface for feet, ticks, reset
    public static Encoder_Pwf drvLdEnc_R = new Encoder_Pwf(drvLead_R, drvLeadTPF_R);
    public static Encoder_Pwf drvFlEnc_L = new Encoder_Pwf(drvFollower_L, drvFollowerTPF_L);  //Interface for feet, ticks, reset
    public static Encoder_Pwf drvFlEnc_R = new Encoder_Pwf(drvFollower_R, drvFollowerTPF_R);
    private static boolean resetEnc = false;

    public static CoorSys coorXY = new CoorSys(navX, drvLdEnc_L, drvLdEnc_R, drvFlEnc_L, drvFlEnc_R);   //CoorXY & drvFeet

    // Snorfler
    public static Victor snorfFeed_Mtr = new Victor(0);     //Feed motor on snorfler
    public static Victor snorfElv_Mtrs = new Victor(1);    //Lower elevator motor
    public static ISolenoid snorflerExt_SV = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 1, false); // Extends both feeders

    // public static ColorSensorV3 ballColorSensor = new ColorSensorV3(I2C.Port.kOnboard);

    // Shooter
    public static ISolenoid select_low_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 1);    // Defaults hi prs; true for lo prs
    public static ISolenoid catapult_L_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 0); // Left catapult trigger. 
    public static ISolenoid catapult_R_SV = new InvertibleSolenoid(1, PneumaticsModuleType.CTREPCM, 2);// Right catapult trigger.

    // Climb
    public static CANSparkMax  climbMotor       = new CANSparkMax(6, MotorType.kBrushless);
    public static CANSparkMax climbMotorFollow = new CANSparkMax(7, MotorType.kBrushless);
    //TODO: Change number
    public static double climbLdMtr_TPD = 0.605; // 1024 t/r (0.5' * 3.14)/r 9:60 gr = 385.4  calibrated= 364.63
    public static Encoder_Neo climbLdMtr_Enc = new Encoder_Neo(climbMotor, climbLdMtr_TPD);
    //Amongus
    public static ISolenoid climbBrakeRel_SV   = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 0, false);
    public static ISolenoid lockPinAExt_SV = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 4);
    public static ISolenoid lockPinARet_SV = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 5);
    public static ISolenoid lockPinBExt_SV = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 3, true);
    public static ISolenoid sliderExt_SV   = new InvertibleSolenoid(2, PneumaticsModuleType.CTREPCM, 2, false);
    public static InvertibleDigitalInput lockPinAExt_L_FB = new InvertibleDigitalInput(1,true);
    public static InvertibleDigitalInput lockPinAExt_R_FB = new InvertibleDigitalInput(2,true);
    public static InvertibleDigitalInput lockPinBExt_L_FB = new InvertibleDigitalInput(3,true);
    public static InvertibleDigitalInput lockPinBExt_R_FB = new InvertibleDigitalInput(4,true);
    public static InvertibleDigitalInput sliderExt_L_FB   = new InvertibleDigitalInput(5,false);
    public static InvertibleDigitalInput sliderExt_R_FB   = new InvertibleDigitalInput(6,false);
    
    /**
     * Initialize any hardware
     */
    public static void init() {
        drvsInit();
        motorsInit();
        climberMtrsInit();  //Called here and Climber case 1, due to CAN issues.
        coorXY.reset();
        coorXY.drvFeetRst();
        sdbInit();
    }

    public static void update() {
        //Resets navX, angle offset, coorXY & offsets to zero.  
        //Also set scaled driving for climbing
        if (JS_IO.btnRst.onButtonPressed()){    //LB 3 / GP 1(A) / NP1(X)
            IO.navX.setAngleAdjustment(0.0);
            IO.navX.reset();
            IO.coorXY.setXY_OS(0.0, 0.0);
            IO.coorXY.drvFeetRst();
            IO.coorXY.reset();
            // Drive.setScaledOut(0.3);
            SmartDashboard.putNumber("Drv/Tele/Drive Scale", 0.5);
            Drive.setScaled(true);
            // Drive.setHdgHold(0.0);  //To release press and release btnHoldZero
        }
        // if (JS_IO.btnRstGyro.onButtonPressed())  IO.navX.reset();           //LJS btn 7
        // if (JS_IO.btnRstFeet.onButtonPressed())  IO.coorXY.drvFeetRst();    //LJS btn 8
        // if (JS_IO.btnRstCoorXY.onButtonPressed())  IO.coorXY.reset();       //LJS btn 9
           
        sdbUpdate();
    }

    /**
     * Initialize drive configuration setup.
     */
    public static void drvsInit() {
        // -------- Configure Lead drive motors ---------
        // drvLead_L.configFactoryDefault();    //No equivalent
        drvLead_L.setInverted(false); // Inverts motor direction and encoder if attached
        drvLead_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        drvLead_L.setControlMode(ControlMode.Proportional);
        // drvLead_L.setSensorPhase(false); // Adjust this to correct phasing with motor

        // drvLead_R.configFactoryDefault();
        drvLead_R.setInverted(false); // Inverts motor direction and encoder if attached
        drvLead_R.setBrakeCoastMode(BrakeCoastMode.Brake);
        drvLead_R.setControlMode(ControlMode.Proportional);
        // drvLead_R.setSensorPhase(false); // Adjust this to correct phasing with motor

        // ----- Tells left and right second drive motors to follow the Lead -----
        // drvFollower_L.configFactoryDefault();
        drvFollower_L.setInverted(true);
        drvFollower_L.setBrakeCoastMode(BrakeCoastMode.Brake);
        drvFollower_L.follow(drvLead_L);
        drvFollower_L.setControlMode(ControlMode.FollowTheLeader);
        // drvFollower_R.configFactoryDefault();
        drvFollower_R.setInverted(false);
        drvFollower_R.setBrakeCoastMode(BrakeCoastMode.Brake);
        drvFollower_R.follow(drvLead_R);
        drvFollower_R.setControlMode(ControlMode.FollowTheLeader);

        // drvLead_L.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
        // drvLead_R.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
    }

    /**
     * Initialize other motors besides the drive motors.
     */
    private static void motorsInit() {
        snorfFeed_Mtr.setInverted(true);
        snorfElv_Mtrs.setInverted(true);
    }

    /**
     * Initialize climber motors for rotation.
     * --- Due to issues with the CAN buss ---
     * <p>This is called from here & Climber case 1, to reinitialize again. ---
     */
    public static void climberMtrsInit() {
        climbMotor.restoreFactoryDefaults();
        climbMotor.setInverted(false);
        climbMotor.setIdleMode(IdleMode.kBrake);

        climbMotorFollow.restoreFactoryDefaults();
        climbMotorFollow.setInverted(false);
        climbMotorFollow.setIdleMode(IdleMode.kBrake);
        climbMotorFollow.follow(climbMotor);     //Disabled for testing
    }

    public static void sdbInit() {
        SmartDashboard.putNumber("Robot/18. Enc drvLeadTPF_L", drvLeadTPF_L);
        SmartDashboard.putNumber("Robot/19. Enc drvLeadTPF_R", drvLeadTPF_R);
        SmartDashboard.putNumber("Robot/20. Enc drvFollowerTPF_L", drvFollowerTPF_L);
        SmartDashboard.putNumber("Robot/21. Enc drvFollowerTPF_R", drvFollowerTPF_R);
        SmartDashboard.putBoolean("Robot/22. Reset Encoders", false);
    }

    public static void sdbUpdate() {
        // SmartDashboard.putNumber("Robot/1. Feet", coorXY.drvFeet());                //CAN from here
        // SmartDashboard.putNumber("Robot/2. CoorX", IO.coorXY.getX());
        // SmartDashboard.putNumber("Robot/3. CoorY", IO.coorXY.getY());
        // SmartDashboard.putNumber("Robot/4. CoorX_OS", IO.coorXY.getX_OS());
        // SmartDashboard.putNumber("Robot/5. CoorY_OS", IO.coorXY.getY_OS());
        // SmartDashboard.putNumber("Robot/6. Ld Enc Ticks L", drvLdEnc_L.ticks());
        // SmartDashboard.putNumber("Robot/7. Ld Enc Ticks R", drvLdEnc_R.ticks());
        // SmartDashboard.putNumber("Robot/8. Fl Enc Ticks L", drvFlEnc_L.ticks());
        // SmartDashboard.putNumber("Robot/9. FL Enc Ticks R", drvFlEnc_R.ticks());
        // SmartDashboard.putNumber("Robot/10. Ld Mtr15 Cmd L", drvLead_R.get());
        // SmartDashboard.putNumber("Robot/11. Ld Mtr11 Cmd R", drvLead_L.get());
        // SmartDashboard.putNumber("Robot/12. Fl Mtr16 Cmd L", drvFollower_L.get());
        // SmartDashboard.putNumber("Robot/13. Fl Mtr12 Cmd R", drvFollower_R.get());
        SmartDashboard.putNumber("Robot/14. Ld Enc Feet L", drvLdEnc_L.feet());
        SmartDashboard.putNumber("Robot/15. Ld Enc Feet R", drvLdEnc_R.feet());
        SmartDashboard.putNumber("Robot/16. Fl Enc Feet L", drvFlEnc_L.feet());
        SmartDashboard.putNumber("Robot/17. FL Enc Feet R", drvFlEnc_R.feet());     //CAN to here
        updTPF();
        
        SmartDashboard.putNumber("Robot/22. Heading", navX.getAngle());
        SmartDashboard.putNumber("Robot/23. Hdg 180", navX.getNormalizedTo180());

        SmartDashboard.putNumber("Climber/leadMtrEnc", climbLdMtr_Enc.ticks());     //CAN
        SmartDashboard.putBoolean("Climber/brakeState_SV", climbBrakeRel_SV.get()); //CAN

    }

    private static void updTPF(){
        double tmp = SmartDashboard.getNumber("Robot/18. Enc drvLeadTPF_L", drvLeadTPF_L);
        if(drvLeadTPF_L != tmp){
            drvLeadTPF_L = tmp;
            drvLdEnc_L.setTPF(tmp);
        }

        tmp = SmartDashboard.getNumber("Robot/19. Enc drvLeadTPF_R", drvLeadTPF_R);
        if(drvLeadTPF_R != tmp){
            drvLeadTPF_R = tmp;
            drvLdEnc_R.setTPF(tmp);
        }

        tmp = SmartDashboard.getNumber("Robot/20. Enc drvFollowerTPF_L", drvFollowerTPF_L);
        if(drvFollowerTPF_L != tmp){
            drvFollowerTPF_L = tmp;
            drvFlEnc_L.setTPF(tmp);
        }

        tmp = SmartDashboard.getNumber("Robot/21. Enc drvFollowerTPF_R", drvFollowerTPF_R);
        if(drvFollowerTPF_R != tmp){
            drvFollowerTPF_R = tmp;
            drvFlEnc_R.setTPF(tmp);
        }
        resetEnc = SmartDashboard.getBoolean("Robot/22. Reset Encoders", false);
        if (resetEnc) {
            drvFlEnc_R.reset();
            drvFlEnc_L.reset();
            drvLdEnc_R.reset();
            drvLdEnc_L.reset();
            SmartDashboard.putBoolean("Robot/22. Reset Encoders", false);
        }
    }
}
