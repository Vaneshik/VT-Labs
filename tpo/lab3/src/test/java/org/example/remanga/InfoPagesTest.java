package org.example.remanga;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InfoPagesTest extends BaseTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "/news, Новости сайта",
            "/about-us, О НАС",
            "/faq, Вопросы и Ответы",
            "/rules, Правила сайта",
            "/mobile-app, FAQ по официальному приложению",
            "/advertisers, Для рекламодателей",
            "/vacancy, Вакансии",
            "/dmca, DMCA",
            "/copyright, Правообладателям",
            "/agency-contract, Агентский Договор",
            "/confidentiality-agreement, Соглашение конфиденциальности",
            "/terms-of-use, Пользовательское соглашение",
            "/cookie-policy, Политика использования файлов cookie",
            "/personal-data-processing, Согласие на обработку персональных данных"
    })
    void user_opens_public_information_page(String path, String expectedText) {
        assertTrue(infoPage().open(path).contains(expectedText),
                "Информационная страница должна содержать ожидаемый заголовок или ключевой текст");
    }
}
