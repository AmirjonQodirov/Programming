package com.company;

public class Missing extends Feel1 {
    String feel = new String("И вдруг Малыш почувствовал, что он очень соскучился по:");
    Human human;

    public Missing(Human human) {
        this.human = human;
    }

    public void feel() {
        System.out.println(feel.toString() + " " + human.name + " " + human.Character);
    }
}