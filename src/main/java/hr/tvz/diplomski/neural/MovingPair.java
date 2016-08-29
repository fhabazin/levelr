package hr.tvz.diplomski.neural;

/**
 * Created by Filip on 17.5.2015..
 */
public class MovingPair {

   private double[] inputs;
    private int direction;

    public MovingPair(double[] inputs, int direction){
        this.inputs = inputs;
        this.direction = direction;
    }

    public double[] getInputs(){
        return inputs;
    }

    public int getDirection(){
        return direction;
    }
}
