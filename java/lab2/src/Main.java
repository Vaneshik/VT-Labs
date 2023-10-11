import ru.ifmo.se.pokemon.*;
import pokemons.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle();
        Pokemon fp1 = new Igglybuff("микрочел", 10);
        Pokemon fp2 = new Jigglypuff("дефолтчел", 7);
        Pokemon fp3 = new Wigglytuff("макрочел", 8);
        Pokemon fp4 = new MimeJr("микроклоун", 20);
        Pokemon fp5 = new MrMime("макроклоун", 17);
        Pokemon fp6 = new Pheromosa("усач", 30);
        b.addAlly(fp1);
        b.addAlly(fp2);
        b.addAlly(fp3);
        b.addAlly(fp4);
        b.addAlly(fp5);
        b.addAlly(fp6);
        Pokemon sp1 = new Igglybuff("комок", 4);
        Pokemon sp2 = new Jigglypuff("рофлер", 14);
        Pokemon sp3 = new Wigglytuff("Гыгыгы", 4);
        Pokemon sp4 = new MimeJr("бэбс", 10);
        Pokemon sp5 = new MrMime("папич", 27);
        Pokemon sp6 = new Pheromosa("хз кто это", 15);
        b.addFoe(sp1);
        b.addFoe(sp2);
        b.addFoe(sp3);
        b.addFoe(sp4);
        b.addFoe(sp5);
        b.addFoe(sp6);
        b.go();
    }
}