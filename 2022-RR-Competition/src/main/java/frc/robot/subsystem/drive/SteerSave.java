package frc.robot.subsystem.drive;
/*
Author: Team 86
History: 
jch - 2/2020 - Original Release
jch - 2/2021 - Mod to move hdg normalization to PropMath(..., k180)0
TODO2: - Need to test
Desc.
Returns arcade drive commands in an array[X(hdg), Y(dist)]
to move bot along hdg at % speed for distance 
Both Hdg & dist Must be initialized with a SP, PB, DB, Mn, Mx, Xcl
A linear responce is calculated for each between SP & PB to Mn & Mx, respectfully.
Returns 0.0 for respective item when within DB.
Also limits acceleration, increase in out, to respective Xcl value.
*/

import edu.wpi.first.math.controller.PIDController;
import frc.util.PropMath;

public class SteerSave {
    private double drvCmds[] = { 0.0, 0.0 }; // Cmds X, Y - Rot, FwdBkwd
    private int status = 0; // 0-Running, 1=On Hdg, 2=at Dist, 3=both in DB
    private double pwrScalar = 100.0; // %Scale output, apply to hdg & distance
    //Heading variables
    private PropMath hdgProp;           // Instance of PropMath to calculate prop response for hdg
    private double hdgOut = 0.0;        //Calc X(rot) output
    private double prvHdgOut = 0.0;     //Previous hdgOut
    private double hdgXclLmt = 1.0;     //Limit change of hdgOut to this
    //Distance variables
    private PropMath distProp;          // Instance of PropMath to calculate prop response for dist
    private double distOut = 0.0;       //Calc Y(dist) output
    private double prvDistOut = 0.0;    //Previous distOut
    private double distXclLmt = 0.07;   //Limit change of distOut to this

    private PIDController hdgPID;
    // private PIDController distPID;


    /**
     * Constructor using a 2d array.  Creates a proportional object for hdg & dist.
     * >p>[0=hdg | 1=dist][0=SP, 1=PB, 2=DB, 3=Mn, 4=Mx, 5=Xcl]
     * 
     * @param propParm - a 2d array of doubles[2][5]
     */
    public SteerSave(double[][] propParm) {
        hdgProp = new PropMath(propParm[0], true);      //Initialize with parms array, SP, PB, DB, Mn, Mx & k180
        hdgXclLmt = propParm[0][5];                     //and Xcl
        distProp = new PropMath(propParm[1], false);    //Initialize with parms array, SP, PB, DB, Mn, Mx & k180
        distXclLmt = propParm[1][5];                    //and Xcl
    }

    /**
     * Constructor using defaults.  Creates a proportional object for hdg & dist:
     * <p>hdg => SP=0.0, PB=-150.0, DB=3.0, Mn=0.1, Mx=1.0, Xcl=1.0, k180=true
     * <p>dist => SP=0.0, PB=5.0, DB=0.5, Mn=0.1, Mx=1.0, Xcl=1.0, k180=false
     */
    public SteerSave() {
        hdgProp = new PropMath(0.0, -70.0, 5.0, 0.3, 1.0, true);    //Initalize with defaults
        hdgPID = new PIDController(-70.0, 0.0, 0.0);                //Testing WPI PID
        hdgPID.enableContinuousInput(-180.0, 180.0);                //Testing continuous -180 to 180 degrees
        hdgXclLmt = 1.0;                                            //and Xcl

        distProp = new PropMath(0.0, 5.0, 0.5, 0.5, 1.0, false);    //Initialize with defaults
        // distPID = new PIDController(5.0, 0.0, 0.0);                 //Testing WPI PID
        distXclLmt = 0.07;                                          //and Xcl
    }

    /**
     * Start steering, set hdg, pwr & dist.
     * <p>Call update after setting setpoints.
     * 
     * @param _hdgSP - heading setpoint
     * @param _distSP - distance setpoint in feet
     * @param _pwrSc - power pct to use in rotation and distance as double
     */
    public void steerTo(double _hdgSP, double _distSP, double _pwrSc) {
        status = 0;         // Status, 0=No, 1=Hdg in DB, 2=Dist in DB, 3=Both in DB
        setHdgSP(_hdgSP);
        setDistSP(_distSP);
        pwrScalar = _pwrSc; // Scalar
    }

    /**
     * Start steering, set hdg, pwr & dist.
     * <p>Call update after setting setpoints.
     * 
     * @param _hdgSP - heading setpoint
     * @param _distSP - distance setpoint in feet
     * @param _pwrSc - power pct to use in rotation and distance as interger
     */
    public void steerTo(double _hdgSP, double _distSP, int _pwrSc) {
        steerTo(_hdgSP, _pwrSc / 100.0, _distSP);
    }

    /**
     * Start steering, set hdg, dist & pwr using double[3] array.
     * <p>Call update after setting setpoints.
     * 
     * @param traj - double[3] = { hdgSP, ftSP, pwr }
     */
    public void steerTo(double[] traj) {
        steerTo(traj[0], traj[1], traj[2]);
        System.out.println("----------SPs: " + traj[0] + "  " + traj[1] + "  " + traj[2]);
    }

    /**
     * Calculate arcade joystick cmds, X (rotation) & Y (fwd/bkwd move)
     * <p>Called after steerTo sets setpoints, hdgSP & distSP
     * 
     * @param _hdgFB heading feedback, gyro
     * @param _distFB distance traveled, encoders
     * @return double[] drvCmds = { X(rot), Y(fwd/bkwd) }
     */
    public double[] update(double _hdgFB, double _distFB) {
        drvCmds[0] = calcX(_hdgFB);       // Rotation
        prvHdgOut = drvCmds[0];     //Save for acceleration limiter
        drvCmds[1] = calcY(_distFB);       // Fwd/Bkwd
        prvDistOut = drvCmds[1];    //Save for acceleration limiter
        return drvCmds;
    }

    /**
     * Calculate arcade joystick cmds, X (rotation) & Y (fwd/bkwd move)
     * <p>Called after steerTo sets setpoints, hdgSP & distSP
     * 
     * @param _hdgFB heading feedback, gyro
     * @param _distFB distance traveled, encoders
     * @return double[] drvCmds = { X(rot), Y(fwd/bkwd) }
     */
    public double[] update() {
        return update(Drive.hdgFB(), Drive.distFB());
    }

    /**
     * @return  0=Not complete, 1=hdg in DB, 2=dist in DB, 3= Both in DB
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return  true if both rotation and distance are in thier respective deadbands.
     */
    public boolean isDone() {
        return (status & 3) == 3;
    }

    /**
     * @return  true if heading is within deadband
     */
    public boolean isHdgDone() {
        return (status & 1) > 0;
    }

    /**
     * @return  true if dist is in DB
     */
    public boolean isDistDone() {
        return (status & 2) > 0;
    }

    /**
     * Calculates the arcade JS X(rot) value.  Calcs err, Normalizes to +/-180 then calcs a proportional response
     * using the heading propband, hdgPB.
     * <p>Sets status bit 1 if within hdgDB, return out is 0.0 else bit is reset.
     * <p>If output is not 0.0, limit output change (acceleration). 
     * 
     * @param hdgFB heading feedback, gyro
     * @return  calc'ed arcade JS X(rot) value.
     */
    private double calcX(double hdgFB) {
        setHdgMx(Math.max(getHdgMn(), Math.min(1.0, pwrScalar)));   //Limit Mx btwn Mn & 1.0
        hdgOut = hdgProp.calcProp(hdgFB, false);

        if (hdgOut == 0.0) {
            status |= 1; // If in DB, set bit,
        } else {
            status &= ~1; // else clear
            hdgOut = accelLimiter(hdgOut, prvHdgOut, hdgXclLmt, getHdgMn());    //Accel limiter
        }
        return hdgOut;
    }

    /**
     * Calculates the arcade JS Y(dist) value.  Passes feet FB to calc a proportional response
     * using the distance propband, distPB.
     * <p>Sets status bit 2 if within distDB, return out is 0.0 else bit is reset.
     * <p>If output is not 0.0, apply scaler & limit output change (acceleration). 
     * 
     * @param distFB distance feedback, encoders
     * @return  Calc'ed arcade JS Y(dist) value
     */
    private double calcY(double distFB) {
        setDistMx(Math.max(getDistMn(), Math.min(1.0, pwrScalar)));   //Limit Mx btwn Mn & 1.0
        distOut = distProp.calcProp(distFB, false);
        if (distOut == 0.0) {
            status |= 2; // If in DB, set bit else clr
        } else {
            status &= ~2;
            distOut = accelLimiter(distOut, prvDistOut, distXclLmt, getDistMn());   //Accel limiter
        }
        return distOut;
    }

    /**
     * Limits the change in output to prevent slipage.
     *  
     * @param psntVal  Present value
     * @param prvVal  Previous value
     * @param xclLmt  Maximum change in present value from previous value
     * @param outMn  If present value not 0.0 must be at least outMn.
     * @return  Limited present value
     */
    private static double accelLimiter(double psntVal, double prvVal, double xclLmt, double outMn) {
        if (Math.abs(psntVal - prvVal) > xclLmt && psntVal != 0.0) {
            psntVal = psntVal < 0.0 ? Math.min(prvVal - xclLmt, -outMn) :   // Value neg.
            /*                      */Math.max(prvVal + xclLmt, outMn);     // else pos.
        }
        return psntVal;
    }

    public void setHdgSP(double hdgSP){ hdgProp.setSP(hdgSP); }
    public void setHdgPB(double hdgPB){ hdgProp.setPB(hdgPB); }
    public void setHdgDB(double hdgDB){ hdgProp.setDB(hdgDB); }
    public void setHdgMn(double hdgMn){ hdgProp.setOutMn(hdgMn); }
    public void setHdgMx(double hdgMx){ hdgProp.setOutMx(hdgMx); }
    public void setHdgXcl(double hdgXcl){ hdgXclLmt = hdgXcl; }

    public double getHdgSP(){ return hdgProp.getSP(); }
    public double getHdgPB(){ return hdgProp.getPB(); }
    public double getHdgDB(){ return hdgProp.getDB(); }
    public double getHdgMn(){ return hdgProp.getOutMn(); }
    public double getHdgMx(){ return hdgProp.getOutMx(); }
    public double getHdgXcl(){ return hdgXclLmt; }
    public boolean getHdgk180(){ return hdgProp.get180(); }

    public void setDistSP(double distSP){ distProp.setSP(distSP); }
    public void setDistPB(double distPB){ distProp.setPB(distPB); }
    public void setDistDB(double distDB){ distProp.setDB(distDB); }
    public void setDistMn(double distMn){ distProp.setOutMn(distMn); }
    public void setDistMx(double distMx){ distProp.setOutMx(distMx); }
    public void setDistXcl(double distXcl){ distXclLmt = distXcl; }

    public double getDistSP(){ return distProp.getSP(); }
    public double getDistPB(){ return distProp.getPB(); }
    public double getDistDB(){ return distProp.getDB(); }
    public double getDistMn(){ return distProp.getOutMn(); }
    public double getDistMx(){ return distProp.getOutMx(); }
    public double getDistXcl(){ return distXclLmt; }

}