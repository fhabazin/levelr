package hr.tvz.diplomski.neural;

import org.encog.EncogError;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.strategy.ResetStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.util.simple.EncogUtility;

import java.io.*;
import java.util.*;

/**
 * Created by Filip on 10.8.2016..
 */
public class LevelrMicrophones {
    private static BasicMLDataSet training;
    private static final Map<Integer, Integer> identity2neuron = new HashMap<>();
    private static final Map<Integer, Integer> neuron2identity = new HashMap<>();
    private static int outputCount;
    private static  String line;
    private static final  Map<String, String> args = new HashMap<>();
    private static BasicNetwork network;
    private static List<MovingPair> moveList;

    public BasicNetwork getNetwork(){
        return this.network;
    }

    public void setNetwork(BasicNetwork network){
        this.network = network;
    }


    public LevelrMicrophones(BasicNetwork net){
        network = net;
    }
    public LevelrMicrophones(String file) throws IOException {

        execute(file);
    }

    public LevelrMicrophones( ) {
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


    public static void execute(final String file) throws IOException {
        final FileInputStream fstream = new FileInputStream(file);
        final DataInputStream in = new DataInputStream(fstream);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        moveList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            executeLine();
        }
        fstream.close();
    }

    private static  void executeCommand(final String command,
                                        final Map<String, String> args) throws IOException {
        if (command.equals("input")) {
            input();
        } else if (command.equals("createtraining")) {
            createTraining();
        }else if(command.equals("network")){
            buildNetwork();
            train();
        }
    }



    public static void executeLine() throws IOException {
        final int index = line.indexOf(':');
        if (index == -1) {
            throw new EncogError("Invalid command: " + line);
        }

        final String command = line.substring(0, index).toLowerCase()
                .trim();
        final String argsStr = line.substring(index + 1).trim();
        final StringTokenizer tok = new StringTokenizer(argsStr, ",");
        args.clear();
        while (tok.hasMoreTokens()) {
            final String arg = tok.nextToken();
            final int index2 = arg.indexOf(':');
            if (index2 == -1) {
                throw new EncogError("Invalid command: " + line);
            }
            final String key = arg.substring(0, index2).toLowerCase().trim();
            final String value = arg.substring(index2 + 1).trim();
            args.put(key, value);
        }

        executeCommand(command, args);
    }

    private static  String getArg(final String name) {
        final String result = args.get(name);
        if (result == null) {
            throw new EncogError("Missing argument " + name + " on line: "
                    + line);
        }
        return result;
    }

    private static  void input() throws IOException {
        final String data = getArg("data");
        final String identity = getArg("identity");
        final int direction = Integer.parseInt(identity);
        final int idx = assignIdentity(direction);
        System.out.println("data:"+data+" identity:" + identity);
        final StringTokenizer tok = new StringTokenizer(data, ";");
        double[] inputData = new double[8];
        int inputIterator = 0;
        MovingPair pair;
        while (tok.hasMoreTokens()) {
            inputData[inputIterator++] = Double.parseDouble(tok.nextToken());
            System.out.println(inputData[inputIterator-1]);
        }
        pair = new MovingPair(inputData,direction);
        moveList.add(pair);

    }

    private static  void createTraining() {
        training = new BasicMLDataSet();
        System.out.println("Training set created");
    }

    private static  void buildNetwork() throws IOException {


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

        final String strHidden1 = getArg("hidden1");
        final String strHidden2 = getArg("hidden2");
        final int hidden1 = Integer.parseInt(strHidden1);
        final int hidden2 = Integer.parseInt(strHidden2);
        network = EncogUtility.simpleFeedForward(training.getInputSize(), hidden1, hidden2,
                training.getIdealSize(), true);
        System.out.println("Created network: " + network.toString());
    }

    private static  void train() throws IOException {
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
        //fireDoneTraining(new NetEvent(network));


        System.out.println("Training Stopped...");
    }

    public int whereTo(double[] inputs){
        BasicMLData input = new BasicMLData(inputs);

        return  neuron2identity.get(network.winner(input));

    }

    public static Map<Integer, Integer> getIdentity2neuron() {
        return identity2neuron;
    }

    public Map<Integer, Integer> getNeuron2identity() {
        return neuron2identity;
    }
}
