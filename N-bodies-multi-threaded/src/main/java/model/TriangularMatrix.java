package model;

/**
 * Stores a Triangular Matrix of Forces without the main diagonal
 * Can't use Java Generics because they slow down the matrix too much.
 */
public class TriangularMatrix {
	private final int rowSize;
	private final Force[] elems;

	public TriangularMatrix(final int size) {
		this.rowSize = size;
		this.elems = new Force[TriangularMatrix.sumFormula(size)];
	}

	public void set(final int i, final int j, final Force value) {
		if (!this.isValid(i, j)) {
			return;
		}
		this.elems[TriangularMatrix.getElemIndex(i, j)] = value;
	}

	public Force get(final int i, final int j) {
		if (!this.isValid(i, j)) {
			return Force.NULL;
		}

		return this.elems[TriangularMatrix.getElemIndex(i, j)];
	}

	public void print() {
		for (int i = 0; i < this.rowSize; i++) {
			for (int j = i + 1; j < this.rowSize; j++) {
				System.out.print(this.get(i, j) + " ");
			}
			System.out.println();
		}
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