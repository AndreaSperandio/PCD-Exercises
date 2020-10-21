package model;

/**
 * Stores a Triangular Matrix of Forces without the main diagonal
 * Can't use Java Generics because they slow down the matrix too much.
 */
public class TriangularMatrix {
	private final int rowSize;
	private final int nRows;
	private final Force[] elems;

	public TriangularMatrix(final int rowSize) {
		this(rowSize, rowSize);
	}

	public TriangularMatrix(final int nRows, final int rowSize) {
		this.rowSize = rowSize;
		this.nRows = nRows < rowSize ? nRows : rowSize;
		this.elems = new Force[this.getFirstElem(rowSize)];
	}

	public void set(final int i, final int j, final Force value) {
		if (!this.isValid(i, j)) {
			return;
		}
		this.elems[this.getElemIndex(i, j)] = value;
	}

	public Force get(final int i, final int j) {
		if (!this.isValid(i, j)) {
			return Force.NULL;
		}

		return this.elems[this.getElemIndex(i, j)];
	}

	public void print() {
		for (int i = 0; i < this.nRows; i++) {
			for (int j = i + 1; j < this.rowSize; j++) {
				System.out.print(this.get(i, j) + " ");
			}
			System.out.println();
		}
	}

	public int getRowSize() {
		return this.rowSize;
	}

	/** Inefficient method */
	public static TriangularMatrix copyOfRange(final TriangularMatrix matrix, final int nRows) {
		final TriangularMatrix newMatrix = new TriangularMatrix(nRows, matrix.rowSize);
		for (int i = 0; i < nRows; i++) {
			for (int j = i + 1; j < newMatrix.rowSize; j++) {
				newMatrix.set(i, j, matrix.get(i, j));
			}
		}
		return newMatrix;
	}

	private int getElemIndex(final int i, final int j) {
		return this.getFirstElem(j) + i;
	}

	/** Consinders a Triangular Matrix without the main diagonal and having nRows rows only */
	private int getFirstElem(final int col) {
		final int prec = col - 1;
		if (this.nRows == this.rowSize) {
			return TriangularMatrix.sumFormula(prec);
		}

		final int delta = prec - this.nRows;
		return TriangularMatrix.sumFormula(prec) - TriangularMatrix.sumFormula(delta);
	}

	/** Sum of the first n positive elements: (n + 1) * n / 2 = (n * n + n) / 2 */
	private static int sumFormula(final int n) {
		return n > 0 ? (n * n + n) / 2 : 0;
	}

	private boolean isValid(final int i, final int j) {
		return i >= 0 && i < j && i < this.nRows && j > 0 && j < this.rowSize;
	}
}