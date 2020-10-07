package model;

/**
 * Stores a Triangular Matrix without the main diagonal
 */
public class TriangularMatrix {
	private static final double NULL = 0.0;

	private final int rowSize;
	private final double[] elems;

	public TriangularMatrix(final int size) {
		this.rowSize = size;
		this.elems = new double[TriangularMatrix.sumFormula(size)];
	}

	public void set(final int i, final int j, final double value) {
		if (!this.isValid(i, j)) {
			return;
		}
		this.elems[TriangularMatrix.getElemIndex(i, j)] = value;
	}

	public double get(final int i, final int j) {
		if (!this.isValid(i, j)) {
			return TriangularMatrix.NULL;
		}

		return this.elems[TriangularMatrix.getElemIndex(i, j)];
	}

	private static int getElemIndex(final int i, final int j) {
		return TriangularMatrix.sumFormula(j) + i;
	}

	private static int sumFormula(final int t) {
		final int x = t - 1;
		return (x * x + x) / 2;
	}

	private boolean isValid(final int i, final int j) {
		return i >= 0 && i < j && j > 0 && j < this.rowSize;
	}
}