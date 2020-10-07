package model;

import org.junit.Assert;
import org.junit.Test;

public class TriangularMatrixTest {
	private static final double NULL = 0.0;
	private static final int SIZE = 7;
	private static int BIG_SIZE = 30000;
	private static final int OOM_DECREASE_SIZE = 3000;

	private TriangularMatrix tm;
	private double[][] matrix;

	private final boolean withPrints = true;

	@Test
	public void testCorrectness() {
		//Initialization
		this.matrix = new double[TriangularMatrixTest.SIZE][TriangularMatrixTest.SIZE];
		this.tm = new TriangularMatrix(TriangularMatrixTest.SIZE);

		double count = 1.0;
		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				if (j <= i) {
					this.matrix[i][j] = TriangularMatrixTest.NULL;
				} else {
					this.matrix[i][j] = count;
					this.tm.set(i, j, count);
					count++;
				}
			}
		}

		/*for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				System.out.print(this.matrix[i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println();
		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				System.out.print(this.tm.get(i, j) + " ");
			}
			System.out.println();
		}*/

		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				Assert.assertEquals("elem[" + i + "," + j + "]", this.tm.get(i, j), this.matrix[i][j], 0.0000000000001);
			}
		}
	}

	@Test
	public void testSpeed() {
		try {
			this.matrix = null;
			this.tm = null;

			Runtime.getRuntime().gc();  // Attempts to call the garbage collector
			if (this.withPrints) {
				System.out.println("SIZE: " + TriangularMatrixTest.BIG_SIZE);
				System.out.println();
				System.out.println("MATRIX");
			}

			DurationTracker dt = new DurationTracker("Creation Time").start();
			this.matrix = new double[TriangularMatrixTest.BIG_SIZE][TriangularMatrixTest.BIG_SIZE];
			double count = 1.0;
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					if (j <= i) {
						this.matrix[i][j] = TriangularMatrixTest.NULL;
					} else {
						this.matrix[i][j] = count;
						count++;
					}
				}
			}
			final long matrixCreationTime = dt.stop(this.withPrints);
			dt = new DurationTracker("Row Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					count = this.matrix[i][j];
				}
			}
			final long matrixRowAccess = dt.stop(this.withPrints);
			dt = new DurationTracker("Column Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					count = this.matrix[j][i];
				}
			}
			final long matrixColumnAccess = dt.stop(this.withPrints);
			this.matrix = null;

			if (this.withPrints) {
				System.out.println();
				System.out.println("TRIANGULAR MATRIX");
			}
			dt = new DurationTracker("Creation Time").start();
			this.tm = new TriangularMatrix(TriangularMatrixTest.BIG_SIZE);
			count = 1.0;
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = i + 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
					this.tm.set(i, j, count);
					count++;
				}
			}
			final long tmCreationTime = dt.stop(this.withPrints);
			dt = new DurationTracker("Row Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = i + 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
					count = this.tm.get(i, j);
				}
			}
			final long tmRowAccess = dt.stop(this.withPrints);
			dt = new DurationTracker("Column Access").start();
			for (int j = 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
				for (int i = 0; i < j; i++) {
					count = this.tm.get(j, i);
				}
			}
			final long tmColumnAccess = dt.stop(this.withPrints);

			final long matrixOverall = matrixCreationTime + matrixRowAccess + matrixColumnAccess;
			final long tmOverall = tmCreationTime + tmRowAccess + tmColumnAccess;
			if (this.withPrints) {
				System.out.println();
				System.out.println("-> MATRIX OVERALL: " + DurationTracker.toMillsDuration(matrixOverall));
				System.out.println("-> TRIANGULAR MATRIX OVERALL: " + DurationTracker.toMillsDuration(tmOverall));
			}

			Assert.assertTrue("TM create/read operations are overall faster with many elems",
					matrixOverall > tmOverall);

			if (this.withPrints) {
				System.out.println("-> TRIANGULAR MATRIX is " + (int) ((float) matrixOverall / tmOverall * 100)
						+ "% faster overall than MATRIX");
			}
		} catch (@SuppressWarnings("unused") final OutOfMemoryError e) {
			System.out.println();
			System.out.println("OutOfMemoryError.... retrying with smaller size.");
			TriangularMatrixTest.BIG_SIZE -= TriangularMatrixTest.OOM_DECREASE_SIZE;
			this.testSpeed();
		}
	}
}
