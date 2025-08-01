import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;
import org.json.*;

class Point {
    int x;
    BigInteger y;

    public Point(int x, BigInteger y) {
        this.x = x;
        this.y = y;
    }
}

// Helper class to handle rational numbers with BigInteger
class Fraction {
    BigInteger numerator;
    BigInteger denominator;

    Fraction(BigInteger num, BigInteger den) {
        BigInteger gcd = num.gcd(den);
        if (den.signum() < 0) {
            gcd = gcd.negate();
        }
        this.numerator = num.divide(gcd);
        this.denominator = den.divide(gcd);
    }

    Fraction add(Fraction other) {
        BigInteger num = this.numerator.multiply(other.denominator).add(other.numerator.multiply(this.denominator));
        BigInteger den = this.denominator.multiply(other.denominator);
        return new Fraction(num, den);
    }

    Fraction multiply(BigInteger val) {
        return new Fraction(this.numerator.multiply(val), this.denominator);
    }

    BigInteger toBigInteger() {
        return numerator.divide(denominator); // denominator is 1 for integer result
    }
}

public class Main {

    public static void main(String[] args) throws Exception {
        // 1. Read JSON
        String filePath = "input1.json";
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject jsonObject = new JSONObject(content);

        JSONObject keys = jsonObject.getJSONObject("keys");
        int n = keys.getInt("n");
        int k = keys.getInt("k");

        List<Point> points = new ArrayList<>();

        // 2. Extract and decode
        for (String key : jsonObject.keySet()) {
            if (key.equals("keys")) continue;

            int x = Integer.parseInt(key);
            JSONObject entry = jsonObject.getJSONObject(key);
            int base = Integer.parseInt(entry.getString("base"));
            String value = entry.getString("value");

            BigInteger y = new BigInteger(value, base);
            points.add(new Point(x, y));
        }

        // 3. Sort and select k points
        points.sort(Comparator.comparingInt(p -> p.x));
        List<Point> selectedPoints = points.subList(0, k);

        // 4. Compute C = a0 using Lagrange interpolation at x = 0
        BigInteger result = lagrangeInterpolationAtZero(selectedPoints);

        // 5. Output
        System.out.println("C = " + result);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write("C = " + result.toString() + "\n");
        }
    }

    static BigInteger lagrangeInterpolationAtZero(List<Point> points) {
        Fraction result = new Fraction(BigInteger.ZERO, BigInteger.ONE);
        int k = points.size();

        for (int i = 0; i < k; i++) {
            Point pi = points.get(i);

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                Point pj = points.get(j);

                numerator = numerator.multiply(BigInteger.valueOf(-pj.x));
                denominator = denominator.multiply(BigInteger.valueOf(pi.x - pj.x));
            }

            Fraction li = new Fraction(numerator, denominator);
            result = result.add(li.multiply(pi.y));
        }

        return result.toBigInteger();
    }
}
