package blog.common.numerical;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.common.Util;


/**
 * Creates MatrixLib objects.
 *
 * All code should use this class to create new MatrixLib objects, instead
 * of importing JamaMatrixLib directly. This allows us to easily change the
 * underlying implementation in the future.
 */
public class MatrixFactory {
  static public MatrixLib fromArray(double[][] array) {
    return new JamaMatrixLib(array);
  }

  static public MatrixLib eye(int size) {
    double[][] result = new double[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (i == j) {
          result[i][j] = 1;
        } else {
          result[i][j] = 0;
        }
      }
    }
    return fromArray(result);
  }

  static public MatrixLib zeros(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 0;
      }
    }
    return fromArray(result);
  }

  static public MatrixLib ones(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 1;
      }
    }
    return fromArray(result);
  }

  static public MatrixLib vstack(MatrixLib a, MatrixLib b)
  {
    if (a.colLen() != b.colLen()) {
      throw new RuntimeException("matrices should have equal number of columns");
    }
    double[][] result = new double[a.rowLen() + b.rowLen()][a.colLen()];
    for (int i = 0; i < a.rowLen(); i++) {
      for (int j = 0; j < a.colLen(); j++) {
        result[i][j] = a.elementAt(i, j);
      }
    }
    for (int i = 0; i < b.rowLen(); i++) {
      for (int j = 0; j < b.colLen(); j++) {
        result[a.rowLen() + i][j] = b.elementAt(i, j);
      }
    }
    return fromArray(result);
  }

  /**
   * Read matrix from space-separated text file.
   *
   * To save in this format from numpy: savetxt('a.txt', a)
   * To save in this format from matlab: save('a.txt', 'a', '-ascii')
   */
  static public MatrixLib fromTxt(String filename) {
    ArrayList<ArrayList<Double>> rows = new ArrayList<ArrayList<Double>>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        ArrayList<Double> row = new ArrayList<Double>();
        for (String word : line.trim().split(" +", -1)) {
          row.add(Double.parseDouble(word));
        }
        rows.add(row);
      }
      reader.close();
    } catch (FileNotFoundException e) {
      Util.fatalError(e);
    } catch (IOException e) {
      Util.fatalError(e);
    }

    double[][] result = new double[rows.size()][rows.get(0).size()];
    for (int r = 0; r < rows.size(); r++) {
      ArrayList<Double> row = rows.get(r);
      // TODO: should check that all rows have the same size
      for (int c = 0; c < row.size(); c++) {
        result[r][c] = row.get(c);
      }
    }
    System.out.println("Loaded " + result.length + "x" + result[0].length +
      " matrix from " + filename);
    return fromArray(result);
  }
}
