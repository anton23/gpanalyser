#include <algorithm>

namespace J
{
	inline double div (double a, double b)
	{
		if (!b)
		{
			return 0.0;
		}
		return a / b;
	}

	inline double divdivmin (double a, double b, double c, double d)
	{
		return div (a * b, c * d) * std::min (c, d);
	}

	inline double divmin (double a, double b, double c)
	{
		return div (a, b) * std::min (c, b);
	}

	inline double ifpos (double c, double n, double p)
	{
		if (c < 0)
		{
			return n;
		}
		return p;
	}

	inline double chebyshev (double e, double m)
	{
		return m / (m + e);
	}
}
