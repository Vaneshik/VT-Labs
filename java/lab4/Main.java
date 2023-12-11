import creatures.Moomin;
import enums.Enviroment;
import enums.LiquidType;
import enums.MoveType;
import exceptions.IllegalWindowMove;
import interfaces.Bumpable;
import interfaces.Movable;
import room.Floor;
import room.furniture.Chair;
import room.furniture.Table;
import room.furniture.Trellie;
import room.furniture.Wardrobe;
import room.items.Cup;
import utils.Weather;

public class Main {
    public static void main(String[] args) {
        room.Room room = new room.Room();

        // Семья Муми-Троллей
        Moomin father = new Moomin("Арам");
        Moomin mother = new Moomin("Ксения");
        Moomin kid = new Moomin("Рофлер");

        // Муми-Тролли выглянули и увидели
        for (Moomin m : new Moomin[]{father, mother, kid}) {
            m.lookOut();
        }

        // Добавляем Муми-Троллей в комнату
        room.addMoomins(father);
        room.addMoomins(mother);
        room.addMoomins(kid);

        Weather weather = new Weather();

        // Cолнце мирно опускается на блестящую гладь летнего моря.
        Weather.Sun sun = weather.new Sun();
        sun.setEnviroment(Enviroment.PEACEFUL);
        sun.stop();

        // Вдруг поднялась буря
        Weather.Storm storm = weather.new Storm();
        storm.start();

        // Закрыть окно от дождя
        try {
            room.getWindow().close();
        } catch (IllegalWindowMove e) {
            System.out.println(e);
        }

        //  Волны бьются об отдаленный берег
        Bumpable waves = new Bumpable() {
            @Override
            public void bump(String o) {
                System.out.println("Волны бьются о " + o + ".");
            }
        };
        waves.bump("отдаленный берег");

        // Льет дождь
        Weather.Rain rain = weather.new Rain();
        rain.start();

        // Налетела гроза
        Weather.ThunderStorm thunderStorm = weather.new ThunderStorm();
        thunderStorm.start();

        // Слышались отдаленные раскаты грома
        thunderStorm.getThunder().rumble();

        // Яркие молнии вспыхивали в зале
        thunderStorm.getLightning().strike();

        // Грохотал гром
        thunderStorm.getThunder().rumble();

        // Начал вращаться пол, сначала медленно
        Floor floor = new Floor();
        floor.rotate();

        // Потом все быстрее и быстрее
        floor.speedUp(1);
        floor.speedUp(2);

        // Чай выплескивался из чашек
        Cup teaCup = new Cup(LiquidType.TEA);
        teaCup.checkIfSpilling(floor.getRotationSpeed());

        // Стол
        Table table = new Table();

        // Стулья
        Chair chair1 = new Chair();
        Chair chair2 = new Chair();
        Chair chair3 = new Chair();

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

        // Все закончилось так же внезапно, как и началось. Гром, молнии, дождь и ветер тоже прекратились.
        weather.stop();

        // Погода хорошая, открыть окно!
        try {
            room.getWindow().open();
        } catch (IllegalWindowMove e) {
            System.out.println(e);
        }
    }
}
