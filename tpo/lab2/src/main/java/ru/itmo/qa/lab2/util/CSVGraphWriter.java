package ru.itmo.qa.lab2.util;

import lombok.Getter;
import lombok.SneakyThrows;
import ru.itmo.qa.lab2.function.AbstractFunction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;

import static java.lang.String.format;

public class CSVGraphWriter {

    private final BufferedWriter writer;
    private final AbstractFunction function;
    @Getter
    private final String filePath;

    public CSVGraphWriter(AbstractFunction function, String outputDir) {
        this.function = function;
        this.filePath = getFilePath(outputDir, function);
        this.writer = createWriter();
    }

    public CSVGraphWriter(BufferedWriter writer, String outputDir, AbstractFunction function) {
        this.function = function;
        this.filePath = getFilePath(outputDir, function);
        this.writer = writer;
    }

    private String getFilePath(String outputDir, AbstractFunction function) {
        return outputDir + function.getClass().getSimpleName() + ".csv";
    }

    @SneakyThrows(IOException.class)
    private BufferedWriter createWriter() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            return new BufferedWriter(new FileWriter(file, false));
        } else {
            file.createNewFile();
            return new BufferedWriter(new FileWriter(file));
        }
    }

    @SneakyThrows(IOException.class)
    public void write(BigDecimal x1, BigDecimal x2, BigDecimal d, BigDecimal precision) {
        try {
            writer.write("x,y");
            writer.newLine();
            for (BigDecimal i = x1; i.compareTo(x2) <= 0; i = i.add(d)) {
                try {
                    BigDecimal y = function.calculate(i, precision);
                    if (y != null) {
                        writer.write(format(Locale.ENGLISH, "%f,%f%n", i.doubleValue(), y.doubleValue()));
                    } else {
                        writer.newLine();
                    }
                } catch (ArithmeticException e) {
                    writer.newLine();
                }
            }
        } finally {
            writer.flush();
        }
    }
}
