package manager;

import command.*;
import models.Organization;
import network.Request;
import network.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class Server {
    private InetSocketAddress address;
    private Selector selector;
    private volatile Response response;
    private Request request;
    protected static Logger logger;
    private CollectionManager collectionManager;
    private CommandManager commandManager;

    private ExecutorService readThreadPool;
    private ExecutorService processThreadPool;
    private ExecutorService sendThreadPool;


    public Server(InetSocketAddress address) {
        this.address = address;
        logger = LogManager.getLogger(Server.class);
        this.collectionManager = new CollectionManager();
        this.commandManager = new CommandManager();
        this.readThreadPool = Executors.newCachedThreadPool();
        this.processThreadPool = Executors.newFixedThreadPool(10);
        this.sendThreadPool = Executors.newFixedThreadPool(10);
    }

    public void run(String[] args) {
        try {
            commandManager.addCommand("help", new HelpCommand(commandManager));
            commandManager.addCommand("info", new InfoCommand(collectionManager));
            commandManager.addCommand("show", new ShowCommand(collectionManager));
            commandManager.addCommand("add", new AddCommand(collectionManager));
            commandManager.addCommand("update", new UpdateCommand(collectionManager));
            commandManager.addCommand("remove_by_id", new RemoveByIdCommand(collectionManager));
            commandManager.addCommand("clear", new ClearCommand(collectionManager));
            commandManager.addCommand("add_if_min", new AddIfMinCommand(collectionManager));
            commandManager.addCommand("remove_greater", new RemoveGreaterCommand(collectionManager));
            commandManager.addCommand("remove_lower", new RemoveLowerCommand(collectionManager));
            commandManager.addCommand("min_by_full_name", new MinByFullNameCommand(collectionManager));
            commandManager.addCommand("filter_contains_name", new FilterContainsNameCommand(collectionManager));
            commandManager.addCommand("filter_less_than_postal_address", new FilterLessThanPostalAddressCommand(collectionManager));

            DBProvider.load(collectionManager);
            logger.info("Коллекция загружена");

            selector = Selector.open();
            logger.info("Селектор открыт");

            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(address);
            serverChannel.configureBlocking(false);

            logger.info("Канал сервера готов к работе");
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    logger.info("Обработка ключа началась");

                    try {
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                                SocketChannel clientChannel = serverSocketChannel.accept();

                                logger.info("Установлено соединение с клиентом " + clientChannel.socket().toString());

                                clientChannel.configureBlocking(false);
                                clientChannel.register(selector, SelectionKey.OP_READ);
                            }

                            if (key.isReadable()) {
                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                clientChannel.configureBlocking(false);

                                readThreadPool.submit(() -> {
                                    try {
                                        readRequest(clientChannel, key);
                                    } catch (SocketException e) {
                                        logger.info("Клиент " + key.channel().toString() + " отключился");
                                        key.cancel();
                                    } catch (IOException e) {
                                        logger.error("Ошибка ввода вывода");
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                        logger.error("Class cast ошибка");
                                    }
                                });

                                Thread processRequestThread = new Thread(this::processRequest);
                                processRequestThread.start();
                                processRequestThread.join();

                                clientChannel.register(selector, SelectionKey.OP_WRITE);
                            }

                            if (key.isWritable()) {
                                SocketChannel clientChannel = (SocketChannel) key.channel();
                                clientChannel.configureBlocking(false);

                                processThreadPool.submit(() -> {
                                    processRequest();

                                    sendThreadPool.submit(() -> {
                                        try {
                                            sendResponse(clientChannel);
                                            clientChannel.register(selector, SelectionKey.OP_READ);
                                        } catch (SocketException e) {
                                            logger.info("Клиент " + key.channel().toString() + " отключился");
                                            key.cancel();
                                        } catch (IOException e) {
                                            logger.error("Ошибка ввода вывода");
                                            e.printStackTrace();
                                        }
                                    });
                                });

                                clientChannel.register(selector, SelectionKey.OP_READ);
                            }
                        }
                    } catch (SocketException | CancelledKeyException e) {
                        logger.info("Клиент " + key.channel().toString() + " отключился");
//                        fileManager.saveCollection();
                        key.cancel();
                    } catch (InterruptedException e ) {
                        e.printStackTrace();
                        logger.error("Поток обработки запроса или отправки запроса был прерван");
                    }
                    keys.remove();
                }
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
        } catch (NoSuchElementException e) {
            System.out.println(1);
            logger.error("Остановка сервера через консоль");
//            fileManager.saveCollection();
            System.exit(1);
        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода");
        } catch (Exception e) {
            logger.error("Ошибка сервера: " + e.getMessage());
        } finally {
            readThreadPool.shutdown();
            processThreadPool.shutdown();
            sendThreadPool.shutdown();
        }
    }

    private synchronized void readRequest(SocketChannel clientChannel, SelectionKey key) throws IOException, ClassNotFoundException {
        ByteBuffer clientData = ByteBuffer.allocate(2048);

        logger.info(clientChannel.read(clientData) + " байт пришло от клиента");
        try (ObjectInputStream clientDataIn = new ObjectInputStream(new ByteArrayInputStream(clientData.array()))) {
            request = (Request) clientDataIn.readObject();
        } catch (StreamCorruptedException e) {
            key.cancel();
        }
    }

    private synchronized void processRequest() {
        if (request.getCommandName() == null) {
            var user = request.getUser();

            if (!request.isRegisterRequired()) {
                if (DBProvider.checkUserExistence(user.getUsername())) {
                    if (DBProvider.checkUserPassword(user)) {
                        response = new Response("Привет, " + user.getUsername() + "\n", true);
                        logger.info("Пользователь " + user.getUsername() + " успешно аутентифицирован");

                    } else {
                        response = new Response("Пароль введён неверно", false);
                        logger.info("Пользователь " + user.getUsername() + " неверно ввёл пароль");
                    }

                } else {
                    response = new Response("Пользователя " + user.getUsername() + " не существует", false);
                    logger.info("Пользователя " + user.getUsername() + " не существует");
                }

            } else {
                if (DBProvider.checkUserExistence(user.getUsername())){
                    response = new Response("Такой пользователь есть в системе(", false);
                } else {
                    DBProvider.addUser(user);
                    response = new Response("Добро пожаловать, " + user.getUsername() + "\n", true);
                    logger.info("Пользователь " + user.getUsername() + " успешно зарегистрирован");
                }
            }

        } else {
            var commandName = request.getCommandName();
            var commandStrArg = request.getCommandStrArg();
            var commandObjArg = (Organization) request.getCommandObjArg();
            var user = request.getUser();

            if (commandManager.getCommands().containsKey(commandName)) {
                response = commandManager.getCommands().get(commandName).execute(user, commandStrArg, commandObjArg);
            } else {
                response = new Response("Команда не найдена. Используйте help для справки", "");
            }


            logger.info("Запрос:\n" + commandName + "\n" + commandStrArg + "\n" + commandObjArg + "\nУспешно обработан");
        }
    }

    private synchronized void sendResponse(SocketChannel clientChannel) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream clientDataOut = new ObjectOutputStream(bytes)) {
            clientDataOut.writeObject(response);

            var byteResponse = bytes.toByteArray();

            ByteBuffer dataLength = ByteBuffer.allocate(8).putInt(byteResponse.length);
            dataLength.flip();
            clientChannel.write(dataLength);
            logger.info("Отправлен пакет с длинной сообщения");

            while(byteResponse.length > 256){
                ByteBuffer packet = ByteBuffer.wrap(Arrays.copyOfRange(byteResponse, 0, 256));
                clientChannel.write(packet);
                byteResponse = Arrays.copyOfRange(byteResponse, 256, byteResponse.length);
                logger.info("Отправлен пакет байтов длины: " + packet.position());
            }
            ByteBuffer packet = ByteBuffer.wrap(byteResponse);
            clientChannel.write(packet);
            Thread.sleep(300);
            logger.info("Отправлен последний пакет байтов длины: " + packet.position());
            ByteBuffer stopPacket = ByteBuffer.wrap(new byte[]{28, 28});
            clientChannel.write(stopPacket);
            logger.info("Отправлен стоп пакет\n");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}