package com.company;

public class Karlson extends Human {
    Karlson(String name, Day day, String Character) {
        super(name, day);
        this.Character = Character;
    }

    public void say() {
        if (day == Day.Terrible)
            System.out.println("Неприятности -- это пустяки, дело житейское, и расстраиваться тут нечего");
        else System.out.println("Не грусти Малыш когда тебе будет плохо я приду!!!");
    }
}