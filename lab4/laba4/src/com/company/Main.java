package com.company;


import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

enum Day {Good, Terrible}
public class Main {
    public static void main(String[] args) {
        Baby Малыш = new Baby("Малыш", Day.Terrible,"Лежал на :");
        Karlson Карлсон = new Karlson("Карлсон", Day.Terrible, "бодрому, веселому человечку, который так потешно махал своей маленькой рукой, приговаривая:");
        Human.Mother Мама = new Human.Mother("Мать", Day.Terrible);
        Missing Скучать = new Missing(Карлсон);
        Thinging Думать = new Thinging(Карлсон);
        Place Места = new Place(Малыш);
        Скучать.feel();
        Карлсон.say();
        Думать.thing();
        Малыш.feel();
        Мама.spank();
        Места.Deystviya_na_meste();
     try {
         Малыш.read();
     }catch (Error2 e){e.getMessage();}

        Малыш.listen();
        Карлсон.actions();
        try{
            Карлсон.fly();
        }
        catch(Error1 ex){
            ex.getMessage(); }
            Карлсон.sing();
    }
}
class Human {
    String name;
    Day day;
    String Character;
    String thing;

    Human(String name, Day day) {
        this.name = name;
        this.day = day;
    }
    static class Mother extends Human {
        Mother(String name, Day day) {
            super(name, day);
        }

        void spank() {
            if (day == Day.Terrible)
                System.out.println("Мать " + name + " Шлёпнула лучшего в мире специалиста по паровым машинам.");
            else System.out.println("Мать " + name + " Поцеловала Малыша");
        }
    }
}
interface Feel {
    void feel();
}
interface Thing {
    void thing();
}
class Missing {
    String feel = new String("И вдруг Малыш почувствовал, что он очень соскучился по:");
    Human human;

    public Missing(Human human) {
        this.human = human;
    }

    public void feel() {
        System.out.println(feel.toString() + " " + human.name + " " + human.Character);
    }
}
class Thinging {
    String Thing = new String("Неужели ");
    String Thing1 = new String("больше никогда не прилетит?");
    Human human;

    Thinging(Human human) {
        this.human = human;
    }

    void thing() {
        System.out.println(Thing.toString() + " " + human.name + " " + Thing1.toString());
    }
}
class Karlson extends Human {
    Karlson(String name, Day day, String Character) {
        super(name, day);
        this.Character = Character;
    }
    void say() {
        if (day == Day.Terrible)
            System.out.println("Неприятности -- это пустяки, дело житейское, и расстраиваться тут нечего");
        else System.out.println("Не грусти Малыш когда тебе будет плохо я приду!!!");
    }
    void fly() {

        Place p = (new Random().nextBoolean() ?
                new Place("потолок"," висячих картин на стенах") :
                null);

        if(p == null) {
            throw new Error1(this);
        }

        System.out.println(name + " летал вокруг " + p.place_in_home + " и мимо" + p.place_in_home1);
    }
    void sing(){

        System.out.println(name + " что то пел");}
    void actions(){
        Place p = new Place("потолок"," висячих картин на стенах");
        System.out.println(name + " смотрел медленно на: " + p.place_in_home1 + " и при этом он склонял набок голову и прищуривал глазки");
    }
}
class Baby extends Human implements Thing, Feel {
    String read = "книга";
    Baby(String name, Day day1,String Character) {
        super(name, day1);
        this.Character=Character;
    }
    void read ()throws Error2 {
        if (name == "") {
            throw new Error2();
        } else {
            System.out.println(name + " читал " + read);
        }
    }


    void listen(){
        System.out.println(name + " услышал за окном какое-то жужжание");
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
class Place {
   String place_in_home;
   String place_in_home1;
   Place(String place_in_home ,String place_in_home1){
       this.place_in_home = place_in_home;
       this.place_in_home1 = place_in_home1;
   }
    String place = new String("пол");
    Human human;
   Place(Human human) {
       this.human=human;}
    void Deystviya_na_meste() {
        System.out.println(human.name + " " + human.Character + " " + place.toString());
    }
}
