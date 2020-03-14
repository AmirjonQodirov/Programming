package com.company;

class Error1 extends RuntimeException {
    Karlson p;

    Error1(Karlson p) {
        this.p = p;
    }

    @Override
    public String getMessage() {
        return "Объект класса не существует"; // use p field
    }
}
