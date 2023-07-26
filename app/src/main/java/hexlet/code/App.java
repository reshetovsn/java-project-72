package hexlet.code;

import hexlet.code.controllers.RootController;
import io.javalin.Javalin;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.valueOf(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging(); // Включаем логгирование
        });

        addRoutes(app);

        // Обработчик before запускается перед каждым запросом. Устанавливаем атрибут ctx для запросов
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}