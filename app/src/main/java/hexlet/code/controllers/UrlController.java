package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlController {

    public static Handler addUrl = ctx -> {
        // Получаем переменную часть пути, из которого будем извлекать url в нужном нам виде
        String inputUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);
        // Парсинг url в виде https://example.com
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

        Url url = new QUrl()
                .name.iequalTo(finalUrl)
                .findOne();

        if (url != null) {
            url.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.redirect("/urls");
        } else {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }
    };
}
