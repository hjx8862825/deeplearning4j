package org.deeplearning4j.nn.layers.recurrent;

import java.util.Arrays;
import java.util.Collections;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.optimize.api.IterationListener;
import static org.junit.Assert.*;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;


public class MaskZeroLayerTest {

  @Test
  public void activate() {

      //GIVEN two examples where some of the timesteps are zero.
      INDArray ex1 = Nd4j.create(new double[][] {
          new double[] { 0, 3, 5},
          new double[] {0, 0, 2}
      });
      INDArray ex2 = Nd4j.create(new double[][] {
          new double[] { 0, 0, 2},
          new double[] {0, 0, 2}
      });

      // A LSTM which adds one for every non-zero timestep
      org.deeplearning4j.nn.conf.layers.LSTM underlying = new org.deeplearning4j.nn.conf.layers.LSTM.Builder()
              .activation(Activation.IDENTITY)
              .gateActivationFunction(Activation.IDENTITY)
              .nIn(2)
              .nOut(1)
              .build();
      NeuralNetConfiguration conf = new NeuralNetConfiguration();
      conf.setLayer(underlying);
      INDArray params = Nd4j.zeros(new int[] {16});

      //Set the biases to 1.
      for (int i = 12;i < 16; i++) {
          params.putScalar(i, 1.0);
      }
      Layer lstm = underlying.instantiate(conf, Collections.<IterationListener>emptyList(), 0, params, false);
      MaskZeroLayer l = new MaskZeroLayer(lstm);
      INDArray input = Nd4j.create( Arrays.asList(ex1, ex2), new int[] {2, 2, 3});
      //WHEN
      INDArray out = l.activate(input);

      //THEN output should only be incremented for the non-zero timesteps
      INDArray firstExampleOutput = out.getRow(0);
      INDArray secondExampleOutput = out.getRow(1);

      assertEquals(firstExampleOutput.getDouble(0), 0.0, 1e-6);
      assertEquals(firstExampleOutput.getDouble(1), 1.0, 1e-6);
      assertEquals(firstExampleOutput.getDouble(2), 2.0, 1e-6);

      assertEquals(secondExampleOutput.getDouble(0), 0.0, 1e-6);
      assertEquals(secondExampleOutput.getDouble(1), 0.0, 1e-6);
      assertEquals(secondExampleOutput.getDouble(2), 1.0, 1e-6);

  }
}
