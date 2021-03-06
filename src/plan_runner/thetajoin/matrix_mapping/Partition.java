package plan_runner.thetajoin.matrix_mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * This abstract class implement the notion of partition of a matrix, typically
 * a set of part that respect the conditions: 1) no overlapping 2) cover the
 * whole matrix
 */

public abstract class Partition implements MatrixAssignment, Serializable {

	private static final long serialVersionUID = 1L;
	private static Random randGen = new Random();

	protected Matrix matrix_;
	protected int numReducers_;
	protected Part parts_[];

	// Constructor
	public Partition(Matrix _matrix, int _numReducers) {
		matrix_ = _matrix;
		numReducers_ = _numReducers;
		parts_ = new Part[numReducers_];

		generatePartition();
	}

	/**
	 * This method returns a cost value for the current matrix partition
	 * 
	 * @return cost value for the current partition scheme
	 */
	public double calculateCost() {
		int reducers_used = 0;

		for (int i = 0; i < numReducers_; ++i)
			if (parts_[i].getArea() != 0)
				reducers_used++;
		// This forces the partitioner to use the maximum number of reducers
		// possible.
		// It is currently used as stub until a better cost function if found.
		return 1 / ((double) reducers_used);
	}

	protected abstract void generatePartition();

	/*
	 * @return the area of the largest part of the partition.
	 */
	public int getMaxArea() {
		int max = 0;
		for (int i = 0; i < numReducers_; ++i)
			if (parts_[i].getArea() > max)
				max = parts_[i].getArea();
		return max;
	}

	/*
	 * @return the half perimeter of the largest part of the partition.
	 */
	public int getMaxHalfPerimeter() {
		int max = 0;
		for (int i = 0; i < numReducers_; ++i)
			if (parts_[i].getHalfPerimeter() > max)
				max = parts_[i].getHalfPerimeter();
		return max;
	}

	/**
	 * Method to get a list of indexes of Part (workers) that are on a
	 * row/column chosen at random
	 * 
	 * @param _dimension
	 *            a Dimension indicating in which direction we want to cross the
	 *            matrix. (Dimension.ROW or Dimension.COLUMN)
	 * @return the list of indexes of workers
	 */
	@Override
	public ArrayList<Integer> getRegionIDs(Dimension _dimension) {

		final ArrayList<Integer> retList = new ArrayList<Integer>();

		if (_dimension == Dimension.ROW) { // tuple from relation S
			final int sIndex = randGen.nextInt(matrix_.getSizeOfS());
			for (int i = 0; i < numReducers_; ++i)
				if (((!matrix_.isSGreaterThanT()) && parts_[i].intersectRow(sIndex))
						|| ((matrix_.isSGreaterThanT()) && parts_[i].intersectColumn(sIndex)))
					retList.add(i);
		} else { // tuple from relation T
			final int tIndex = randGen.nextInt(matrix_.getSizeOfT());
			for (int i = 0; i < numReducers_; ++i)
				if (((matrix_.isSGreaterThanT()) && parts_[i].intersectRow(tIndex))
						|| ((!matrix_.isSGreaterThanT()) && parts_[i].intersectColumn(tIndex)))
					retList.add(i);
		}
		return retList;
	}

	/**
	 * @return the sum of half perimeters of Part in the Partition
	 */
	public int getSumHalfPerimeter() {
		int sum = 0;
		for (int i = 0; i < numReducers_; ++i)
			sum += parts_[i].getHalfPerimeter();
		return sum;
	}

	@Override
	public String toString() {
		String ret = "";
		ret = ret.concat("Print of a Partition: \n");
		ret = ret.concat(matrix_.toString());
		ret = ret.concat("Number of reducers: " + numReducers_ + "\n");
		for (int i = 0; i < numReducers_; ++i) {
			ret = ret.concat("Partition " + i + ": \n ");
			ret = ret.concat("[( " + parts_[i].getWIndex() + ", "
					+ (parts_[i].getWIndex() + parts_[i].getWidth()) + "), ("
					+ parts_[i].getHIndex() + ", "
					+ (parts_[i].getHIndex() + parts_[i].getHeight()) + ")]\n");
		}
		return ret;
	}

	/**
	 * This method check that each portion of the matrix is covered by exactly
	 * one part.
	 * 
	 * @return true if all partition conditions are respected false otherwise
	 */
	public boolean valid() {
		for (int h = 0; h < matrix_.getHeight(); ++h)
			for (int w = 0; w < matrix_.getWidth(); ++w) {
				int index = 0;
				for (int i = 0; i < numReducers_; ++i)
					if (parts_[i].covers(h, w))
						index++;
				if (index != 1)
					return false;
			}
		return true;
	}

}
