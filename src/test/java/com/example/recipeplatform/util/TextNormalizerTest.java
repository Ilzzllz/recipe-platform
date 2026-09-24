package com.example.recipeplatform.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizerTest {
    @Test
    void trimsCollapsesSpacesAndCapitalizesEverySentence() {
        assertThat(TextNormalizer.normalize("  суп   готов.  подать   горячим!  "))
                .isEqualTo("Суп готов. Подать горячим!");
    }

    @Test
    void capitalizesTextAfterQuestionMark() {
        assertThat(TextNormalizer.normalize("готово? продолжить"))
                .isEqualTo("Готово? Продолжить");
    }

    @Test
    void keepsNullAndEmptyValuesSafe() {
        assertThat(TextNormalizer.normalize(null)).isNull();
        assertThat(TextNormalizer.normalize("  ")).isEmpty();
    }
}
