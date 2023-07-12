package com.example.database_test.audioclassify;

import com.jlibrosa.audio.process.AudioFeatureExtraction;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public final class Preprocessing {

    /**
     *
     */
    private static final AudioFeatureExtraction featureExtraction = new AudioFeatureExtraction();


    private Preprocessing() { /* Util-Class */ }


    /**
     *
     */
    public static double[][] process(float[] input, int sr, int num_fft, int hop_length, int num_freq_bin, double fmin, double fmax) {

        featureExtraction.setSampleRate(sr);
        featureExtraction.setN_fft(num_fft);
        featureExtraction.setHop_length(hop_length);
        featureExtraction.setN_mels(num_freq_bin);
        featureExtraction.setfMin(fmin);
        featureExtraction.setfMax(fmax);

        // librosa.feature.melspectrogram
        double[][] melspec_features = melSpectogram(input);

        // np.log(features)
        double[][] log_features = ln(melspec_features);

        //feat_data - np.min(feat_data)) / (np.max(feat_data) - np.min(feat_data))
        return normalize(log_features);
    }


    /**
     * @param meanBuffer
     * @return
     */
    private static double[][] melSpectogram(float[] meanBuffer) {

        double[][] melBasis = melFilter();
        double[][] spectro = featureExtraction.extractSTFTFeatures(meanBuffer);
        double[][] melS = new double[melBasis.length][spectro[0].length];

        for (int i = 0; i < melBasis.length; ++i) {
            for (int j = 0; j < spectro[0].length; ++j) {
                for (int k = 0; k < melBasis[0].length; ++k) {
                    melS[i][j] += melBasis[i][k] * spectro[k][j];
                }
            }
        }
        return melS;

    }


    /**
     * @return
     */
    private static double[][] ln(double[][] data) {

        return (Arrays.stream(data).map(
                ls -> (DoubleStream.of(ls).map(e -> Math.log(e + 0.0000001))).toArray()
        )).toArray(double[][]::new);
    }


    /**
     * @param data
     * @return
     */
    private static double[][] normalize(double[][] data) {

        double min = Arrays.stream(data).flatMapToDouble(Arrays::stream).min().getAsDouble();
        double max = Arrays.stream(data).flatMapToDouble(Arrays::stream).max().getAsDouble();

        return (double[][]) Arrays.stream(data).map(ls -> Arrays.stream(ls).map(e -> (e - min) / (max - min)).toArray()).toArray(double[][]::new);
    }


    /****************************/
    /**  Re-Implement Librosa  **/
    /****************************/


    private static double[][] melFilter() {
        double[] fftFreqs = fftFreq();
        double[] melF = melFreq(featureExtraction.getN_mels() + 2);
        double[] fdiff = new double[melF.length - 1];

        for (int i = 0; i < melF.length - 1; ++i) {
            fdiff[i] = melF[i + 1] - melF[i];
        }

        double[][] ramps = new double[melF.length][fftFreqs.length];

        for (int i = 0; i < melF.length; ++i) {
            for (int j = 0; j < fftFreqs.length; ++j) {
                ramps[i][j] = melF[i] - fftFreqs[j];
            }
        }

        double[][] weights = new double[
                featureExtraction.getN_mels()][1 + (int) (featureExtraction.getN_fft() / 2)];

        for (int i = 0; i < featureExtraction.getN_mels(); ++i) {
            for (int j = 0; j < fftFreqs.length; ++j) {
                double lowerF = -ramps[i][j] / fdiff[i];
                double upperF = ramps[i + 2][j] / fdiff[i + 1];
                if (lowerF > upperF && upperF > 0.0D) {
                    weights[i][j] = upperF;
                } else if (lowerF > upperF && upperF < 0.0D) {
                    weights[i][j] = 0.0D;
                } else if (lowerF < upperF && lowerF > 0.0D) {
                    weights[i][j] = lowerF;
                } else if (lowerF < upperF && lowerF < 0.0D) {
                    weights[i][j] = 0.0D;
                }
            }
        }

        // DO NOT NORMALIZE
        //double[] enorm = new double[featureExtraction.getN_mels()];

        //for(j = 0; j < featureExtraction.getN_mels(); ++j) {
        //    enorm[j] = 2.0D / (melF[j + 2] - melF[j]);

        //    for(int j = 0; j < fftFreqs.length; ++j) {
        //        weights[j][j] *= enorm[j];
        //    }
        //}

        return weights;
    }


    private static double[] fftFreq() {
        double[] freqs = new double[1 + featureExtraction.getN_fft() / 2];

        for (int i = 0; i < 1 + featureExtraction.getN_fft() / 2; ++i) {
            freqs[i] = 0.0D + featureExtraction.getSampleRate() / 2.0D / (double) (featureExtraction.getN_fft() / 2) * (double) i;
        }

        return freqs;
    }


    private static double[] melFreq(int numMels) {
        double[] LowFFreq = new double[1];
        double[] HighFFreq = new double[1];
        LowFFreq[0] = featureExtraction.getfMin();
        HighFFreq[0] = featureExtraction.getfMax();
        double[] melFLow = freqToMel(LowFFreq);
        double[] melFHigh = freqToMel(HighFFreq);
        double[] mels = new double[numMels];

        for (int i = 0; i < numMels; ++i) {
            mels[i] = melFLow[0] + (melFHigh[0] - melFLow[0]) / (double) (numMels - 1) * (double) i;
        }

        return melToFreq(mels);
    }


    private static double[] freqToMel(double[] freqs) {

        // Python: 2595.0 * np.log10(1.0 + frequencies / 700.0)

        double[] mels = new double[freqs.length];

        for (int i = 0; i < mels.length; i++) {
            mels[i] = 2595.0 * Math.log10(1.0 + freqs[i] / 700.0);
        }

        return mels;
    }


    private static double[] melToFreq(double[] mels) {

        // Python: return 700.0 * (10.0 ** (mels / 2595.0) - 1.0)

        double[] freqs = new double[mels.length];

        for (int i = 0; i < mels.length; ++i) {
            freqs[i] = 700.0D * (Math.pow(10.0D, mels[i] / 2595.0D) - 1.0D);
        }

        return freqs;
    }
}
