package org.example;

import java.io.*;
import java.util.BitSet;


// сдвиг влево
    class Cipher {

    public static final Integer REG_SIZE = 39;
    private static final int[] indices = {4};
    private static BitSet register = new BitSet(REG_SIZE);
    public static BitSet key = new BitSet();


    public static class EncryptionResult {
        public final BitSet encryptedData;
        public final String originalFileBits;
        public final int totalBits;

        public EncryptionResult(BitSet encryptedData, String originalFileBits, int totalBits) {
            this.encryptedData = encryptedData;
            this.originalFileBits = originalFileBits;
            this.totalBits = totalBits;
        }
    }

    public Cipher() { }

    public EncryptionResult encrypt(String filePath, String inputRegister) {
        BitSet encryptedResult = new BitSet();
        StringBuilder buffer = new StringBuilder(inputRegister);
        register = validate(buffer);

        if (register == null) {
            System.err.println("Ошибка: Не удалось инициализировать регистр.");
            return new EncryptionResult(new BitSet(), "", 0);
        }

        StringBuilder binaryString = new StringBuilder(); // Строка для всех битов файла


        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            int byteRead;
            while ((byteRead = bufferedInputStream.read()) != -1) {
                String bits = String.format("%8s", Integer.toBinaryString(byteRead & 0xFF)).replace(' ', '0');
                binaryString.append(bits);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
            return new EncryptionResult(new BitSet(), "", 0); // Пустой результат при ошибке чтения
        }

        int totalBits = binaryString.length();
        for (int i = 0; i < totalBits; i++) {
            boolean keyBit = register.get(REG_SIZE - 1);
            key.set(i, keyBit);

            boolean fileBit = (binaryString.charAt(i) == '1');
            encryptedResult.set(i, fileBit ^ keyBit);

            shiftRegister();
        }

        System.out.println("\n(Отладка) Биты файла прочитаны: " + totalBits + " бит");
        System.out.println("(Отладка) Биты ключа сгенерированы: " + key.length() + " бит (фактически " + totalBits + ")");
        System.out.println("(Отладка) Биты шифртекста: " + encryptedResult.length() + " бит (фактически " + totalBits + ")");

        return new EncryptionResult(encryptedResult, binaryString.toString(), totalBits);
    }

    private void shiftRegister() {
        boolean newBit = register.get(REG_SIZE - 1);
        for (int index : indices) {
            if (index > 0 && index <= REG_SIZE) {
                newBit ^= register.get(index - 1);
            } else {
                System.err.println("Предупреждение: Неверный индекс отступа в LFSR: " + index);
            }
        }
        for (int i = REG_SIZE - 1; i > 0; i--) {
            register.set(i, register.get(i - 1));
        }
        register.set(0, newBit);
    }

    private BitSet validate(StringBuilder input) {
        BitSet tempRegister = new BitSet(REG_SIZE);
        int counter = 0;
        for (int i = 0; i < input.length() && counter < REG_SIZE; i++) {
            char c = input.charAt(i);
            if (c == '1') {
                tempRegister.set(REG_SIZE - 1 - counter);
                counter++;
            } else if (c == '0') {
                counter++;
            }
        }
        if (counter < REG_SIZE) {
            System.out.println("Предупреждение: Входная строка регистра короче REG_SIZE (" + REG_SIZE + "). Оставшиеся биты установлены в 0.");
        }
        return tempRegister;
    }

    public static String convertBitSetToString(BitSet bitSet, int length) {
        if (bitSet == null) return "";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(bitSet.get(i) ? '1' : '0');
        }
        return result.toString();
    }

    public static void convertBitSetToFile(BitSet bitSet, int bitLength, String filePath) {
        if (bitSet == null) {
            System.err.println("Ошибка: Попытка записи null BitSet в файл.");
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte currentByte = 0;
            int bitCount = 0;
            for (int i = 0; i < bitLength; i++) {
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
            System.out.println("BitSet ("+ bitLength +" бит) успешно преобразован в файл: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при записи BitSet в файл '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
}