package io.exe.examples.efficiency;

public class ArrayTest {

	private static final int MAX = 10000;
	private final int[][] array2d = new int[MAX][MAX];

	public int efficient2d() {
		int result = 0;

		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				result &= array2d[i][j];
			}
		}

		return result;
	}

	public int inefficient2d() {
		int result = 0;

		for (int i = 0; i < array2d.length; i++) {
			for (int j = 0; j < array2d[i].length; j++) {
				result &= array2d[j][i];
			}
		}

		return result;
	}

}
