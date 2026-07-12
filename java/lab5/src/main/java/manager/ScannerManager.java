package manager;

import lombok.Getter;
import lombok.Setter;

import java.util.Scanner;

/**
 * Менеджер сканнеров.
 */
public class ScannerManager {
    @Getter
    @Setter
    private static Scanner scanner = new Scanner(System.in);
    private ScannerManager() {
    }

}
