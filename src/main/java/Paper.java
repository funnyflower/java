import java.util.List;

public class Paper {
    private String author;
    private String title;
    private int year;
    private String month;
    private Category category;
    private String filePath;
    private List<String> keywords; // добавим поле для ключевых слов

    // Перечисление для рубрик
    //public enum Category {
    //    SCIENCE, TECHNOLOGY, MEDICINE, ARTS
    //}

    public enum Category {
        MATH("MATH"),
        PHYSICS("PHYSICS"),
        CHEMISTRY("CHEMISTRY"),
        BIOLOGY("BIOLOGY"),
        COMPUTER_SCIENCE("COMPUTER_SCIENCE");

        private final String displayName;

        // Конструктор с отображаемым именем
        Category(String displayName) {
            this.displayName = displayName;
        }

        // Переопределение метода toString для правильного отображения в JComboBox
        @Override
        public String toString() {
            return displayName;
        }
    }

    // Добавляем новое поле keywords в конструктор
    public Paper(String author, String title, int year, String month, Category category, String filePath, List<String> keywords) {
        this.author = author;
        this.title = title;
        this.year = year;
        this.month = month;
        this.category = category;
        this.filePath = filePath;
        this.keywords = keywords;
    }

    // Геттеры и сеттеры для keywords
    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    // Геттеры и сеттеры
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // Add a getter for filePath
    public String getFilePath() {
        return filePath;
    }

    // Add a setter for filePath
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Автор: " + author + ", Название: " + title + ", Год: " + year + ", Месяц: " + month + ", Категория: " + category + ", Файл: " + filePath;
    }
}