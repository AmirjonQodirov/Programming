package com.company;

public class Mother extends Human {
    Mother(String name, Day day) {
        super(name, day);
    }

    void spank() {
        if (day == Day.Terrible)
            System.out.println("Мать " + name + " Шлёпнула лучшего в мире специалиста по паровым машинам.");
        else System.out.println("Мать " + name + " Поцеловала Малыша");
    }
}