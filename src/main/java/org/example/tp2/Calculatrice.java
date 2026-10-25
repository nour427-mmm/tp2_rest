package org.example.tp2;

public class Calculatrice {

    public int somme(int a, int b) {
        return a + b;
    }

    public int soustraction(int a, int b) {
        return a - b;
    }

    public int produit(int a, int b) {
        return a * b;
    }

    public double division(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division par zéro impossible");
        }
        return (double) a / b;
    }
}