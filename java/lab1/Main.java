// Variant -> 2522

public class Main {
    public static void main(String[] args) {
        // create c with required numbers
        long[] c = { 15, 13, 11, 9, 7, 5, 3 };

        // create x
        double[] x = new double[13];

        // fill x with required random doubles
        double min = -5.0d, max = 2.0d;
        for (int i = 0; i < x.length; i++) {
            x[i] = Math.random() * (max - min) + min;
        }

        double[][] arr = new double[7][13];

        // calc values in array
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 13; j++) {
                arr[i][j] = switch ((int) c[i]) {
                    case 5:
                        yield Math.log(Math.pow(Math.sin(Math.sin(Math.pow((x[j] + 0.5), 2))), 2));
                    case 9:
                        yield Math.log(Math.pow((2 * (Math.PI / (2 + Math.sqrt(Math.abs(x[j]))))), 2));
                    case 11:
                        yield Math.log(Math.pow(Math.sin(Math.sin(Math.pow((x[j] + 0.5), 2))), 2));
                    case 13:
                        yield Math.log(Math.pow(Math.sin(Math.sin(Math.pow((x[j] + 0.5), 2))), 2));
                    default:
                        yield Math.atan(0.25 * Math.cos(Math.log(Math.abs(x[j]) / 2)));
                };
            }
        }

        // Print calculated array
        for (double[] arrayOfDoubles : arr) {
            for (double elem : arrayOfDoubles) {
                System.out.printf("%10.4f", elem);
            }
            System.out.println();
        }
    }
}
