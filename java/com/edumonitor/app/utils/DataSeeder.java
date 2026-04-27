package com.edumonitor.app.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataSeeder: seeds Firestore with 3 predefined tests (one per subject)
 * on first run. Safe to call repeatedly — checks existence first.
 */
public class DataSeeder {

    public static void seedIfNeeded() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tests").get().addOnSuccessListener(snap -> {
            if (snap.isEmpty()) {
                seedMath(db);
                seedRussian(db);
                seedEnglish(db);
            }
        });
    }

    private static void seedMath(FirebaseFirestore db) {
        List<Map<String, Object>> questions = Arrays.asList(
            q("Чему равно 2 + 2 × 3?", Arrays.asList("8", "10", "12", "6"), 0),   // 8 (умножение первее)
            q("Сколько градусов в прямом угле?", Arrays.asList("90", "180", "45", "360"), 0),
            q("Чему равен квадрат числа 7?", Arrays.asList("14", "49", "42", "21"), 1),
            q("Как называется результат сложения?", Arrays.asList("Разность", "Произведение", "Сумма", "Частное"), 2),
            q("Чему равно 15 ÷ 3?", Arrays.asList("3", "4", "5", "6"), 2),
            q("Какое число является простым?", Arrays.asList("9", "15", "11", "21"), 2),
            q("Найди периметр квадрата со стороной 5 см", Arrays.asList("10 см", "15 см", "25 см", "20 см"), 3),
            q("Чему равно 3³?", Arrays.asList("9", "6", "27", "12"), 2),
            q("Как называется боковая поверхность цилиндра?", Arrays.asList("Квадрат", "Прямоугольник", "Трапеция", "Круг"), 1),
            q("Сумма углов треугольника равна", Arrays.asList("90°", "180°", "270°", "360°"), 1)
        );
        saveTest(db, "math", "Математика", questions);
    }

    private static void seedRussian(FirebaseFirestore db) {
        List<Map<String, Object>> questions = Arrays.asList(
            q("Что такое подлежащее?", Arrays.asList("Член предложения, обозначающий действие", "Главный член предложения, обозначающий предмет", "Второстепенный член предложения", "Часть речи"), 1),
            q("Какая часть речи изменяется по падежам?", Arrays.asList("Глагол", "Наречие", "Прилагательное", "Союз"), 2),
            q("Сколько падежей в русском языке?", Arrays.asList("5", "6", "7", "8"), 1),
            q("Что такое синоним?", Arrays.asList("Слово с противоположным значением", "Слово с похожим значением", "Слово с одинаковым написанием", "Однокоренное слово"), 1),
            q("Какой знак ставится в конце вопросительного предложения?", Arrays.asList(".", "!", "?", "..."), 2),
            q("Как пишется слово: бе_ный?", Arrays.asList("бедный", "безный", "бейный", "бесный"), 0),
            q("Что такое морфология?", Arrays.asList("Раздел о звуках", "Раздел о частях речи", "Раздел о предложениях", "Раздел об орфографии"), 1),
            q("Глагол «читать» в прошедшем времени — это", Arrays.asList("читаю", "читал", "читает", "прочитай"), 1),
            q("Найди предложение с однородными членами", Arrays.asList("Солнце светит ярко.", "Мальчик бежал быстро.", "Кот спит и мурлычет.", "Я пошёл домой."), 2),
            q("Что такое диалог?", Arrays.asList("Речь одного человека", "Разговор двух и более людей", "Описание природы", "Перечисление событий"), 1)
        );
        saveTest(db, "russian", "Русский язык", questions);
    }

    private static void seedEnglish(FirebaseFirestore db) {
        List<Map<String, Object>> questions = Arrays.asList(
            q("Как переводится слово «book»?", Arrays.asList("Ручка", "Книга", "Тетрадь", "Стол"), 1),
            q("Выберите правильный артикль: ___ apple", Arrays.asList("a", "an", "the", "—"), 1),
            q("Как будет «Я люблю кошек» на английском?", Arrays.asList("I love cats", "I loving cats", "I am love cats", "I loves cats"), 0),
            q("Прошедшее время глагола «go»", Arrays.asList("goed", "goes", "went", "going"), 2),
            q("Что означает «beautiful»?", Arrays.asList("Маленький", "Быстрый", "Красивый", "Умный"), 2),
            q("Как спросить о времени по-английски?", Arrays.asList("Where is the time?", "What time is it?", "What is time?", "How time is?"), 1),
            q("Множественное число слова «child»", Arrays.asList("childs", "childes", "children", "child"), 2),
            q("Выберите правильный вариант: She ___ a doctor.", Arrays.asList("am", "is", "are", "be"), 1),
            q("Переведите: «every day»", Arrays.asList("каждый день", "иногда", "вчера", "завтра"), 0),
            q("Как сказать «До свидания» по-английски?", Arrays.asList("Hello", "Thank you", "Please", "Goodbye"), 3)
        );
        saveTest(db, "english", "Английский язык", questions);
    }

    private static Map<String, Object> q(String text, List<String> options, int correctIndex) {
        Map<String, Object> q = new HashMap<>();
        q.put("text", text);
        q.put("options", options);
        q.put("correctIndex", correctIndex);
        return q;
    }

    private static void saveTest(FirebaseFirestore db, String id, String subject,
                                 List<Map<String, Object>> questions) {
        Map<String, Object> test = new HashMap<>();
        test.put("id", id);
        test.put("subject", subject);
        test.put("questions", questions);
        db.collection("tests").document(id).set(test);
    }
}
