package org.example;

import java.io.*;
import java.util.BitSet;


// сдвиг влево
public class Cipher {

    private static Integer REG_SIZE = 37;
    private static int[] indices = {12, 10, 2};
    private static BitSet register = new BitSet(REG_SIZE);
    private static BitSet key = new BitSet();

    public Cipher() {
    }

    public BitSet encrypt(String filePath, String inputRegister) {
        BitSet result = new BitSet();
        StringBuilder buffer = new StringBuilder();
        buffer.append(inputRegister);
        register = validate(buffer);


        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) { // Используем BufferedInputStream

            int byteRead;

            System.out.println("Биты файла: ");
            StringBuilder binaryString = new StringBuilder();

            while ((byteRead = bufferedInputStream.read()) != -1) {

                String bufferedString = String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0');
                bufferedString = bufferedString.replaceAll(" ", "");
                binaryString.append(bufferedString);

                System.out.print(bufferedString);

            }

            // ПРОВЕРИТЬ ПРАВИЛЬНОСТЬ ЗАПИСИ БИТОВ

            for (int i = 0; i < binaryString.length(); i++) {

                if (register.get(REG_SIZE - 1)) {
                    key.set(i);
                } else {
                    key.clear(i);
                }

                result.set(i, (binaryString.charAt(i) == '1' ^ key.get(i)));
                shiftRegister(); // ОСОБОЕ ВНИМАНИЕ ПРИ ПРОВЕРКЕ
            }

            System.out.println("\nБиты ключа: ");
            System.out.println(convertBitSetToString(key));

            System.out.println("Зашифрованный текст");
            System.out.println(convertBitSetToString(result));

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
            newBit = newBit ^ register.get(indices[i] - 1);
        }

        for (int i = REG_SIZE - 1; i > 0; i--) {
            if (register.get(i - 1)) {
                register.set(i);
            } else {
                register.clear(i);
            }
        }
        register.set(0, newBit);
        return;
    }

    private boolean keyAppend() {
        return register.get(REG_SIZE - 1) ? true : false;
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
                register.clear(REG_SIZE - counter - 1);
                counter++;
            }
        }
        return null;
    }

    public String convertBitSetToString(BitSet bitSet) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                result.append('1');
            } else {
                result.append('0');
            }
        }
        return result.toString();
    }

    public void convertBitSetToFile(BitSet bitSet, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte currentByte = 0;
            int bitCount = 0;

            for (int i = 0; i < bitSet.length(); i++) {
                if (bitSet.get(i)) {
                    currentByte |= (1 << (7 - (bitCount % 8)));
                }

                bitCount++;

                if (bitCount % 8 == 0) {
                    bos.write(currentByte);
                    currentByte = 0;
                }
            }

            if (bitCount % 8 != 0) {
                bos.write(currentByte);
            }

            System.out.println("BitSet успешно преобразован в файл: " + filePath);

        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
            e.printStackTrace();
        }
    }

}