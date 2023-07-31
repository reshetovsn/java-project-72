package hexlet.code.controllers;

import hexlet.code.domain.Url;
import io.javalin.http.Handler;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlController {

    public static Handler addUrl = ctx -> {
        // Получаем переменную часть пути, из которого будем извлекать url
        String inputUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);

        URL newUrl;
        try {
            newUrl = new URL(inputUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        String finalUrl = newUrl.getProtocol() + "://" + newUrl.getAuthority();
        String finalUrlWithPort = newUrl.getProtocol() + "://" + newUrl.getAuthority() + ":" + newUrl.getPort();

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new Qurl()

        Url urlToDB = new Url(finalUrl);
        urlToDB.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/articles");

    };
}
