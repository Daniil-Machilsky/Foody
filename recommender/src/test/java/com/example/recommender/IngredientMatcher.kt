package com.example.recommender

import com.example.recommender.implementations.ingredientMatcher.IngredientMatcherImpl
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class IngredientMatcherTest {

    private val matcher = IngredientMatcherImpl()

    private fun checkCase(a: String, b: String, answer: Boolean) {
        assertEquals(answer, matcher.equal(a, b))
    }

    @Test
    fun test1() {
        checkCase("чёрный перец", "сладкий перец", false)
    }

    @Test
    fun test2() {
        checkCase("черный перец", "сладкий перец", false)
    }

    @Test
    fun test3() {
        checkCase("квашеная капуста", "свежая капуста", false)
    }

    @Test
    fun test4() {
        checkCase("жареная картошка", "вареная картошка", false)
    }

    @Test
    fun test5() {
        checkCase("томатная паста", "паста из помидоров", true)
    }

    @Test
    fun test6() {
        checkCase("красное яблоко", "зелёное яблоко", true)
    }

    @Test
    fun test7() {
        checkCase("овсяное молоко", "миндальное молоко", true)
    }

    @Test
    fun test8() {
        checkCase("коровье молоко", "миндальное молоко", true)
    }

    @Test
    fun test9() {
        checkCase("овсяное молоко", "коровье молоко", true)
    }

    @Test
    fun test10() {
        checkCase("яблоки", "сушёные яблоки", false)
    }

    @Test
    fun test11() {
        checkCase("яблоко", "красные яблоки", true)
    }

    @Test
    fun test12() {
        checkCase("яблоки зелёные", "красные яблоки", true)
    }

    @Test
    fun test13() {
        checkCase("морковь", "тёртая морковка", true)
    }

    @Test
    fun test14() {
        checkCase("свёкла", "свекла", true)
    }

    @Test
    fun test15() {
        checkCase("картошка", "картошечка", true)
    }

    @Test
    fun test16() {
        checkCase("картофель", "варёный картофель", false)
    }

    @Test
    fun test17() {
        checkCase("варёная картошка", "варёный картофель", true)
    }

    @Test
    fun test18() {
        checkCase("картошка", "картофель", true)
    }

    @Test
    fun test19() {
        checkCase("малина", "малинка", true)
    }

    @Test
    fun test20() {
        checkCase("яблочный сок", "апельсиновый сок", false)
    }

    @Test
    fun test21() {
        checkCase("яблочный сок", "сок из яблок", true)
    }

    @Test
    fun test22() {
        checkCase("морковь", "морковный сок", false)
    }

    @Test
    fun test23() {
        checkCase("малиновый сок", "морковный сок", false)
    }

    @Test
    fun test24() {
        checkCase("овсяная крупа", "гречневая крупа", false)
    }

    @Test
    fun test25() {
        checkCase("рисовая мука", "кукурузная мука", false)
    }

    @Test
    fun test26() {
        checkCase("гречка", "гречневая крупа", true)
    }

    @Test
    fun test27() {
        checkCase("крахмал", "кукурузный крахмал", true)
    }

    @Test
    fun test28() {
        checkCase("мороженое", "мороженая рыба", false)
    }

    @Test
    fun test29() {
        checkCase("сахар", "сахарный песок", true)
    }

    @Test
    fun test30() {
        checkCase("перловка", "перловая крупа", true)
    }

    @Test
    fun test31() {
        checkCase("перловка", "гречневая крупа", false)
    }

    @Test
    fun test32() {
        checkCase("молоко 2% жирности", "молоко", true)
    }

    @Test
    fun test33() {
        checkCase("перловка", "ячменная крупа", false)
    }

    @Test
    fun test34() {
        checkCase("перловая крупа", "ячменная крупа", false)
    }

    @Test
    fun test35() {
        checkCase("яблоки фуджи", "яблоки голден", true)
    }

    @Test
    fun test36() {
        checkCase("яблоки фуджи", "яблоки", true)
    }

    @Test
    fun test37() {
        checkCase("морковь", "мини-морковь", true)
    }

    @Test
    fun test38() {
        checkCase("листья сельдерея", "листья щавеля", false)
    }

    @Test
    fun test39() {
        checkCase("щавель", "листья щавеля", true)
    }

    @Test
    fun test40() {
        checkCase("куриное мясо", "курица", true)
    }
}