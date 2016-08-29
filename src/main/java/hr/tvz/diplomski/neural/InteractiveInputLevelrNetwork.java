package hr.tvz.diplomski.neural;

import java.io.Serializable;

/**
 * Created by Filip on 23.6.2016..
 */
public class InteractiveInputLevelrNetwork implements Serializable {
   /* private static List<MovingPair> moveList;
    private static BasicMLDataSet training;
    private static final Map<Integer, Integer> identity2neuron = new HashMap<>();
    private static final Map<Integer, Integer> neuron2identity = new HashMap<>();
    private static int outputCount;
    private static BasicNetwork network;
    final int firstHiddenLayer = 100;
    final int secondHiddenLayer = 50;

    public InteractiveInputLevelrNetwork(){
        moveList = new ArrayList<>();
    }

    public void addInput(short[][] inputWave, double[] walls, int direction) {
        MovingPair pair;
        int sizeOfArray = inputWave.length * inputWave[0].length;
        double [] inputData = new double[sizeOfArray + 4];
        final int idx = assignIdentity(direction);
        int i = 0;
        for(int y = 0; y < inputWave.length; y++) {
            for (int x = 0; x < inputWave[0].length; x++) {
                inputData[i++] = inputWave[y][x];
            }
        }
        for(i = 0; i < walls.length; i++){
            inputData[sizeOfArray + i] = walls[i];
        }
        pair = new MovingPair(inputData,direction);
        moveList.add(pair);
        System.out.println("input added");


    }
    public void createTraining() {
        training = new BasicMLDataSet();
        System.out.println("Training set created");
    }

    public  void train()  {
        System.out.println("Training Beginning... Output patterns="
                + outputCount);

        final double strategyError = 0.3;
        final int strategyCycles = 50;

        final ScaledConjugateGradient train = new ScaledConjugateGradient(network, training);
        train.addStrategy(new ResetStrategy(strategyError, strategyCycles));

        System.out.println(network.getInputCount());
        do{
            train.iteration();

            System.out.println(train.getError()*100);
        }while(train.getError()>0.000000000000000000001);



        System.out.println("Training Stopped...");
    }

    public void buildNetwork() {


        for (final MovingPair pair : moveList) {
            final MLData ideal = new BasicMLData(outputCount);
            final int idx = pair.getDirection();
            for (int i = 0; i < outputCount; i++) {
                if (i == idx) {
                    ideal.setData(i, 1);
                } else {
                    ideal.setData(i, -1);
                }
            }


            final BasicMLData data = new BasicMLData(pair.getInputs());
            training.add(data, ideal);
        }



        network = EncogUtility.simpleFeedForward(training.getInputSize(), firstHiddenLayer, secondHiddenLayer,training.getIdealSize(), true);
        System.out.println("Created network: " + network.toString());
    }
    private static int assignIdentity(final int identity) {

        if (identity2neuron.containsKey(identity)) {
            return identity2neuron.get(identity);
        }

        final int result = outputCount;
        identity2neuron.put(identity, result);
        neuron2identity.put(result, identity);
        outputCount++;
        return result;
    }

    public int whereTo(short[][] inputs){
        int sizeOfArray = inputs.length * inputs[0].length;
        double [] inputData = new double[sizeOfArray];

        int i = 0;
        for(int y = 0; y < inputs.length; y++) {
            for (int x = 0; x < inputs[0].length; x++) {
                inputData[i++] = inputs[y][x];
            }
        }
        BasicMLData input = new BasicMLData(inputData);

        return  neuron2identity.get(network.winner(input));

    }*/
}
