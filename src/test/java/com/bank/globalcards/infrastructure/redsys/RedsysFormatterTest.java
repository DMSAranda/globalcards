package com.bank.globalcards.infrastructure.redsys;

import com.bank.globalcards.domain.models.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedsysFormatterTest {

    private RedsysFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new RedsysFormatter();
    }

    @Test
    void shouldFormatCardCorrectlyWithZeroPadding() {

        Card card = new Card();
        card.setPan("1234");
        card.setHolder("5678");
        card.setCardId("99");

        String result = formatter.format(card);

        assertThat(result)
                .isEqualTo("000000000000123400000000000000005678000000000099978");
    }

    @Test
    void shouldKeepExactLengthWhenValuesMatchFieldSize() {

        Card card = new Card();
        card.setPan("1234567890123456");   // 16
        card.setHolder("12345678901234567890"); // 20
        card.setCardId("123456789012"); // 12

        String result = formatter.format(card);

        assertThat(result)
                .isEqualTo("123456789012345612345678901234567890123456789012978");
    }

    @Test
    void shouldTruncateValuesWhenTooLong() {

        Card card = new Card();
        card.setPan("12345678901234567890");
        card.setHolder("1234567890123456789012345");
        card.setCardId("123456789012345");

        String result = formatter.format(card);

        assertThat(result.substring(0, 16))
                .isEqualTo("1234567890123456");

        assertThat(result.substring(16, 36))
                .isEqualTo("12345678901234567890");

        assertThat(result.substring(36, 48))
                .isEqualTo("123456789012");
    }

    @Test
    void shouldHandleNullValues() {

        Card card = new Card();
        card.setPan(null);
        card.setHolder(null);
        card.setCardId(null);

        String result = formatter.format(card);

        assertThat(result)
                .isEqualTo("000000000000000000000000000000000000000000000000978");
    }

    @Test
    void shouldProduceFixedLengthRecord() {

        Card card = new Card();
        card.setPan("1");
        card.setHolder("2");
        card.setCardId("3");

        String result = formatter.format(card);

        assertThat(result.length()).isEqualTo(51);
    }
}