/*
Desc. The timer is set, started, when the a change of variable, cov, occurs
for an int or boolean.  The same call is made to set the time and check it.
History -
1/1/20 - Anthony released
3/12/20 - JCH added simple startTimer and renamed vars.
3/15/21 - JCH added comments and chgd to timer v. delay.  Cascade to startTimer().
2/17/22 - Po added clearTimer(); & chgd trgrs to objs, to init them. Overall clean up.
*/

package frc.util;

public class Timer {
    private long time;            //Time in milli Seconds.  Passed as Seconds.
    private long timer;           //Current time + delay
    private Integer covTrgr = null;       //Integer cov trigger
    private Boolean trgr = null;   //Boolean trigger

    //Constructor
    /**
     * Create an on delay timer.  Can be triggered with an integer or boolean
     * <p>If trigger is different than last call, timer is set to current time + delay
     * @param delay default time in seconds.
     */
    public Timer(double delay){
        this.time = (long) (delay * 1000);
        startTimer();               //?? Needed in constructor???
    }

    /**
     * If chg of var, cov, set delay time once, then continue to call for expired time.
     * @param delay in seconds
     * @param covTrgr set timer if trigger is different than the last check
     * @return  Timer has expired
     */
    public boolean hasExpired(double delay, int covTrgr){
        if(this.covTrgr == null || this.covTrgr != covTrgr){ // new state found
            this.time = (long) (delay * 1000);
            timer = System.currentTimeMillis() + this.time; // new time to wait for
            this.covTrgr = covTrgr; // save new state
        }
       
        return hasExpired();    // tell if wanted time has been reached
    }

    /**
     * If chg of var, cov, set delay time once, then continue to call for expired time.
     * @param delay in seconds
     * @param trgr set timer if trigger is different than the last check
     * @return  Timer has expired
     */
    public boolean hasExpired(double delay, boolean trgr){
        
        if(this.trgr == null || this.trgr != trgr){
            this.time = (long)(delay * 1000);
            startTimer();
            this.trgr = trgr;
        }
        
        return hasExpired();
    }

    /**Current time is <= timer */
    public boolean hasExpired(){
        return System.currentTimeMillis() > timer; // Are we at wanted time yet?
    }

    /**
     * Reset & start the timer.  Uses & sets a new delay.
     * @param sec delay in seconds
     */
    public void startTimer(double sec){
        time = (long) (sec * 1000);
        startTimer();
    }

    /**
     * Reset & start the timer.  Uses & sets a new delay.
     * @param sec delay in seconds
     */
    public void startTimer(int mSec){
        time = mSec;
        startTimer();
    }

    /**Reset & start the timer using existing delay */
    public void startTimer(){
        timer = System.currentTimeMillis() + time;
    }
    
    /**Set the triggers to null */
    public void clearTimer(){
        covTrgr = null;
        trgr = null;
    }

}
