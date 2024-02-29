package models;

public class Location {
    private Long x; //Поле не может быть null
    private Double y; //Поле не может быть null
    private String name; //Строка не может быть пустой, Поле может быть null

    public Location(Long x, Double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }
}
