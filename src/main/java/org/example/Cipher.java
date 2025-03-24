package org.example;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.BitSet;


// сдвиг влево
public class Cipher {

    private static Integer REG_SIZE = 37;
    private static int[] indices = {12, 10, 2};
    private static BitSet register = new BitSet(REG_SIZE);
    private static BitSet key = new BitSet();

    private BitSet encrypt(String filePath, StringBuilder inputRegister) {
        BitSet result = new BitSet();
        register = validate(inputRegister);


        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) { // Используем BufferedInputStream

            int byteRead;
            System.out.println("Биты файла:");
            String binaryString = ""; // не безопасно, подправить

            while ((byteRead = bufferedInputStream.read()) != -1) {

                binaryString = String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0');
                binaryString = binaryString.replaceAll(" ", "");


                System.out.print(binaryString + " ");

            }

            // ПРОВЕРИТЬ ПРАВИЛЬНОСТЬ ЗАПИСИ БИТОВ

            for (int i = 0; i < binaryString.length(); i++) {
                result.set(i, (binaryString.charAt(i) == '1' ^ keyAppend()));
                shiftRegister(); // ОСОБОЕ ВНИМАНИЕ ПРИ ПРОВЕРКЕ
            }

            System.out.println();

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private void shiftRegister() {

        boolean newBit = register.get(REG_SIZE - 1);

        for (int i = 0; i < indices.length; i++) {
            newBit = newBit ^ key.get(indices[i]);
        }

        for (int i = REG_SIZE - 1; i > 0; i--) {
            if (register.get(i - 1)) {
                register.set(i);
            } else {
                register.clear(i);
            }
        }
        register.set(REG_SIZE - 1, newBit);
        return;
    }

    private BitSet validate(StringBuilder input) {
        int counter = 0;
        for (int i = 0; i < input.length(); i++) {

            if (REG_SIZE.equals(counter)) {
                return register;
            }

            if (input.charAt(i) == '1') {
                register.set(REG_SIZE - counter - 1);
                counter++;
            } else if (input.charAt(i) == '0') {
                register.clear(counter);
                counter++;
            }
        }
        return null;
    }

    private boolean keyAppend() {
        return register.get(REG_SIZE - 1) ? true : false;
    }

}
