package util;

import exceptions.ScriptRecursionException;
import models.Organization;
import models.forms.OrganizationForm;
import network.Request;
import network.Response;
import network.User;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.lang.System.exit;

public class Client {
    private final Set<Path> scriptsNames = new TreeSet<>();
    private final InetAddress host;
    private final int port;
    private SocketChannel channel;
    private User user;

    public Client(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
            SocketAddress address = new InetSocketAddress(host, port);
            channel = SocketChannel.open();
            channel.connect(address);

            PromptScan.setUserScanner(new Scanner(System.in));
            var scanner = PromptScan.getUserScanner();

            System.out.println("Клиентское приложение запущено. Для просмотра доступных команд введите 'help'");

            authenticateUser();

            while (true) {
                System.out.print("> ");
                try {
                    while (scanner.hasNext()) {
                        var command = "";
                        var arguments = "";
                        String[] input = (scanner.nextLine() + " ").trim().split(" ");

                        command = input[0].trim();
                        processUserPrompt(command, Arrays.copyOfRange(input, 1, input.length));
                        System.out.print("> ");
                    }

                } catch (NoSuchElementException e) {
                    System.out.println("Остановка клиента через консоль");
                    exit(1);
                } catch (ClassNotFoundException e) {
                    System.out.println("Объект поступивший в ответ от сервера не найден");
                } catch (SocketException e) {
                    System.out.println("Сервер был остановлен во время обработки вашего запроса. Пожалуйста, повторите попытку позже");
                    exit(1);
                }
            }

        } catch (ConnectException e) {
            System.out.println("Сервер недоступен в данный момент. Пожалуйста, повторите попытку позже");
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода");
        }
    }

    private void authenticateUser(){
        var scanner = PromptScan.getUserScanner();
        var username = "";
        var password = "";

        try {
            while (true){
                System.out.println("Введите логин: ");
                username = scanner.nextLine();

                System.out.println("Введите пароль: ");
                password = scanner.nextLine();

                user = new User(username, PasswordHasher.getHash(password));

                var userAuthenticationRequest = new Request(user, false);
                Response response = sendAndReceive(userAuthenticationRequest);

                if (response.isUserAuthentication()){
                    printResponse(response);
                    break;
                } else {
                    printResponse(response);

                    if (response.getMessage().equals("Пользователя " + user.getUsername() + " не существует")){
                        System.out.println("Если вы хотите зарегистрироваться, нажмите 'y'");
                        var ans = scanner.nextLine().trim();

                        if (ans.equalsIgnoreCase("y")){
                            while (!registerUser()){
                                registerUser();
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            System.out.println("Остановка клиента через консоль");
            System.exit(1);
        }
    }

    private boolean registerUser() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        try {
            var scanner = PromptScan.getUserScanner();

            var username = "";
            var password = "";

            System.out.println("Введите логин: ");
            username = scanner.nextLine();

            System.out.println("Введите пароль: ");
            password = scanner.nextLine();

            user = new User(username, PasswordHasher.getHash(password));

            var userAuthenticationRequest = new Request(user, true);
            Response response = sendAndReceive(userAuthenticationRequest);
            printResponse(response);
            return response.isUserAuthentication();

        } catch (NoSuchElementException e) {
            System.out.println("Остановка клиента через консоль");
            System.exit(1);
            return false;
        }
    }

    private void processUserPrompt(String command, String[] arguments) throws IOException, ClassNotFoundException {
        Request request;
        if (command.equalsIgnoreCase("add") || command.equalsIgnoreCase("update") || command.equalsIgnoreCase("remove_greater") || command.equalsIgnoreCase("add_if_min") || command.equalsIgnoreCase("remove_lower")) {
            Organization objArgument = new OrganizationForm(user.getUsername()).build();
            request = new Request(user, command, arguments, objArgument);
            sendAndReceive(request);
        } else if (command.equalsIgnoreCase("exit")) {
            System.out.println("Работа клиентского приложения завершена");
            exit(0);
        } else if (command.equalsIgnoreCase("execute_script")) {
            if (arguments.length == 0) {
                System.out.println("Неверные аргументы команды");
            } else if (arguments.length == 1) {
                executeScript(arguments[0]);
            } else {
                System.out.println("Неверное количество аргументов");
            }
        } else {
            request = new Request(user, command, arguments);
            Response response = sendAndReceive(request);
            printResponse(response);
        }
    }

    private Response sendAndReceive(Request request) throws IOException, ClassNotFoundException {

        try(ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes)) {

            out.writeObject(request);
            ByteBuffer dataToSend = ByteBuffer.wrap(bytes.toByteArray());
            channel.write(dataToSend); // отправляем серверу запрос
            out.flush();
        }

        ByteBuffer dataToReceiveLength = ByteBuffer.allocate(8);
        channel.read(dataToReceiveLength); // читаем длину ответа от сервера
        dataToReceiveLength.flip();
        int responseLength = dataToReceiveLength.getInt(); // достаём её из буфера

        ByteBuffer responseBytes = ByteBuffer.allocate(responseLength); // создаем буфер нужной нам длины
        ByteBuffer packetFromServer = ByteBuffer.allocate(256);

        while (true){
            channel.read(packetFromServer);
            if (packetFromServer.position() == 2 && packetFromServer.get(0) == 28 && packetFromServer.get(1) == 28) break;
            packetFromServer.flip();
            responseBytes.put(packetFromServer);
            packetFromServer.clear();
        }

        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(responseBytes.array()))){
            return (Response) in.readObject();
        }
    }



    private void printResponse(Response response) {
        System.out.println(response.getMessage());
        String collection = response.getCollectionToStr();
        try{
            if (!collection.isEmpty()){
                System.out.println(collection);
            }
        } catch (NullPointerException ignored){}
    }

    private void executeScript(String path) throws ClassNotFoundException {
        if (path.isBlank()) {
            System.out.println("Неверные аргументы команды");

        } else {
            try {
                Path pathToScript = Paths.get(path);

                PromptScan.setUserScanner(new Scanner(pathToScript));
                Scanner scriptScanner = PromptScan.getUserScanner();

                Path scriptFile = pathToScript.getFileName();

                if (!scriptScanner.hasNext()) throw new NoSuchElementException();

                scriptsNames.add(scriptFile);

                do {
                    var command = "";
                    String[] input = (scriptScanner.nextLine() + " ").trim().split(" ");
                    command = input[0].trim();

                    while (scriptScanner.hasNextLine() && command.isEmpty()) {
                        input = (scriptScanner.nextLine() + " ").trim().split(" ", 2);
                        command = input[0].trim();
                    }

                    if (command.equalsIgnoreCase("execute_script")) {
                        try {
                            Path scriptNameFromArgument = Paths.get(input[1]).getFileName();

                            if (scriptsNames.contains(scriptNameFromArgument)) {
                                throw new ScriptRecursionException("Один и тот же скрипт не может выполнятся рекурсивно");
                            }
                            executeScript(input[1]);

                        } catch (ScriptRecursionException e) {
                            System.out.println(e.getMessage());
                        }

                    } else {
                        processUserPrompt(command, Arrays.copyOfRange(input, 1, input.length));
                    }

                } while (scriptScanner.hasNextLine());

                scriptsNames.remove(scriptFile);
                PromptScan.setUserScanner(new Scanner(System.in));
                System.out.println("Скрипт " + scriptFile + " успешно выполнен");

            } catch (FileNotFoundException e) {
                System.out.println("Файл " + path + " не найден");
            } catch (NoSuchElementException e) {
                System.out.println("Файл " + path + " пуст");
            } catch (IllegalStateException e) {
                System.out.println("Непредвиденная ошибка");
            } catch (SecurityException e) {
                System.out.println("Недостаточно прав для чтения файла " + path);
            } catch (IOException e) {
                System.out.println("Ошибка ввода/вывода");
                System.out.println(e.getClass().getName());
            } catch (InvalidPathException e) {
                System.out.println("Проверьте путь к файлу. В нём не должно быть лишних символов");
            }
        }


    }
}