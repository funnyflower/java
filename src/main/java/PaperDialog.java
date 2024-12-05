import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

public class PaperDialog extends JDialog {
    private JPanel contentPane;
    private JTextField textFieldAuthor;
    private JTextField textFieldTitle;
    private JTextField textFieldYear;
    private JTextField textFieldMonth;
    private JComboBox<Paper.Category> comboBoxCategory;
    private JButton buttonAdd;
    private JButton buttonDelete;
    private JButton buttonEdit;
    private JButton buttonSearch;
    private JComboBox comboBoxSearchCriteria;
    private JTextField textFieldSearch;
    private JComboBox comboBoxSortCriteria;
    private JButton buttonSort;
    private JList<Paper> paperList;
    private JButton buttonViewFile;
    private JButton buttonMoveFile;
    private JButton buttonSaveJson;
    private JButton buttonLoadJson;
    private JButton buttonShowKeywords;
    private JButton buttonGroupByKeyword;
    private JTextField textFieldKeywords;
    private DefaultListModel<Paper> listModel;


    private List<Paper> papers;

    public PaperDialog() {
        papers = new ArrayList<>();
        listModel = new DefaultListModel<>();
        paperList.setModel(listModel);

        //comboBoxCategory.setModel(new DefaultComboBoxModel<>(Paper.Category.values()));

        for (Paper.Category category : Paper.Category.values()) {
            comboBoxCategory.addItem(category);
        }

        // Инициализация критериев поиска
        comboBoxSearchCriteria.addItem("Автор");
        comboBoxSearchCriteria.addItem("Название");
        comboBoxSearchCriteria.addItem("Год");
        comboBoxSearchCriteria.addItem("Рубрика");

        // Инициализация критериев сортировки
        comboBoxSortCriteria.addItem("Автор");
        comboBoxSortCriteria.addItem("Название");
        comboBoxSortCriteria.addItem("Год");
        comboBoxSortCriteria.addItem("Рубрика");

        // Добавление новой статьи
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPaper();
            }
        });

        // Редактирование статьи
        buttonEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editPaper();
            }
        });

        // Удаление выбранной статьи
        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePaper();
            }
        });

        // Поиск статей
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPapers();
            }
        });

        // Сортировка статей
        buttonSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortPapers();
            }
        });

        // Детали о статье (с путем к файлу)
        paperList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Paper selectedPaper = paperList.getSelectedValue();
                if (selectedPaper != null) {
                    JOptionPane.showMessageDialog(this, "Article: " + selectedPaper.toString());
                }
            }
        });

        // Вызываем метод для просмотра файла
        buttonViewFile.addActionListener(e -> {
            Paper selectedPaper = paperList.getSelectedValue();
            if (selectedPaper != null) {
                viewFile(selectedPaper);
            } else {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите статью для просмотра.");
            }
        });

        // Открываем JFileChooser для выбора новой директории
        buttonMoveFile.addActionListener(e -> {
            Paper selectedPaper = paperList.getSelectedValue();
            if (selectedPaper != null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Выберите новую папку для перемещения файла");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File newDirectory = fileChooser.getSelectedFile();
                    moveFile(selectedPaper, newDirectory.getPath()); // Передаем путь к новой директории
                }
            } else {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите статью для перемещения файла.");
            }
        });

        // Добавим действия для кнопок
        buttonSaveJson.addActionListener(e -> saveToJson("papers.json"));
        buttonLoadJson.addActionListener(e -> loadFromJson("papers.json"));
        buttonShowKeywords.addActionListener(e -> {
            Set<String> uniqueKeywords = getUniqueKeywords();
            JOptionPane.showMessageDialog(this, "Уникальные ключевые слова: " + uniqueKeywords);
        });
        buttonGroupByKeyword.addActionListener(e -> {
            Map<String, List<Paper>> groupedPapers = groupByKeyword();
            groupedPapers.forEach((keyword, papers) -> {
                JOptionPane.showMessageDialog(this, "Статьи для ключевого слова \"" + keyword + "\": " + papers);
            });
        });

        setContentPane(contentPane);
        setModal(true);
        //setTitle("Каталог научных статей");
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void addPaper() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.isFile()) {
                String filePath = selectedFile.getAbsolutePath();
                String author = textFieldAuthor.getText();
                String title = textFieldTitle.getText();
                int year = Integer.parseInt(textFieldYear.getText());
                String month = textFieldMonth.getText();
                Paper.Category category = Paper.Category.valueOf(comboBoxCategory.getSelectedItem().toString());

                // Получаем ключевые слова из текстового поля
                String keywordsInput = textFieldKeywords.getText(); // Получение текста из поля
                List<String> keywords = new ArrayList<>();
                if (!keywordsInput.trim().isEmpty()) {
                    String[] keywordArray = keywordsInput.split(",");
                    for (String keyword : keywordArray) {
                        keywords.add(keyword.trim()); // Убираем лишние пробелы
                    }
                }

                // Создаем объект Paper с ключевыми словами
                Paper paper = new Paper(author, title, year, month, category, filePath, keywords);
                papers.add(paper);
                listModel.addElement(paper);
            } else {
                JOptionPane.showMessageDialog(this, "Выберите файл, а не директорию.");
            }
        }
    }



    private void searchPapers() {
        String searchQuery = textFieldSearch.getText();
        String searchCriteria = comboBoxSearchCriteria.getSelectedItem().toString();

        listModel.clear(); // Очищаем список перед выводом результатов поиска

        for (Paper paper : papers) {
            if (searchCriteria.equals("Автор") && paper.getAuthor().equalsIgnoreCase(searchQuery)) {
                listModel.addElement(paper);
            } else if (searchCriteria.equals("Название") && paper.getTitle().equalsIgnoreCase(searchQuery)) {
                listModel.addElement(paper);
            } else if (searchCriteria.equals("Год") && String.valueOf(paper.getYear()).equals(searchQuery)) {
                listModel.addElement(paper);
            } else if (searchCriteria.equals("Рубрика") && paper.getCategory().toString().equalsIgnoreCase(searchQuery)) {
                listModel.addElement(paper);
            }
        }
    }

    private void sortPapers() {
        String sortCriteria = comboBoxSortCriteria.getSelectedItem().toString();

        Comparator<Paper> comparator = null;
        if (sortCriteria.equals("Автор")) {
            comparator = Comparator.comparing(Paper::getAuthor);
        } else if (sortCriteria.equals("Название")) {
            comparator = Comparator.comparing(Paper::getTitle);
        } else if (sortCriteria.equals("Год")) {
            comparator = Comparator.comparingInt(Paper::getYear);
        } else if (sortCriteria.equals("Рубрика")) {
            comparator = Comparator.comparing(Paper::getCategory);
        }

        if (comparator != null) {
            papers.sort(comparator);
            listModel.clear();
            for (Paper paper : papers) {
                listModel.addElement(paper);
            }
        }
    }

    private boolean isEditing = false; // Флаг для отслеживания режима редактирования
    private int selectedIndex = -1;    // Индекс выбранной статьи

    private void editPaper() {
        if (!isEditing) {
            // Входим в режим редактирования
            selectedIndex = paperList.getSelectedIndex();

            if (selectedIndex != -1) { // Если статья выбрана
                Paper selectedPaper = listModel.getElementAt(selectedIndex);

                // Заполняем поля текущими значениями статьи
                textFieldAuthor.setText(selectedPaper.getAuthor());
                textFieldTitle.setText(selectedPaper.getTitle());
                textFieldYear.setText(String.valueOf(selectedPaper.getYear()));
                textFieldMonth.setText(selectedPaper.getMonth());
                comboBoxCategory.setSelectedItem(selectedPaper.getCategory());

                // Заполняем текстовое поле для ключевых слов, соединяя их запятыми
                textFieldKeywords.setText(String.join(", ", selectedPaper.getKeywords()));

                // Изменяем текст кнопки на "Сохранить изменения"
                buttonEdit.setText("Сохранить изменения");
                buttonAdd.setEnabled(false); // Отключаем кнопку "Добавить"

                isEditing = true; // Устанавливаем флаг редактирования
            } else {
                JOptionPane.showMessageDialog(this, "Пожалуйста, выберите статью для редактирования.");
            }
        } else {
            // Сохраняем изменения
            try {
                if (selectedIndex != -1) {
                    Paper selectedPaper = listModel.getElementAt(selectedIndex);

                    // Обновляем выбранную статью новыми значениями из полей
                    selectedPaper.setAuthor(textFieldAuthor.getText());
                    selectedPaper.setTitle(textFieldTitle.getText());
                    selectedPaper.setYear(Integer.parseInt(textFieldYear.getText()));
                    selectedPaper.setMonth(textFieldMonth.getText());
                    selectedPaper.setCategory((Paper.Category) comboBoxCategory.getSelectedItem());

                    // Получаем и устанавливаем измененные ключевые слова
                    String keywordsInput = textFieldKeywords.getText();
                    List<String> keywords = new ArrayList<>();
                    if (!keywordsInput.trim().isEmpty()) {
                        String[] keywordArray = keywordsInput.split(",");
                        for (String keyword : keywordArray) {
                            keywords.add(keyword.trim());
                        }
                    }
                    selectedPaper.setKeywords(keywords);

                    // Обновляем элемент в списке
                    listModel.setElementAt(selectedPaper, selectedIndex);

                    // Возвращаем интерфейс в режим добавления
                    buttonEdit.setText("Редактировать статью");
                    buttonAdd.setEnabled(true); // Включаем кнопку "Добавить"
                    clearFields(); // Очищаем поля

                    isEditing = false; // Выходим из режима редактирования
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Неправильный формат данных. Пожалуйста, проверьте введенные значения.");
            }
        }
    }


    private void clearFields() {
        textFieldAuthor.setText("");
        textFieldTitle.setText("");
        textFieldYear.setText("");
        textFieldMonth.setText("");
        comboBoxCategory.setSelectedIndex(0);
        textFieldKeywords.setText("");// Сбрасываем категорию на первую
    }

    private void viewFile(Paper paper) {
        try {
            File file = new File(paper.getFilePath());
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "Файл не найден.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void moveFile(Paper paper, String newDirPath) {
        try {
            File oldFile = new File(paper.getFilePath());
            // Получаем имя файла из старого пути
            String fileName = oldFile.getName(); // Получаем только имя файла
            File newFile = new File(newDirPath, fileName); // Создаем новый путь с новым директорией и старым именем

            // Проверяем, существует ли новая директория
            if (!new File(newDirPath).exists()) {
                JOptionPane.showMessageDialog(this, "Новая директория не существует.");
                return;
            }

            // Перемещаем файл
            Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            paper.setFilePath(newFile.getPath()); // Обновляем путь в объекте Paper
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при перемещении файла: " + e.getMessage());
        }
    }
    private void deletePaper() {
        int selectedIndex = paperList.getSelectedIndex();
        if (selectedIndex != -1) {
            Paper paper = listModel.getElementAt(selectedIndex);
            File file = new File(paper.getFilePath());
            if (file.exists()) {
                file.delete(); // Удалить физический файл
            }
            listModel.remove(selectedIndex); // Удалить из списка
            papers.remove(selectedIndex);    // Удалить из коллекции
        }
    }

    // Сохранение данных в JSON-файл
    private void saveToJson(String fileName) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(papers, writer);
            JOptionPane.showMessageDialog(this, "Данные успешно сохранены в JSON файл.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Загрузка данных из JSON-файла
    private void loadFromJson(String fileName) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(fileName)) {
            papers = gson.fromJson(reader, new TypeToken<List<Paper>>() {}.getType());
            listModel.clear();
            papers.forEach(listModel::addElement);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Получение уникальных ключевых слов
    private Set<String> getUniqueKeywords() {
        return papers.stream()
                .flatMap(paper -> paper.getKeywords().stream())
                .collect(Collectors.toSet());
    }

    // Получение статей, сгруппированных по ключевым словам
    private Map<String, List<Paper>> groupByKeyword() {
        return papers.stream()
                .flatMap(paper -> paper.getKeywords().stream()
                        .map(keyword -> Map.entry(keyword, paper)))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public static void main(String[] args) {
        PaperDialog dialog = new PaperDialog();
        dialog.setVisible(true);
    }
}
