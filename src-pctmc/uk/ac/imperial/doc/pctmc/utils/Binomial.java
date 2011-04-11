package uk.ac.imperial.doc.pctmc.utils;

public class Binomial {
	public static int choose(int n, int k) {
		int ret = 1;
		for (int i = n; i > n - k; i--) {
			ret *= i;
		}
		for (int i = 1; i <= k; i++) {
			ret /= i;
		}
		return ret;
	}

}
