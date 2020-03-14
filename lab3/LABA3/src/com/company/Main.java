package com.company;

enum Day {Good, Terrible}

public class Main {
    public static void main(String[] args) {
        Baby Малыш = new Baby("Малыш", Day.Good);
        Karlson Карлсон = new Karlson("Карлсон", Day.Terrible, "бодрому, веселому человечку, который так потешно махал своей маленькой рукой, приговаривая:");
        Mother Мама = new Mother("Мама", Day.Terrible);
        Missing feell = new Missing(Карлсон);
        Thinging thing = new Thinging(Карлсон);
        feell.feel();
        Карлсон.say();
        thing.thing();
        Малыш.feel();
        Мама.spank();
    }
}