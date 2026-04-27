package com.edumonitor.app.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.edumonitor.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private TextView tvQuestion, tvProgress, tvSubject;
    private RadioGroup radioGroup;
    private RadioButton[] radioButtons = new RadioButton[4];
    private ProgressBar progressBar;
    private Button btnNext;

    private List<Map<String, Object>> questions = new ArrayList<>();
    private List<Integer> userAnswers = new ArrayList<>();
    
    private int currentIndex = 0;
    private int correctCount = 0;
    private int selectedAnswer = -1;

    private String subjectId, subjectName, grade;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        subjectId = getIntent().getStringExtra("subjectId");
        subjectName = getIntent().getStringExtra("subjectName");
        grade = getIntent().getStringExtra("grade");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(subjectName);

        tvQuestion = findViewById(R.id.tv_question);
        tvProgress = findViewById(R.id.tv_progress);
        tvSubject = findViewById(R.id.tv_subject);
        radioGroup = findViewById(R.id.radio_group);
        progressBar = findViewById(R.id.progress_bar);
        btnNext = findViewById(R.id.btn_next);

        radioButtons[0] = findViewById(R.id.rb_option_0);
        radioButtons[1] = findViewById(R.id.rb_option_1);
        radioButtons[2] = findViewById(R.id.rb_option_2);
        radioButtons[3] = findViewById(R.id.rb_option_3);

        tvSubject.setText(subjectName + " (" + grade + " класс)");
        
        loadTest();

        btnNext.setOnClickListener(v -> handleNext());
    }

    private void loadTest() {
        // Generate mock test based on grade and subject
        questions = generateMockTest(subjectId, grade);
        userAnswers.clear();
        
        if (questions.isEmpty()) {
            Toast.makeText(this, "Тест для этого класса пока не готов", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showQuestion();
    }

    private Map<String, Object> createQ(String text, List<String> options, int correctIndex) {
        Map<String, Object> q = new HashMap<>();
        q.put("text", text);
        q.put("options", options);
        q.put("correctIndex", correctIndex);
        return q;
    }

    private List<Map<String, Object>> generateMockTest(String subjectId, String grade) {
        List<Map<String, Object>> list = new ArrayList<>();
        int gradeNum = 7;
        try {
            gradeNum = Integer.parseInt(grade);
        } catch (Exception ignored) {}

        if (subjectId.equals("math")) {
            if (gradeNum <= 4) {
                list.add(createQ("Сколько будет 2 + 2?", Arrays.asList("3", "4", "5", "6"), 1));
                list.add(createQ("У Пети было 5 яблок, он отдал 2. Сколько осталось?", Arrays.asList("2", "3", "4", "5"), 1));
                list.add(createQ("Сколько будет 5 * 5?", Arrays.asList("15", "20", "25", "30"), 2));
                list.add(createQ("Сколько минут в одном часе?", Arrays.asList("50", "60", "100", "120"), 1));
                list.add(createQ("Как называется фигура с тремя углами?", Arrays.asList("Квадрат", "Круг", "Треугольник", "Ромб"), 2));
                list.add(createQ("Сколько будет 10 - 3?", Arrays.asList("6", "7", "8", "9"), 1));
                list.add(createQ("Сколько дней в неделе?", Arrays.asList("5", "6", "7", "8"), 2));
                list.add(createQ("Что больше: 50 или 15?", Arrays.asList("15", "Одинаково", "50", "Не знаю"), 2));
                list.add(createQ("Сколько будет 2 * 3?", Arrays.asList("4", "5", "6", "8"), 2));
                list.add(createQ("Как называется фигура без углов?", Arrays.asList("Треугольник", "Квадрат", "Ромб", "Круг"), 3));
            } else if (gradeNum <= 8) {
                list.add(createQ("Решите уравнение: 2x = 10", Arrays.asList("x = 2", "x = 4", "x = 5", "x = 10"), 2));
                list.add(createQ("Чему равна площадь квадрата со стороной 4?", Arrays.asList("8", "12", "16", "20"), 2));
                list.add(createQ("Сколько градусов в прямом угле?", Arrays.asList("45", "90", "180", "360"), 1));
                list.add(createQ("Вычислите: -5 + 8", Arrays.asList("-3", "3", "13", "-13"), 1));
                list.add(createQ("Что такое периметр?", Arrays.asList("Площадь", "Объем", "Сумма длин всех сторон", "Угол"), 2));
                list.add(createQ("Вычислите: 3³", Arrays.asList("9", "18", "27", "81"), 2));
                list.add(createQ("Решите: 15 - 3x = 0", Arrays.asList("x = 3", "x = 5", "x = 15", "x = -5"), 1));
                list.add(createQ("Как называется отрезок, соединяющий центр круга с его краем?", Arrays.asList("Диаметр", "Хорда", "Радиус", "Катет"), 2));
                list.add(createQ("Найдите 20% от 100", Arrays.asList("10", "20", "50", "80"), 1));
                list.add(createQ("Какая дробь больше: 1/2 или 1/3?", Arrays.asList("1/3", "1/2", "Равны", "Зависит от числа"), 1));
            } else {
                list.add(createQ("Чему равен sin(90°)?", Arrays.asList("0", "0.5", "1", "-1"), 2));
                list.add(createQ("Решите: x² - 4 = 0", Arrays.asList("x = 2", "x = -2", "x = ±2", "x = 4"), 2));
                list.add(createQ("Что такое логарифм?", Arrays.asList("Показатель степени", "Корень", "Дробь", "Производная"), 0));
                list.add(createQ("Чему равна производная функции f(x) = x²?", Arrays.asList("x", "2x", "x³", "2"), 1));
                list.add(createQ("Сумма углов в треугольнике?", Arrays.asList("90°", "180°", "270°", "360°"), 1));
                list.add(createQ("Значение cos(0°)?", Arrays.asList("0", "1", "-1", "Не существует"), 1));
                list.add(createQ("Решите неравенство: x² > 0", Arrays.asList("x > 0", "x < 0", "x != 0", "x = 0"), 2));
                list.add(createQ("Как называется график функции y = x²?", Arrays.asList("Прямая", "Гипербола", "Парабола", "Окружность"), 2));
                list.add(createQ("Интеграл от 2x?", Arrays.asList("x²", "2", "x", "x³"), 0));
                list.add(createQ("Сколько корней имеет уравнение D < 0?", Arrays.asList("1", "2", "Бесконечно", "Нет действительных корней"), 3));
            }
        } else if (subjectId.equals("russian")) {
            if (gradeNum <= 4) {
                list.add(createQ("Как правильно написать: ЖИ или ШИ?", Arrays.asList("С буквой Ы", "С буквой И", "С буквой Е", "С буквой А"), 1));
                list.add(createQ("Какая буква первая в алфавите?", Arrays.asList("Б", "В", "А", "Г"), 2));
                list.add(createQ("Выберите слово, обозначающее предмет (существительное):", Arrays.asList("Бегать", "Красивый", "Стол", "Быстро"), 2));
                list.add(createQ("В каком слове есть мягкий знак?", Arrays.asList("Мяч", "Нож", "День", "Дом"), 2));
                list.add(createQ("Сколько гласных букв в слове МАМА?", Arrays.asList("1", "2", "3", "4"), 1));
                list.add(createQ("Как пишется слово (Ч/Щ)УКА?", Arrays.asList("Щука", "Чюка", "Щюка", "Чука"), 0));
                list.add(createQ("Найдите слово-действие (глагол):", Arrays.asList("Прыгать", "Красный", "Мяч", "Вчера"), 0));
                list.add(createQ("Что ставят в конце вопросительного предложения?", Arrays.asList(".", ",", "!", "?"), 3));
                list.add(createQ("Разделите слово СОБАКА на слоги:", Arrays.asList("Со-бак-а", "С-оба-ка", "Со-ба-ка", "Соб-ака"), 2));
                list.add(createQ("Какое слово пишется с большой буквы?", Arrays.asList("дерево", "москва", "река", "город"), 1));
            } else if (gradeNum <= 8) {
                list.add(createQ("Укажите часть речи слова 'Быстро'", Arrays.asList("Существительное", "Глагол", "Наречие", "Прилагательное"), 2));
                list.add(createQ("В каком слове пишется НН?", Arrays.asList("Деревя..ый", "Стекля..ый", "Оловя..ый", "Все перечисленные"), 3));
                list.add(createQ("Какое из слов является антонимом к слову 'светлый'?", Arrays.asList("Яркий", "Темный", "Чистый", "Белый"), 1));
                list.add(createQ("Где нужна запятая?", Arrays.asList("Он пришел и сел", "Когда стемнело мы ушли", "Я ем яблоко", "Кот спит"), 1));
                list.add(createQ("Определите падеж: 'думаю о маме'", Arrays.asList("Именительный", "Дательный", "Предложный", "Творительный"), 2));
                list.add(createQ("Синоним к слову 'храбрый':", Arrays.asList("Трусливый", "Смелый", "Слабый", "Глупый"), 1));
                list.add(createQ("Укажите несклоняемое существительное:", Arrays.asList("Пальто", "Стол", "Окно", "Книга"), 0));
                list.add(createQ("В каком слове есть приставка?", Arrays.asList("Дорога", "Поход", "Дом", "Кот"), 1));
                list.add(createQ("Что такое подлежащее?", Arrays.asList("Главный член предложения", "Второстепенный член", "Часть речи", "Знак препинания"), 0));
                list.add(createQ("Как пишется 'не' с глаголами?", Arrays.asList("Слитно", "Раздельно", "Через дефис", "По-разному"), 1));
            } else {
                list.add(createQ("Укажите сложносочиненное предложение:", Arrays.asList("Я пошел, потому что стемнело.", "Стемнело, и мы пошли домой.", "Когда мы пришли, было темно.", "Темнеющий лес пугал."), 1));
                list.add(createQ("В каком слове ударение падает на первый слог?", Arrays.asList("Каталог", "Звонит", "Торты", "Договор"), 2));
                list.add(createQ("Укажите вид связи в словосочетании 'быстро бежать'", Arrays.asList("Согласование", "Управление", "Примыкание", "Подчинение"), 2));
                list.add(createQ("В каком предложении есть деепричастный оборот?", Arrays.asList("Человек, читающий книгу.", "Читая книгу, он уснул.", "Книга была прочитана.", "Он прочитал книгу."), 1));
                list.add(createQ("Какое средство выразительности: 'Лес спал'?", Arrays.asList("Эпитет", "Олицетворение", "Метафора", "Сравнение"), 1));
                list.add(createQ("Где НЕ пишется слитно?", Arrays.asList("(Не)был", "(Не)правда, а ложь", "(Не)нависть", "(Не)зная"), 2));
                list.add(createQ("Какой союз является подчинительным?", Arrays.asList("И", "Но", "Чтобы", "Да"), 2));
                list.add(createQ("Что такое синекдоха?", Arrays.asList("Разновидность метафоры", "Разновидность метонимии", "Эпитет", "Олицетворение"), 1));
                list.add(createQ("В каком слове пишется буква 'Е'?", Arrays.asList("Пр..зидент", "Пр..вилегия", "Пр..оритет", "Пр..чина"), 0));
                list.add(createQ("Определите тип сказуемого: 'Он был врачом'", Arrays.asList("ПГС", "СГС", "СИС", "Нет правильного"), 2));
            }
        } else { // english
            if (gradeNum <= 4) {
                list.add(createQ("Как будет 'Собака' по-английски?", Arrays.asList("Cat", "Dog", "Bird", "Fish"), 1));
                list.add(createQ("Выберите правильный цвет 'Красный':", Arrays.asList("Blue", "Green", "Red", "Yellow"), 2));
                list.add(createQ("Как перевести 'Hello'?", Arrays.asList("Пока", "Привет", "Спасибо", "Пожалуйста"), 1));
                list.add(createQ("Один, два, ... ?", Arrays.asList("Three", "Four", "Five", "Six"), 0));
                list.add(createQ("Яблоко на английском:", Arrays.asList("Banana", "Orange", "Apple", "Grape"), 2));
                list.add(createQ("Как будет 'Мальчик'?", Arrays.asList("Girl", "Man", "Boy", "Woman"), 2));
                list.add(createQ("Переведите 'My name is':", Arrays.asList("Меня зовут", "Мой друг", "Я живу", "Мой дом"), 0));
                list.add(createQ("Какой цвет 'Yellow'?", Arrays.asList("Синий", "Желтый", "Зеленый", "Белый"), 1));
                list.add(createQ("Как сказать 'Пока'?", Arrays.asList("Hello", "Yes", "No", "Goodbye"), 3));
                list.add(createQ("Кошка на английском:", Arrays.asList("Dog", "Cat", "Mouse", "Cow"), 1));
            } else if (gradeNum <= 8) {
                list.add(createQ("Выберите правильную форму: He ___ a student.", Arrays.asList("am", "is", "are", "be"), 1));
                list.add(createQ("Прошедшее время от слова 'Go'?", Arrays.asList("Goed", "Gone", "Went", "Going"), 2));
                list.add(createQ("Вставьте артикль: She is ___ doctor.", Arrays.asList("a", "an", "the", "нет артикля"), 0));
                list.add(createQ("Как сказать 'Я люблю читать'?", Arrays.asList("I like read", "I likes reading", "I like reading", "I reading like"), 2));
                list.add(createQ("Что означает 'Always'?", Arrays.asList("Никогда", "Иногда", "Всегда", "Часто"), 2));
                list.add(createQ("Вопрос к 'They play tennis':", Arrays.asList("Do they play?", "Does they play?", "Are they play?", "Is they play?"), 0));
                list.add(createQ("Множественное число от 'Child'?", Arrays.asList("Childs", "Children", "Childrens", "Childes"), 1));
                list.add(createQ("Переведите 'Вчера':", Arrays.asList("Today", "Tomorrow", "Yesterday", "Now"), 2));
                list.add(createQ("Вставьте предлог: I go to school ___ morning.", Arrays.asList("at", "on", "in the", "for"), 2));
                list.add(createQ("Переведите 'Beautiful':", Arrays.asList("Умный", "Быстрый", "Красивый", "Сильный"), 2));
            } else {
                list.add(createQ("If I ___ you, I would study harder.", Arrays.asList("am", "was", "were", "been"), 2));
                list.add(createQ("What is the passive form of 'They built the house'?", Arrays.asList("The house was built by them.", "The house is built by them.", "The house built them.", "They were built."), 0));
                list.add(createQ("Choose the correct spelling:", Arrays.asList("Recieve", "Receive", "Receeve", "Riceive"), 1));
                list.add(createQ("I have been living here ___ 10 years.", Arrays.asList("since", "for", "during", "in"), 1));
                list.add(createQ("What does 'Piece of cake' mean?", Arrays.asList("A dessert", "Very easy", "Very hard", "A mistake"), 1));
                list.add(createQ("He said that he ___ a new car.", Arrays.asList("bought", "has bought", "had bought", "will buy"), 2));
                list.add(createQ("Which word is an adverb?", Arrays.asList("Quick", "Quickly", "Quicker", "Quickness"), 1));
                list.add(createQ("Complete: Neither John ___ Mary came.", Arrays.asList("or", "and", "nor", "but"), 2));
                list.add(createQ("A person who designs buildings is an ___", Arrays.asList("Engineer", "Architect", "Builder", "Artist"), 1));
                list.add(createQ("By this time next year, I ___ my degree.", Arrays.asList("will finish", "will have finished", "finish", "finished"), 1));
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private void showQuestion() {
        if (questions.isEmpty()) return;
        Map<String, Object> q = questions.get(currentIndex);

        tvProgress.setText((currentIndex + 1) + " / " + questions.size());
        progressBar.setMax(questions.size());
        progressBar.setProgress(currentIndex + 1);

        tvQuestion.setText((String) q.get("text"));
        List<String> opts = (List<String>) q.get("options");
        for (int i = 0; i < 4 && i < opts.size(); i++) {
            radioButtons[i].setText(opts.get(i));
            radioButtons[i].setChecked(false);
        }
        radioGroup.clearCheck();
        selectedAnswer = -1;
        btnNext.setText(currentIndex == questions.size() - 1 ? "Завершить" : "Следующий →");
    }

    private void handleNext() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (radioButtons[i].getId() == checkedId) {
                selectedAnswer = i;
                break;
            }
        }

        userAnswers.add(selectedAnswer);

        Map<String, Object> q = questions.get(currentIndex);
        // Safely parse correctIndex (handles Integer or Long)
        int correctIndex = Integer.parseInt(String.valueOf(q.get("correctIndex")));
        if (selectedAnswer == correctIndex) correctCount++;

        currentIndex++;
        if (currentIndex < questions.size()) {
            showQuestion();
        } else {
            finishTest();
        }
    }

    private void finishTest() {
        int total = questions.size();
        int score = (int) Math.round((correctCount * 100.0) / total);

        ArrayList<String> qTexts = new ArrayList<>();
        ArrayList<String> qCorrects = new ArrayList<>();
        ArrayList<String> qUserAns = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            qTexts.add((String) q.get("text"));
            
            List<String> opts = (List<String>) q.get("options");
            int correctIndex = Integer.parseInt(String.valueOf(q.get("correctIndex")));
            qCorrects.add(opts.get(correctIndex));
            
            int uAns = userAnswers.get(i);
            qUserAns.add(opts.get(uAns));
        }

        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        db.collection("users").document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    String name = userDoc.exists() && userDoc.getString("name") != null
                            ? userDoc.getString("name") : "Ученик";
                    String storedGrade = userDoc.exists() && userDoc.getString("grade") != null
                            ? userDoc.getString("grade") : grade;
                    String email = userDoc.exists() && userDoc.getString("email") != null
                            ? userDoc.getString("email") : "Нет email";

                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", uid);
                    result.put("userName", name);
                    result.put("email", email);
                    result.put("subject", subjectName);
                    result.put("grade", grade);
                    result.put("score", score);
                    result.put("correctAnswers", correctCount);
                    result.put("totalQuestions", total);
                    result.put("date", com.google.firebase.Timestamp.now());

                    db.collection("results").add(result).addOnCompleteListener(ref -> {
                        Intent intent = new Intent(this, ResultActivity.class);
                        intent.putExtra("score", score);
                        intent.putExtra("correct", correctCount);
                        intent.putExtra("total", total);
                        intent.putExtra("subjectName", subjectName);
                        
                        intent.putStringArrayListExtra("qTexts", qTexts);
                        intent.putStringArrayListExtra("qCorrects", qCorrects);
                        intent.putStringArrayListExtra("qUserAns", qUserAns);

                        startActivity(intent);
                        finish();
                    });
                });
    }
}
