package org.example;

import java.util.BitSet;
import java.util.Scanner;

//111111111111111111111111111111111111111111111111

public class Main {
    public static void main(String[] args) {
        Cipher cipher = new Cipher();
        System.out.println("Enter text to encrypt: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        BitSet bitSet = new BitSet();

        BitSet myBitSet = cipher.encrypt("C:\\Users\\vanya\\IdeaProjects\\TILab2\\inputfile.txt", input);
        cipher.convertBitSetToFile(myBitSet, "C:\\Users\\vanya\\IdeaProjects\\TILab2\\outputfile.txt");

        BitSet myBitSet2 = cipher.encrypt("C:\\Users\\vanya\\IdeaProjects\\TILab2\\outputfile.txt", input);
        cipher.convertBitSetToFile(myBitSet2, "C:\\Users\\vanya\\IdeaProjects\\TILab2\\outputfile2.txt");
    }
}