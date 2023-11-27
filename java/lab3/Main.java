import creatures.Moomin;
import enums.Enviroment;
import enums.MoveType;
import interfaces.Movable;
import room.*;
import room.Floor;
import utils.Sun;

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
        CupOfTea teaCup = new CupOfTea();
        teaCup.checkIfSpilling(floor.getRotationSpeed());

        // Стол
        Table table = new Table();

        // Стулья
        Chair chair1 = new Chair();
        Chair chair2 = new Chair();
        Chair chair3 = new Chair();

        // Семья Муми-Троллей
        Moomin father = new Moomin("Арам");
        Moomin mother = new Moomin("Ксения");
        Moomin kid = new Moomin("Рофлер");

        // Трельяж
        Trellie trellie = new Trellie();
        // Плятаной шкаф
        Wardrobe wardrobe = new Wardrobe();

        Movable[] toMove = {
                table,
                chair1, chair2, chair3, // стулья
                father, mother, kid, // семья Мумми-Троллей
                trellie,
                wardrobe
        };

        // Ехали по кругу
        for (Movable elem : toMove) {
            elem.move(MoveType.CIRCLE);
        }
    }
}
