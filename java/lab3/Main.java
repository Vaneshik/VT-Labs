import creatures.Moomin;
import creatures.Moomins;
import enums.Enviroment;
import enums.MoveType;
import things.*;

public class Main {
    public static void main(String[] args) {
        // Cолнце садилось в полной тишине и молчании
        Sun sun = new Sun();
        sun.setEnviroment(Enviroment.SILENCE);
        sun.goDown();

        // Начал вращаться пол, сначала медленно
        Floor floor = new Floor();
        floor.rotate();

        // Потом все быстрее и быстрее
        floor.speedUp(1);
        floor.speedUp(2);

        // Чай выплескивался из чашек
        Cup cup = new Cup();
        cup.checkSpeed(floor.getRotationSpeed());

        // Стол
        Table table = new Table();

        // Стулья
        Chair chair1 = new Chair();
        Chair chair2 = new Chair();
        Chair chair3 = new Chair();
        Chairs chairs = new Chairs(chair1, chair2, chair3);

        // Семья Муми-Троллей
        Moomin father = new Moomin("Арам");
        Moomin mother = new Moomin("Ксения");
        Moomin kid = new Moomin("Динах");
        Moomins moominFamily = new Moomins(father, mother, kid);

        // Трельяж
        Trellie trellie = new Trellie();
        // Плятаной шкаф
        Wardrobe wardrobe = new Wardrobe();

        MovableThing[] toMove = {table, chairs, moominFamily, trellie, wardrobe};

        // Ехали по кругу
        for (MovableThing elem : toMove) {
            elem.move(MoveType.CIRCLE);
            System.out.println(elem.describe());
        }

    }
}
