package com.company;

public class Thinging extends Feel1 {
    String Thing = new String("Неужели ");
    String Thing1 = new String("больше никогда не прилетит?");
    Human human;

    public Thinging(Human human) {
        this.human = human;
    }

    public void thing() {
        System.out.println(Thing.toString() + " " + human.name + Thing1.toString());
    }
}