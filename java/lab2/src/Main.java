import ru.ifmo.se.pokemon.*;
import pokemons.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle();
        Pokemon fp1 = new Igglybuff("Микрочел", 10);
        Pokemon fp2 = new Jigglypuff("Дефолтчел", 7);
        Pokemon fp3 = new Wigglytuff("Макрочел", 27);
        b.addAlly(fp1);
        b.addAlly(fp2);
        b.addAlly(fp3);
        Pokemon sp1 = new MimeJr("Бэбс", 10);
        Pokemon sp2 = new MrMime("Папич", 27);
        Pokemon sp3 = new Pheromosa("Неономи", 15);
        b.addFoe(sp1);
        b.addFoe(sp2);
        b.addFoe(sp3);
        b.go();
    }
}