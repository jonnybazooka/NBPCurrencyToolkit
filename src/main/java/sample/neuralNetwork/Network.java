package sample.neuralNetwork;

import java.util.Arrays;

public class Network {

    private double[][] output;
    private double[][][] weights;
    private double[][] bias;

    private double[][] error_signal;
    private double[][] output_derivative;

    public final int[] NETWORK_LAYER_SIZES;
    public final int INPUT_SIZE;
    public final int OUTPUT_SIZE;
    public final int NETWORK_SIZE;

    public Network(int... NETWORK_LAYER_SIZES) {
        this.NETWORK_LAYER_SIZES = NETWORK_LAYER_SIZES;
        this.INPUT_SIZE = NETWORK_LAYER_SIZES[0];
        this.NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
        this.OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE-1];

        this.output = new double[NETWORK_SIZE][];
        this.weights = new double[NETWORK_SIZE][][];
        this.bias = new double[NETWORK_SIZE][];
        this.error_signal = new double[NETWORK_SIZE][];
        this.output_derivative = new double[NETWORK_SIZE][];

        for (int i = 0; i < NETWORK_SIZE; i++) {
            this.output[i] = new double[NETWORK_LAYER_SIZES[i]];
            this.error_signal[i] = new double[NETWORK_LAYER_SIZES[i]];
            this.output_derivative[i] = new double[NETWORK_LAYER_SIZES[i]];
            this.bias[i] = NetworkTools.createRandomArray(NETWORK_LAYER_SIZES[i], -0.5, 0.6);

            if (i > 0) {
                weights[i] = NetworkTools.createRandomArray(NETWORK_LAYER_SIZES[i], NETWORK_LAYER_SIZES[i-1], -0.8, 0.8);
            }
        }
    }

    public double[] calculate(double... input) {
        if (input.length != this.INPUT_SIZE) {
            return null;
        }
        this.output[0] = input;
        for (int layer = 1; layer < NETWORK_SIZE; layer++) {
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++) {

                double sum = bias[layer][neuron];
                for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer-1]; prevNeuron++) {
                    sum += output[layer-1][prevNeuron] * weights[layer][neuron][prevNeuron];
                }
                output[layer][neuron] = sigmoid(sum);
                output_derivative[layer][neuron] = output[layer][neuron] * (1 - output[layer][neuron]);
            }
        }
        return output[NETWORK_SIZE-1];
    }

    public void train(TrainSet set, int loops, int batch_size) {
        if (set.INPUT_SIZE != INPUT_SIZE || set.OUTPUT_SIZE != OUTPUT_SIZE) {
            return;
        }
        for (int i = 0; i < loops; i++) {
            TrainSet batch = set.extractBatch(batch_size);
            for (int b = 0; b < batch_size; b++) {
                this.train(batch.getInput(b), batch.getOutput(b), 0.3);
            }
        }
    }

    public void train(double[] input, double[] target, double eta) {
        if (input.length != INPUT_SIZE || target.length != OUTPUT_SIZE) {
            return;
        }
        calculate(input);
        backPropagateError(target);
        updateWeights(eta);
    }

    public double MSE(double[] input, double[] target) {
        if (input.length != INPUT_SIZE || target.length != OUTPUT_SIZE) {
            return 0;
        }
        calculate(input);
        double v = 0;
        for (int i =0; i < target.length; i++) {
            v += (target[i] - output[NETWORK_SIZE-1][i]) * (target[i] - output[NETWORK_SIZE-1][i]);
        }
        return v / (2d * target.length);
    }

    public double MSE(TrainSet set) {
        double v = 0;
        for (int i = 0; i < set.size(); i++) {
            v += MSE(set.getInput(i), set.getOutput(i));
        }
        return v / set.size();
    }

    public void backPropagateError(double[] target) {
        for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[NETWORK_SIZE-1]; neuron++) {
            error_signal[NETWORK_SIZE-1][neuron] = (output[NETWORK_SIZE-1][neuron] - target[neuron])
                    * output_derivative[NETWORK_SIZE-1][neuron];
        }
        for (int layer = NETWORK_SIZE-2; layer > 0; layer--) {
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++) {
                double sum = 0;
                for (int nextNeuron = 0; nextNeuron < NETWORK_LAYER_SIZES[layer+1]; nextNeuron++) {
                    sum += weights[layer+1][nextNeuron][neuron] * error_signal[layer+1][nextNeuron];
                }
                this.error_signal[layer][neuron] = sum * output_derivative[layer][neuron];
            }
        }
    }

    public void updateWeights(double eta) {
        for (int layer = 1; layer < NETWORK_SIZE; layer++) {
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++) {

                double delta = -eta * error_signal[layer][neuron];
                bias[layer][neuron] += delta;

                for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer-1]; prevNeuron++) {
                    weights[layer][neuron][prevNeuron] += delta * output[layer-1][prevNeuron];
                }
            }
        }
    }

    private double sigmoid(double x) {
        return 1d / (1 + Math.exp(-x));
    }

    public static void main(String[] args) {
        Network net = new Network(4,3,2,1);
        TrainSet set = new TrainSet(4,1);
        set.addData(new double[]{1.84, 1.20, 1, 0.38}, new double[]{1});
        set.addData(new double[]{1.88, 1.13, 1, 0.22}, new double[]{0});
        set.addData(new double[]{1.78, 0.68, 0, 0.18}, new double[]{0});
        set.addData(new double[]{1.56, 0.60, 0, 0.23}, new double[]{1});
        set.addData(new double[]{1.75, 1.60, 1, 0.54}, new double[]{1});
        net.train(set, 100000, 5);
        for (int i = 0; i < 5; i++) {
            System.out.println(Arrays.toString(net.calculate(set.getInput(i))));
        }
        double[] test1 = new double[] {1.90, 0.99, 1, 0.289};
        double[] exp1 = new double[]{0.1, 0.9};
        double[] test2 = new double[] {1.60, 0.76, 0, 0.23};
        double[] exp2 = new double[]{0.9, 0.1};
        System.out.println("TEST 1: " + Arrays.toString(net.calculate(test1)));
        System.out.println("TEST 2: " + Arrays.toString(net.calculate(test2)));

        /*Network net = new Network(4,3,3,2);

        TrainSet set = new TrainSet(4,2);
        set.addData(new double[]{0.1,0.2,0.3,0.4}, new double[]{0.9,0.1});
        set.addData(new double[]{0.9,0.8,0.7,0.6}, new double[]{0.1,0.9});
        set.addData(new double[]{0.3,0.8,0.1,0.4}, new double[]{0.3,0.7});
        set.addData(new double[]{0.9,0.8,0.1,0.2}, new double[]{0.7,0.3});


        net.train(set, 100000, 4);

        for (int i = 0; i < 4; i++) {
            System.out.println(Arrays.toString(net.calculate(set.getInput(i))));
        }*/

        /*Network net = new Network(4,3,3,2);
        double[] input = new double[]{0.1,0.2,0.3,0.4};
        double[] input2 = new double[]{0.6,0.1,0.4,0.8};
        double[] target = new double[]{0.9,0.1};
        double[] target2 = new double[]{0.1,0.9};

        for (int i = 0; i < 10000; i++) {
            net.train(input, target, 0.3);
            net.train(input2, target2, 0.3);
        }
        System.out.println(Arrays.toString(net.calculate(input)));
        System.out.println(Arrays.toString(net.calculate(input2)));*/

        /*Network net = new Network(4,1,3,4);

        double[] input = new double[]{0.1,0.5,0.2,0.9};
        double[] target = new double[]{0,1,0,0};
        for (int i = 0; i < 10000; i++) {
            net.train(input, target, 0.3);
        }
        System.out.println(Arrays.toString(net.calculate(input)));*/
    }
}
