package manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import models.Organization;

import java.io.*;
import java.util.List;

/**
 * Менеджер файлов.
 */
public class FileManager {
    private final String inputFileName;
    private final ConsoleManager console;
    private final CollectionManager collectionManager;

    public FileManager(String inputFileName, ConsoleManager console, CollectionManager collectionManager) {
        this.inputFileName = inputFileName;
        this.console = console;
        this.collectionManager = collectionManager;
    }

    /**
     * Проверить, можно ли читать файл.
     *
     * @param file    файл
     * @param console менеджер консоли
     * @return true, если можно читать файл, иначе false
     */
    public static boolean canRead(File file, ConsoleManager console) {
        if (!file.exists()) {
            console.printError("Файл не найден");
            return false;
        }

        if (!file.canRead()) {
            console.printError("Нет прав на чтение файла");
            return false;
        }

        if (!file.isFile()) {
            console.printError("Указанный путь не является файлом");
            return false;
        }

        return true;
    }

    /**
     * Сохранить коллекцию в файл.
     */
    public void saveCollection() {
        try {
            XmlMapper mapper = new XmlMapper();
            String xmlResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(collectionManager.getCollection());
            try (FileOutputStream fos = new FileOutputStream(inputFileName)) {
                fos.write(xmlResult.getBytes());
                fos.flush();
            }
        } catch (Exception e) {
            console.printError("Ошибка сохранения коллекции в файл");
        }
    }

    /**
     * Заполнить коллекцию из файла.
     */
    public void fillCollection() {
        File file = new File(inputFileName);
        if (!canRead(file, console)) {
            return;
        }

        try {
            XmlMapper xmlMapper = new XmlMapper();
            String xml = bufferedReaderToString(new BufferedReader(new FileReader(file)));
            List<Organization> to_check = xmlMapper.readValue(xml, new TypeReference<List<Organization>>() {
            });

            for (var organization : to_check) {
                if (ValidationManager.isValidOrganization(organization, collectionManager)) {
                    collectionManager.add(organization);
                } else {
                    console.printError("Организация с id " + organization.getId() + " не прошла валидацию");

                }
            }
        } catch (Exception e) {
            console.printError("Ошибка чтения файла");
        }
    }

    /**
     * Преобразовать BufferedReader в строку.
     *
     * @param br BufferedReader
     * @return строка
     * @throws IOException ошибка ввода/вывода
     */
    public String bufferedReaderToString(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
