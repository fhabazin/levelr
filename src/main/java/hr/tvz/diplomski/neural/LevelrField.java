package hr.tvz.diplomski.neural;

import org.encog.EncogError;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.ResetStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.util.simple.EncogUtility;

import java.io.*;
import java.util.*;

/**
 * Created by Filip on 17.5.2015..
 */
public class LevelrField implements Serializable {


    private static BasicMLDataSet training;
    private static final Map<Integer, Integer> identity2neuron = new HashMap<>();
    private static final Map<Integer, Integer> neuron2identity = new HashMap<>();
    private static int outputCount;
    private static  String line;
    private static final  Map<String, String> args = new HashMap<>();
    private static BasicNetwork network;
    private static List<MovingPair> moveList;
    private static List<MovingPair> testMoveList;
    private static String FILENAME = "levelr.net";
    private static String N2I = "N2I.net";
    private static String I2N = "I2N.net";
    private static Random rnd;
    private static double posibility = 0.2;

    private static double[][] testIn = {{1.0,0.990909090909091,0.9766233766233766,0.977922077922078,0.964935064935065,0.9675324675324676,0.9766233766233766,0.9961038961038962,1.0,0.980719794344473,0.9640102827763496,0.9537275064267352,0.9524421593830334,0.9485861182519281,0.9473007712082262,0.9820051413881749,1.0,0.9794079794079794,0.9626769626769627,0.9536679536679536,0.9305019305019305,0.9279279279279279,0.9330759330759331,0.963963963963964,1.0,0.9922279792746114,0.9702072538860104,0.9650259067357513,0.944300518134715,0.9300518134715026,0.9378238341968912,0.9559585492227979,1.0,0.9884020618556701,0.961340206185567,0.9600515463917526,0.9226804123711341,0.9097938144329897,0.8969072164948454,0.9239690721649485,1.0,0.9908616187989556,0.9725848563968669,0.9543080939947781,0.9295039164490861,0.9151436031331592,0.8955613577023499,0.902088772845953,1.0,0.9741602067183462,0.962532299741602,0.9483204134366925,0.9121447028423773,0.8850129198966409,0.8630490956072352,0.8578811369509044,1.0,0.9792207792207792,0.9467532467532468,0.9337662337662338,0.912987012987013,0.8636363636363636,0.8363636363636363,0.8337662337662337,1.0,0.0,1.0,1.0},
                                        {0.9946879150066401,0.9628154050464808,0.9614873837981408,0.9641434262948207,0.9694555112881806,0.9601593625498008,0.9601593625498008,1.0,1.0,0.9762845849802372,0.9604743083003953,0.9578392621870883,0.9525691699604744,0.9499341238471674,0.9512516469038208,0.9670619235836627,1.0,0.9828722002635046,0.9749670619235836,0.9591567852437418,0.9499341238471674,0.9341238471673254,0.9367588932806324,0.9538866930171278,1.0,0.9750328515111695,0.9750328515111695,0.9658344283837057,0.9408672798948752,0.9303547963206308,0.9198423127463863,0.9461235216819974,1.0,0.9921052631578947,0.9868421052631579,0.9723684210526315,0.9513157894736842,0.9263157894736842,0.9184210526315789,0.9328947368421052,1.0,0.9802890932982917,0.9710906701708278,0.9645203679369251,0.938239159001314,0.9250985545335085,0.900131406044678,0.8948751642575559,1.0,0.9804941482444733,0.9544863459037711,0.9505851755526658,0.9245773732119635,0.8972691807542262,0.8686605981794538,0.8595578673602081,1.0,0.9628681177976952,0.9475032010243278,0.9295774647887324,0.8988476312419974,0.8732394366197183,0.8361075544174136,0.8194622279129321,1.0,0.0,1.0,1.0}};
    private static double testIdeal[][] = {{0},{0}};
    public BasicNetwork getNetwork(){
        return this.network;
    }

    public void setNetwork(BasicNetwork network){
        this.network = network;
    }
    public LevelrField(BasicNetwork net){
        network = net;
    }
    public LevelrField(String file) throws IOException {

        execute(file);
    }



   /* public LevelrNetwork( ) {
        try {
            network = (BasicNetwork) SerializeObject.load(new File(FILENAME));
            MLDataSet trainingSet = new BasicMLDataSet(testIn, testIdeal);
            double e = network.getLayerCount();// network.calculateError(trainingSet);
            System.out
                    .println("Loaded network's error is(should be same as above): "
                            + e);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }*/

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
        testMoveList = new ArrayList<>();
        rnd = new Random();
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
        double[] inputData = new double[68];


        int inputIterator = 0;
        MovingPair pair;
        while (tok.hasMoreTokens()) {
            inputData[inputIterator++] = Double.parseDouble(tok.nextToken());

        }




        pair = new MovingPair(inputData,direction);
        if (rnd.nextDouble() < posibility)
            testMoveList.add(pair);
        else
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
        network = EncogUtility.simpleFeedForward(training.getInputSize(), hidden1, hidden2, training.getIdealSize(), true);


        System.out.println("Created network: " + network.toString());
    }

    private static  void train() throws IOException {
        System.out.println("Training Beginning... Output patterns="
                + outputCount);

        final double strategyError = 0.3;
        final int strategyCycles = 10;

        final MLTrain train = new ScaledConjugateGradient(network, training);
       // train.addStrategy(new Greedy());
        train.addStrategy(new ResetStrategy(strategyError, strategyCycles));
       // train.addStrategy(new RegularizationStrategy(1.5));
        System.out.println(network.getInputCount());
        do{
            train.iteration();

            System.out.println(train.getError()*100);
        }while(train.getError()>0.000000000000000000001);

        test();


        System.out.println("Training Stopped...");
        //SerializeObject.save(new File(FILENAME), network);

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

    public static  void test() {
        int successCount = 0;

        for (MovingPair pair : moveList) {
            BasicMLData input = new BasicMLData(pair.getInputs());
            System.out.println("original: "+pair.getDirection()+" net: "+neuron2identity.get(network.winner(input)));

            if (neuron2identity.get(network.winner(input)) == pair.getDirection()) {
                successCount++;
            }
        }
        System.out.println(successCount / testMoveList.size());
    }

}