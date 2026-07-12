package lab1.task1;

public class SinSeries {

    private static final int DEFAULT_TERMS = 20;

    public static double sin(double x) {
        return sin(x, DEFAULT_TERMS);
    }

    public static double sin(double x, int terms) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return Double.NaN;
        }
        if (terms <= 0) {
            throw new IllegalArgumentException("Number of terms must be positive");
        }

        // [-pi, pi]
        x = x % (2 * Math.PI);
        if (x > Math.PI) {
            x -= 2 * Math.PI;
        } else if (x < -Math.PI) {
            x += 2 * Math.PI;
        }

        double result = 0.0;
        double term = x;

        for (int n = 0; n < terms; n++) {
            result += term;
            term *= -x * x / ((2 * n + 2) * (2 * n + 3));
        }

        return result;
    }
}
