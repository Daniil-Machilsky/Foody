package com.example.recommender.implementations.ingredientMatcher

import javax.inject.Inject

class IngredientMatcherImpl @Inject constructor(): IngredientMatcher {
    /**
     * Основная функция сравнения двух ингредиентов
     * @param ingredient1 первый ингредиент
     * @param ingredient2 второй ингредиент
     * @return true если ингредиенты считаются одинаковыми, false иначе
     */
    override fun equal(ingredient1: String, ingredient2: String): Boolean {
        val formattedIngr1 = formatIngredient(ingredient1)
        val formattedIngr2 = formatIngredient(ingredient2)

        var a = formattedIngr1.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        var b = formattedIngr2.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        // Гарантируем, что a - меньший список
        if (a.size > b.size) {
            a = b.also { b = a }
        }

        val (aWords, bWords, unused) = matchWords(a, b)

        val match = aWords.zip(bWords).map { (w1, w2) ->
            val d = dist(w1, w2)
            val n = maxOf(w1.length, w2.length)
            val same = d.toDouble() / n <= 0.5 && d < 5
            Triple(what(w1), what(w2), same)
        }

        val noMatch = unused.map { what(it) }

        var penalty = 0
        for ((x, y, same) in match) {
            when {
                (x == WordType.NOUN || y == WordType.NOUN) && !same -> penalty += 5
                (x == WordType.CHARACTERISTIC || y == WordType.CHARACTERISTIC) && !same -> penalty += 4
                x == WordType.ADJECTIVE && y == WordType.ADJECTIVE && !same -> penalty += 1
            }
        }

        for (elem in noMatch) {
            when (elem) {
                WordType.NOUN -> penalty += 5
                WordType.CHARACTERISTIC -> penalty += 4
                else -> {}
            }
        }

        return penalty < 4
    }

    /**
     * Вычисляет расстояние Левенштейна между двумя строками и возвращает индексы редактирования
     */
    private fun levenshteinIndices(s1: String, s2: String): List<Int> {
        val rows = s1.length + 1
        val cols = s2.length + 1

        val dist = Array(rows) { IntArray(cols) }

        for (i in 1 until rows) {
            dist[i][0] = i
        }

        for (j in 1 until cols) {
            dist[0][j] = j
        }

        for (i in 1 until rows) {
            for (j in 1 until cols) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1

                dist[i][j] = minOf(
                    dist[i - 1][j] + 1,
                    dist[i][j - 1] + 1,
                    dist[i - 1][j - 1] + cost
                )
            }
        }

        // Восстановление пути
        val indices = mutableListOf<Int>()
        var i = s1.length
        var j = s2.length

        while (i > 0 || j > 0) {
            when {
                // Совпадение
                i > 0 && j > 0 && s1[i - 1] == s2[j - 1] && dist[i][j] == dist[i - 1][j - 1] -> {
                    i--
                    j--
                }
                // Замена
                i > 0 && j > 0 && dist[i][j] == dist[i - 1][j - 1] + 1 -> {
                    i--
                    j--
                    indices.add(i)
                }
                // Удаление
                i > 0 && dist[i][j] == dist[i - 1][j] + 1 -> {
                    i--
                    indices.add(i)
                }
                // Вставка
                j > 0 -> {
                    indices.add(i)
                    j--
                }
            }
        }

        return indices.reversed()
    }

    /**
     * Вычисляет взвешенное расстояние между двумя строками
     */
    private fun dist(a: String, b: String): Int {
        val indices = levenshteinIndices(a, b)
        var result = 0
        val n = minOf(a.length, b.length)

        for (index in indices) {
            result += when {
                index * 3 <= n -> 5
                index * 2 <= n -> 2
                else -> 1
            }
        }

        return result
    }

    /**
     * Проверяет, является ли слово предлогом
     */
    private fun isPreposition(s: String): Boolean = s in prepositions

    /**
     * Проверяет, является ли слово прилагательным
     */
    private fun isAdjective(s: String): Boolean {
        if (s == "козье" || s == "коровье") return true
        return endings.any { s.endsWith(it) }
    }

    /**
     * Проверяет, является ли слово характеристикой
     */
    private fun isCharacteristic(s: String): Boolean {
        if (!isAdjective(s)) return false
        return characteristics.any { s.startsWith(it) }
    }

    /**
     * Определяет тип слова
     */
    private fun what(w: String): WordType = when {
        isPreposition(w) -> WordType.PREPOSITION
        isCharacteristic(w) -> WordType.CHARACTERISTIC
        isAdjective(w) -> WordType.ADJECTIVE
        else -> WordType.NOUN
    }

    /**
     * Сопоставляет слова из двух списков с использованием алгоритма венгерского метода
     * Возвращает тройку: сопоставленные слова из первого списка, сопоставленные слова из второго списка, неиспользованные слова
     */
    private fun matchWords(a: List<String>, b: List<String>): Triple<List<String>, List<String>, List<String>> {
        val (shorter, longer) = if (a.size > b.size) b to a else a to b

        val n = shorter.size
        val m = longer.size

        if (n == 0) {
            return Triple(emptyList(), emptyList(), longer)
        }

        // Создаем матрицу стоимостей
        val cost = Array(n) { i ->
            IntArray(m) { j ->
                dist(shorter[i], longer[j])
            }
        }

        // Венгерский алгоритм для оптимального сопоставления
        val assignment = hungarianAlgorithm(cost)

        val shorterIndices = assignment.map { it.first }
        val longerIndices = assignment.map { it.second }

        val unusedWords = longer.filterIndexed { index, _ -> index !in longerIndices }
        val shorterWords = shorterIndices.map { shorter[it] }
        val longerWords = longerIndices.map { longer[it] }

        return Triple(shorterWords, longerWords, unusedWords)
    }

    /**
     * Реализация венгерского алгоритма для решения задачи о назначениях
     * Возвращает список пар (row, col) оптимального назначения
     */
    private fun hungarianAlgorithm(cost: Array<IntArray>): List<Pair<Int, Int>> {
        val n = cost.size
        val m = cost[0].size

        // Для простоты используем жадный подход с улучшением
        // В продакшене лучше использовать полноценную библиотеку или более оптимизированную реализацию

        val rowUsed = BooleanArray(n)
        val colUsed = BooleanArray(m)
        val result = mutableListOf<Pair<Int, Int>>()

        // Сортируем все возможные пары по стоимости
        val allPairs = mutableListOf<Triple<Int, Int, Int>>()
        for (i in 0 until n) {
            for (j in 0 until m) {
                allPairs.add(Triple(cost[i][j], i, j))
            }
        }
        allPairs.sortBy { it.first }

        // Жадно выбираем наилучшие пары
        for ((_, i, j) in allPairs) {
            if (!rowUsed[i] && !colUsed[j]) {
                result.add(Pair(i, j))
                rowUsed[i] = true
                colUsed[j] = true
            }
        }

        return result
    }

    /**
     * Форматирует ингредиент: удаляет лишние символы, нормализует текст
     */
    private fun formatIngredient(a: String): String {
        val result = a
            .replace('-', ' ')
            .replace(Regex("\\([^()]*\\)"), "")  // Удаляем текст в скобках
            .replace(Regex("[^a-zA-Zа-яА-ЯёЁ\\s]"), "")  // Оставляем только буквы и пробелы
            .trim()
            .lowercase()
            .replace('ё', 'е')
            .replace("томат", "помидор")
            .replace("кукуруз", "руз")

        // Разбиваем на слова и удаляем запрещенные
        val words = result.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val filtered = words.filterNot { it in forbidden }

        return filtered.joinToString(" ")
    }

    companion object {
        /**
         * Функция для сравнения ингредиентов с использованием нечеткого сопоставления
         */

        // Предлоги для игнорирования при сравнении
        private val prepositions = setOf("для", "из", "в", "с")

        // Окончания прилагательных
        private val endings = listOf("ое", "ые", "ой", "ая", "ие", "ее", "яя", "ый", "ий")

        // Характеристики продуктов (способы приготовления, типы)
        private val characteristics = listOf(
            "жарен", "варен", "тушен", "парен", "грецк", "кунжут", "кокос",
            "маринован", "сушен", "курин", "томатн", "яблочн", "плавлен",
            "лимон", "абрикосов", "кокосов", "апельсин", "мангов", "медов", "молочн", "безлактоз",
            "сливочн", "оливков", "свеж", "квашен", "черн", "грибн",
            "помидорн", "сух", "чесночн", "луков", "кабочков", "тыквен",
            "малинов", "морковн", "огуречн", "бананов", "гречнев", "ячмен", "перлов", "рисова"
        )

        // Запрещенные слова, которые не влияют на сравнение
        private val forbidden = setOf(
            "песок", "крупы", "крупа", "зёрна", "каша", "песок", "быстрого", "приготовления",
            "жирности", "листья", "мини", "большой", "молодой", "мясо", "фуджи", "голден"
        )
    }
}

// Типы слов для классификации
private enum class WordType {
    PREPOSITION,
    ADJECTIVE,
    NOUN,
    CHARACTERISTIC
}