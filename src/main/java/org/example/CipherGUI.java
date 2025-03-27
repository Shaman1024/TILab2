package org.example;

import javax.swing.*;
import java.awt.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;

public class CipherGUI extends JFrame {

    private JTextField filePathField;
    private JTextField registerField;
    private JTextArea originalBitsAreaFirst;
    private JTextArea keyBitsAreaFirst;
    private JTextArea encryptedBitsAreaFirst;
    private JTextArea originalBitsAreaLast;
    private JTextArea keyBitsAreaLast;
    private JTextArea encryptedBitsAreaLast;

    private JTextArea logArea;
    private JButton browseButton;
    private JButton encryptButton;
    private JFileChooser fileChooser;

    private final int BITS_TO_DISPLAY = 2 * Cipher.REG_SIZE;

    public CipherGUI() {
        super("LFSR Потоковый Шифратор");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        pack();
        setMinimumSize(getPreferredSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        registerField = new JTextField(Cipher.REG_SIZE);

        originalBitsAreaFirst = createBitsTextArea();
        keyBitsAreaFirst = createBitsTextArea();
        encryptedBitsAreaFirst = createBitsTextArea();
        originalBitsAreaLast = createBitsTextArea();
        keyBitsAreaLast = createBitsTextArea();
        encryptedBitsAreaLast = createBitsTextArea();

        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        browseButton = new JButton("Обзор...");
        encryptButton = new JButton("Шифровать");
        fileChooser = new JFileChooser();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Файл:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(filePathField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        add(browseButton, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        add(new JLabel("Нач. регистр (" + Cipher.REG_SIZE + " бит):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        add(registerField, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        add(encryptButton, gbc);
        gbc.anchor = GridBagConstraints.WEST;

        int currentRow = 3;
        gbc.gridwidth = 3;

        gbc.gridx = 0; gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Первые " + BITS_TO_DISPLAY + " бит исходного файла:"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(originalBitsAreaFirst), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Соответствующие биты ключа (первые):"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(keyBitsAreaFirst), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Первые " + BITS_TO_DISPLAY + " бит зашифрованного файла:"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(encryptedBitsAreaFirst), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Последние " + BITS_TO_DISPLAY + " бит исходного файла:"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(originalBitsAreaLast), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Соответствующие биты ключа (последние):"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(keyBitsAreaLast), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Последние " + BITS_TO_DISPLAY + " бит зашифрованного файла:"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.2;
        add(new JScrollPane(encryptedBitsAreaLast), gbc);

        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0.0;
        add(new JLabel("Лог:"), gbc);
        gbc.gridy = currentRow++; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        add(new JScrollPane(logArea), gbc);

        browseButton.addActionListener(e -> browseFile());
        encryptButton.addActionListener(e -> encryptFile());
    }

    private JTextArea createBitsTextArea() {
        JTextArea textArea = new JTextArea(3, 40);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return textArea;
    }


    private void browseFile() {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            log("Выбран файл: " + selectedFile.getName());
            clearBitAreas();
        }
    }

    private void encryptFile() {
        String filePath = filePathField.getText();
        String initialRegister = registerField.getText().trim();

        if (filePath.isEmpty()) {
            showError("Файл не выбран.");
            return;
        }
        if (initialRegister.isEmpty()) {
            showError("Начальное состояние регистра не введено.");
            return;
        }

        if (initialRegister.length() != Cipher.REG_SIZE) {
            log("Предупреждение: Длина регистра ("+ initialRegister.length()
                    +") не равна "+ Cipher.REG_SIZE +". Будет использовано/дополнено.");
        }

        File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            showError("Выбранный файл не найден или не является файлом.");
            return;
        }

        log("Начало шифрования файла: " + inputFile.getName());
        encryptButton.setEnabled(false);
        clearBitAreas();

        SwingWorker<Cipher.EncryptionResult, String> worker = new SwingWorker<>() {
            private String outputFilePath = null;
            private String initialBitsStrFirst = "";
            private String keyBitsStrFirst = "";
            private String encryptedBitsStrFirst = "";
            private String initialBitsStrLast = "";
            private String keyBitsStrLast = "";
            private String encryptedBitsStrLast = "";
            private boolean success = false;
            private int totalBits = 0;


            @Override
            protected Cipher.EncryptionResult doInBackground() throws Exception {
                Cipher.EncryptionResult encryptionResult = null;
                try {
                    Cipher cipher = new Cipher();
                    Cipher.key = new BitSet();
                    encryptionResult = cipher.encrypt(filePath, initialRegister);
                    totalBits = encryptionResult.totalBits;

                    if (encryptionResult.encryptedData == null || (encryptionResult.encryptedData.isEmpty() && totalBits > 0)) {
                        throw new Exception("Шифрование вернуло пустой результат для непустого файла.");
                    }
                    publish("Файл успешно зашифрован в памяти (" + totalBits + " бит).");

                    outputFilePath = generateUniqueFilename(filePath);
                    publish("Имя выходного файла: " + Paths.get(outputFilePath).getFileName());

                    Cipher.convertBitSetToFile(encryptionResult.encryptedData, totalBits, outputFilePath);
                    publish("Зашифрованные данные сохранены.");

                    int bitsToShow = Math.min(BITS_TO_DISPLAY, totalBits);
                    int startIndexLast = Math.max(0, totalBits - bitsToShow);

                    initialBitsStrFirst = encryptionResult.originalFileBits.substring(0, bitsToShow);
                    keyBitsStrFirst = formatBitSetRange(Cipher.key, 0, bitsToShow);
                    encryptedBitsStrFirst = formatBitSetRange(encryptionResult.encryptedData, 0, bitsToShow);

                    initialBitsStrLast = encryptionResult.originalFileBits.substring(startIndexLast);
                    keyBitsStrLast = formatBitSetRange(Cipher.key, startIndexLast, totalBits - startIndexLast);
                    encryptedBitsStrLast = formatBitSetRange(encryptionResult.encryptedData, startIndexLast, totalBits - startIndexLast);

                    success = true;

                } catch (Exception ex) {
                    publish("Ошибка во время шифрования: " + ex.getMessage());
                    ex.printStackTrace();
                    throw ex;
                }
                return encryptionResult;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }

            @Override
            protected void done() {
                encryptButton.setEnabled(true);
                try {
                    get();
                    if (success) {
                        originalBitsAreaFirst.setText(initialBitsStrFirst);
                        keyBitsAreaFirst.setText(keyBitsStrFirst);
                        encryptedBitsAreaFirst.setText(encryptedBitsStrFirst);

                        originalBitsAreaLast.setText(initialBitsStrLast);
                        keyBitsAreaLast.setText(keyBitsStrLast);
                        encryptedBitsAreaLast.setText(encryptedBitsStrLast);

                        String resultMessage = "Шифрование завершено (" + totalBits + " бит). Результат: " + outputFilePath;
                        log(resultMessage);
                        JOptionPane.showMessageDialog(CipherGUI.this,
                                resultMessage, "Успех", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        clearBitAreas();
                        JOptionPane.showMessageDialog(CipherGUI.this,
                                "Произошла ошибка. Подробности в логе.",
                                "Ошибка шифрования", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    clearBitAreas();
                    String errorMsg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                    log("Критическая ошибка шифрования: " + errorMsg);
                    JOptionPane.showMessageDialog(CipherGUI.this,
                            "Произошла критическая ошибка: " + errorMsg,
                            "Ошибка шифрования", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void clearBitAreas() {
        originalBitsAreaFirst.setText("");
        keyBitsAreaFirst.setText("");
        encryptedBitsAreaFirst.setText("");
        originalBitsAreaLast.setText("");
        keyBitsAreaLast.setText("");
        encryptedBitsAreaLast.setText("");
    }

    private void showError(String message) {
        log("Ошибка: " + message);
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void log(String message) {
        if (SwingUtilities.isEventDispatchThread()) {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> {
                logArea.append(message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    private String generateUniqueFilename(String originalPath) {
        Path path = Paths.get(originalPath);
        String dir = path.getParent() != null ? path.getParent().toString() : ".";
        String filename = path.getFileName().toString();
        String baseName;
        String extension;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            baseName = filename.substring(0, dotIndex);
            extension = filename.substring(dotIndex);
        } else {
            baseName = filename;
            extension = "";
        }
        String suffix = "_encrypted";
        String newFilename = baseName + suffix + extension;
        Path outputPath = Paths.get(dir, newFilename);
        int counter = 1;
        while (Files.exists(outputPath)) {
            newFilename = String.format("%s%s(%d)%s", baseName, suffix, counter++, extension);
            outputPath = Paths.get(dir, newFilename);
        }
        return outputPath.toString();
    }

    private String formatBitSetRange(BitSet bitSet, int startIndex, int count) {
        if (bitSet == null || startIndex < 0 || count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        int endIndex = startIndex + count;
        for (int i = startIndex; i < endIndex; i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }
    // 10111110 10001100 11100001 10010110 0011001
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Не удалось установить системный Look and Feel.");
            }
            new CipherGUI();
        });
    }
}