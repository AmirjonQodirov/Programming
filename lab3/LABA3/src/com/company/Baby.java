package com.company;

public class Baby extends Human implements Thing, Feel {
    Baby(String name, Day day1) {
        super(name, day1);
    }

    @Override
    public void feel() {
        if (day == Day.Good) System.out.println("Сегодня хороший день вдруг Карлсон вернётся");
        else System.out.println("");
    }

    @Override
    public void thing() {
        System.out.println(thing);
    }
}