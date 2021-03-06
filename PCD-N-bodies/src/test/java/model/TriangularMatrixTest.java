package model;

import org.junit.Assert;
import org.junit.Test;

import control.DurationTracker;

public class TriangularMatrixTest {
	private static final int SIZE = 7;
	private static final int N_ROWS = 3;
	private static int BIG_SIZE = 10000;  // equals to 100mln of Forces for matrix
	private static final int OOM_DECREASE_SIZE = 3000;

	private TriangularMatrix tm;
	private TriangularMatrix tmNRows;
	private Force[][] matrix;

	private final boolean withPrints = true;

	@Test
	public void testCorrectness() {
		//Initialization
		this.matrix = new Force[TriangularMatrixTest.SIZE][TriangularMatrixTest.SIZE];
		this.tm = new TriangularMatrix(TriangularMatrixTest.SIZE);

		double count = 1.0;
		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				if (j <= i) {
					this.matrix[i][j] = Force.NULL;
				} else {
					this.matrix[i][j] = new Force(count, count);
					this.tm.set(i, j, new Force(count, count));
					count++;
				}
			}
		}

		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				Assert.assertEquals("elem[" + i + "," + j + "]", this.tm.get(i, j), this.matrix[i][j]);
			}
		}
	}

	@Test
	public void testNRowsCorrectness() {
		//Initialization
		this.tm = new TriangularMatrix(TriangularMatrixTest.SIZE);
		this.tmNRows = new TriangularMatrix(TriangularMatrixTest.N_ROWS, TriangularMatrixTest.SIZE);

		double count = 1.0;
		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = +1; j < TriangularMatrixTest.SIZE; j++) {
				this.tm.set(i, j, new Force(count, count));
				if (i < TriangularMatrixTest.N_ROWS) {
					this.tmNRows.set(i, j, new Force(count, count));
				}
				count++;
			}
		}

		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				Assert.assertEquals("NRows_elem[" + i + "," + j + "]", this.tmNRows.get(i, j),
						i < TriangularMatrixTest.N_ROWS ? this.tm.get(i, j) : Force.NULL);
			}
		}

		this.tmNRows = TriangularMatrix.copyOfRange(this.tm, TriangularMatrixTest.N_ROWS);
		for (int i = 0; i < TriangularMatrixTest.SIZE; i++) {
			for (int j = 0; j < TriangularMatrixTest.SIZE; j++) {
				Assert.assertEquals("CopyOf_elem[" + i + "," + j + "]", this.tmNRows.get(i, j),
						j > i && i < TriangularMatrixTest.N_ROWS ? this.tm.get(i, j) : Force.NULL);
			}
		}
	}

	@Test
	public void testSpeed() {
		try {
			this.matrix = null;
			this.tm = null;
			Runtime.getRuntime().gc();  // Attempts to call the garbage collector
			Thread.sleep(3000);

			if (this.withPrints) {
				System.out.println("SIZE: " + TriangularMatrixTest.BIG_SIZE);
				System.out.println();
				System.out.println("MATRIX");
			}

			DurationTracker dt = new DurationTracker("Creation Time").start();
			this.matrix = new Force[TriangularMatrixTest.BIG_SIZE][TriangularMatrixTest.BIG_SIZE];
			double count = 1.0;
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					if (j <= i) {
						this.matrix[i][j] = Force.NULL;
					} else {
						this.matrix[i][j] = new Force(count, count);
						count++;
					}
				}
			}
			final long matrixCreationTime = dt.stop(this.withPrints);
			@SuppressWarnings("unused")
			Force force;
			dt = new DurationTracker("Row Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					force = this.matrix[i][j];
				}
			}
			final long matrixRowAccess = dt.stop(this.withPrints);
			dt = new DurationTracker("Column Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = 0; j < TriangularMatrixTest.BIG_SIZE; j++) {
					force = this.matrix[j][i];
				}
			}
			final long matrixColumnAccess = dt.stop(this.withPrints);

			this.matrix = null;
			this.tm = null;
			Runtime.getRuntime().gc();  // Attempts to call the garbage collector
			Thread.sleep(3000);

			if (this.withPrints) {
				System.out.println();
				System.out.println("TRIANGULAR MATRIX");
			}
			dt = new DurationTracker("Creation Time").start();
			this.tm = new TriangularMatrix(TriangularMatrixTest.BIG_SIZE);
			count = 1.0;
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = i + 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
					this.tm.set(i, j, new Force(count, count));
					count++;
				}
			}
			final long tmCreationTime = dt.stop(this.withPrints);
			dt = new DurationTracker("Row Access").start();
			for (int i = 0; i < TriangularMatrixTest.BIG_SIZE; i++) {
				for (int j = i + 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
					force = this.tm.get(i, j);
				}
			}
			final long tmRowAccess = dt.stop(this.withPrints);
			dt = new DurationTracker("Column Access").start();
			for (int j = 1; j < TriangularMatrixTest.BIG_SIZE; j++) {
				for (int i = 0; i < j; i++) {
					force = this.tm.get(i, j);
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

			Assert.assertTrue("TM read operations are overall faster with many elems",
					matrixOverall - matrixCreationTime > tmOverall - tmCreationTime);

			if (this.withPrints) {
				System.out.println("-> TRIANGULAR MATRIX is " + (int) ((float) matrixOverall / tmOverall * 100)
						+ "% faster overall than MATRIX");
			}
		} catch (@SuppressWarnings("unused") final OutOfMemoryError e) {
			System.out.println();
			System.out.println("OutOfMemoryError.... retrying with smaller size.");
			TriangularMatrixTest.BIG_SIZE -= TriangularMatrixTest.OOM_DECREASE_SIZE;
			this.testSpeed();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}