package blog.common.numerical;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import blog.common.Util;

/**
 * Creates MatrixLib objects.
 * 
 * All code should use this class to create new MatrixLib objects, instead
 * of importing JamaMatrixLib directly. This allows us to easily change the
 * underlying implementation in the future.
 */
public class MatrixFactory {
  /**
   * create a MatrixLib from array of doubles
   * 
   * @param array
   * @return
   */
  static public MatrixLib fromArray(double[][] array) {
    return new JamaMatrixLib(array);
  }

  /**
   * create identity matrix
   * 
   * @param size
   *          size of the identity matrix
   * @return
   */
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

  /**
   * create identity matrix
   * 
   * @param row
   *          number of rows
   * @param col
   *          number of cols
   * @return
   */
  static public MatrixLib eye(int row, int col) {
    double[][] result = new double[row][col];
    int size = (row < col ? row : col);
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

  /**
   * Returns a MatrixLib column vector of dimension args.length by 1. args is
   * interpreted as an array of doubles that comprise the first and only column
   * for the returned MatrixLib instance.
   * 
   * @param args
   * @return
   */
  static public MatrixLib createColumnVector(double... args) {
    double[][] ary = new double[args.length][1];
    for (int i = 0; i < args.length; i++) {
      ary[i][0] = args[i];
    }
    return MatrixFactory.fromArray(ary);
  }

  /**
   * Returns a MatrixLib row vector of dimension 1 by args.length. args is
   * interpreted as an array of doubles that comprise the first and only row for
   * the returned MatrixLib instance.
   * 
   * @param args
   * @return
   */
  static public MatrixLib createRowVector(double... args) {
    double[][] ary = new double[1][args.length];
    ary[0] = args;
    return MatrixFactory.fromArray(ary);
  }

  /**
   * create a matrix with all 0's
   * 
   * @param rows
   * @param cols
   * @return
   */
  static public MatrixLib zeros(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 0;
      }
    }
    return fromArray(result);
  }

  /**
   * create a column vector with all 0's
   * 
   * @param size
   * @return
   */
  static public MatrixLib zeros(int size) {
    double[][] result = new double[size][1];
    for (int i = 0; i < size; i++) {
      result[i][0] = 0;
    }
    return fromArray(result);
  }

  /**
   * create a matrix with all 1's
   * 
   * @param rows
   * @param cols
   * @return
   */
  static public MatrixLib ones(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 1;
      }
    }
    return fromArray(result);
  }

  /**
   * create a column with all 1's
   * 
   * @param size
   * @return
   */
  static public MatrixLib ones(int size) {
    double[][] result = new double[size][1];
    for (int i = 0; i < size; i++) {
      result[i][0] = 1;
    }
    return fromArray(result);
  }

  /**
   * vertically concatenate two matrix together
   * 
   * @param a
   * @param b
   * @return
   */
  static public MatrixLib vstack(MatrixLib a, MatrixLib b) {
    if (a.numCols() != b.numCols()) {
      throw new RuntimeException("matrices should have equal number of columns");
    }
    double[][] result = new double[a.numRows() + b.numRows()][a.numCols()];
    for (int i = 0; i < a.numRows(); i++) {
      for (int j = 0; j < a.numCols(); j++) {
        result[i][j] = a.elementAt(i, j);
      }
    }
    for (int i = 0; i < b.numRows(); i++) {
      for (int j = 0; j < b.numCols(); j++) {
        result[a.numRows() + i][j] = b.elementAt(i, j);
      }
    }
    return fromArray(result);
  }

  /**
   * horizontally concatenate two matrices together
   * 
   * @param a
   * @param b
   * @return
   */
  static public MatrixLib hstack(MatrixLib a, MatrixLib b) {
    if (a.numRows() != b.numRows()) {
      throw new RuntimeException("matrices should have equal number of rows");
    }
    double[][] result = new double[a.numRows()][a.numCols() + b.numCols()];
    for (int i = 0; i < a.numRows(); i++) {
      for (int j = 0; j < a.numCols(); j++) {
        result[i][j] = a.elementAt(i, j);
      }
    }
    for (int i = 0; i < b.numRows(); i++) {
      for (int j = 0; j < b.numCols(); j++) {
        result[i][a.numCols() + j] = b.elementAt(i, j);
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
    // Do Caching for matrix loading from Disk
    if (MatCache.containsKey(filename))
      return MatCache.get(filename);

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
      throw new IllegalArgumentException("File " + filename + " not found");
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading " + filename);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Could not parse matrix");
    }

    final int numRows = rows.size();
    final int numCols = rows.get(0).size();
    if (numRows == 0 || numCols == 0) {
      throw new IllegalArgumentException("Tried to read an empty matrix");
    }
    double[][] result = new double[numRows][numCols];
    for (int r = 0; r < numRows; r++) {
      ArrayList<Double> row = rows.get(r);
      if (row.size() != numCols) {
        throw new IllegalArgumentException("Matrix rows have unequal lengths");
      }
      for (int c = 0; c < numCols; c++) {
        result[r][c] = row.get(c);
      }
    }
    Util.debug("Loaded ", result.length, "x", result[0].length,
        " matrix from ", filename);
    MatrixLib mat = fromArray(result);
    MatCache.put(filename, mat);
    return mat;
  }

  private static final HashMap<String, MatrixLib> MatCache = new HashMap<String, MatrixLib>();
}
