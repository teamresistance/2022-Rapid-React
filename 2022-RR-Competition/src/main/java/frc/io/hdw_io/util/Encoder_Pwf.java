package frc.io.hdw_io.util;

import com.playingwithfusion.CANVenom;

import edu.wpi.first.math.util.Units;

public class Encoder_Pwf {

    private double tpf;
    private CANVenom venomCtlr;

    /**Interface to Spark Max controller with NEO motor encoders */
    public Encoder_Pwf(CANVenom escPort, double _tpf){
        tpf = _tpf;
        venomCtlr = escPort;
    }

    /**@return Encoder ticks. */
    public double ticks(){
        return venomCtlr.getPosition();
    }

    /**@return calculated feet from ticks. */
    public double feet(){
        return tpf == 0.0 ? 0.0 : venomCtlr.getPosition() / tpf;
    }

    /**@return calcuate meters from feet. */
    public double meters() {
        return Units.feetToMeters(feet());
    }

    /** Reset encoder count to zero. */
    public void reset(){
        venomCtlr.resetPosition();
        // venomCtlr.setPosition(0.0);
    }

    /**@return the existiing ticks per foot, tpf. */
    public double getTPF() { return tpf; }

    /**@param tpf - Set ticks per foot.  */
    public void setTPF( double tpf) { this.tpf = tpf; }
}
