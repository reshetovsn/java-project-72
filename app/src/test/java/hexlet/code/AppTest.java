package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.javalin.http.NotFoundResponse;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import io.javalin.Javalin;
import io.ebean.Database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp(); // Получаем инстанс приложения
        app.start(0); // Запускаем приложение на рандомном порту
        int port = app.port(); // Получаем порт, на которм запустилось приложение
        baseUrl = "http://localhost:" + port; // Формируем базовый URL
        database = DB.getDefault();

        mockServer = new MockWebServer();

        String testPage = Files.readString(Paths.get("src/test/resources/testPage.html"));
        mockServer.enqueue(new MockResponse().setBody(testPage));
        mockServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed.sql");
    }

    @Nested
    class RootTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl)
                    .asString();
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }

    @Nested
    class UrlTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://youtube.com");
            assertThat(body).doesNotContain("https://yandex.ru");
        }

        @Test
        void testShow() {
            String inputUrl = "https://youtube.com";

            Url actualUrl = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            if (actualUrl == null) {
                throw new NotFoundResponse();
            }

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + actualUrl.getId())
                    .asString();

            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://youtube.com");
            assertThat(body).contains("2023-07-31");
        }

        @Test
        void testPageExist() {
            String inputUrl = "https://gmail.com";

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains(inputUrl);
            assertThat(content).contains("Страница уже существует");
        }

        @Test
        void testValidPage() {
            String inputUrl = "https://ya.ru";

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains(inputUrl);
            assertThat(content).contains("Страница успешно добавлена");

            Url addedUrl = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(addedUrl).isNotNull();
            assertThat(addedUrl.getName()).isEqualTo(inputUrl);
        }

        @Test
        void testInvalidPage() {
            String inputUrl = "ya.ru";

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();

            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).doesNotContain(inputUrl);
            assertThat(content).contains("Некорректный URL");

            Url actualUrl = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(actualUrl).isNull();
        }

        @Test
        void testCheckUrl() {
            // Вызвав на созданном инстансе сервера метод mockServer.url("/").toString(),
            // можно получить адрес сайта, который нужно будет использовать в тестах
            String mockUrl = mockServer.url("/").toString().replaceAll("/$", "");

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", mockUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);

            Url url = new QUrl()
                    .name.equalTo(mockUrl)
                    .findOne();

            assertThat(url).isNotNull();
            assertThat(url.getName()).isEqualTo(mockUrl);

            HttpResponse<Empty> responseCheck = Unirest
                    .post(baseUrl + "/urls/" + url.getId() + "/checks")
                    .asEmpty();

            assertThat(responseCheck.getStatus()).isEqualTo(302);

            UrlCheck urlCheck = url.getUrlChecks().get(0);
            assertThat(urlCheck.getStatusCode()).isEqualTo(200);
            assertThat(urlCheck.getTitle()).isEqualTo("title example");
            assertThat(urlCheck.getH1()).isEqualTo("header example");
            assertThat(urlCheck.getDescription()).isEqualTo("some description");

            // Проверка, что в БД данные записались
            UrlCheck actualCheckUrl = new QUrlCheck()
                    .url.urlChecks.equalTo(urlCheck)
                    .orderBy()
                    .createdAt.desc()
                    .findOne();

            assertThat(actualCheckUrl).isNotNull();
            assertThat(actualCheckUrl.getTitle()).isEqualTo("title example");
            assertThat(actualCheckUrl.getH1()).isEqualTo("header example");
            assertThat(actualCheckUrl.getDescription()).isEqualTo("some description");
        }
    }
}
