package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {

    public static Handler addUrl = ctx -> {
        // Получаем переменную часть пути, из которого будем извлекать url в нужном нам виде
        String inputUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);

        // Парсинг url в вид https://example.com
        URL newUrl;
        try {
            newUrl = new URL(inputUrl);
            String parsedUrl = String
                    .format(
                            "%s://%s%s",
                            newUrl.getProtocol(),
                            newUrl.getHost(),
                            newUrl.getPort() == -1 ? "" : ":" + newUrl.getPort()
                    )
                    .toLowerCase();

            Url foundUrl = new QUrl()
                    .name.equalTo(parsedUrl)
                    .findOne();

            if (foundUrl == null) {
                Url url = new Url(parsedUrl);
                url.save();
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
                ctx.redirect("/urls");
            } else {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");
            }

        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
        }
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy()
                .id.asc()
                .findList();

        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        try {
            assert url != null;
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            String body = response.getBody();
            Document doc = Jsoup.parse(body);

            int statusCode = response.getStatus();
            String title = doc.title();

            Element tagH1 = doc.selectFirst("h1");
            String h1 = tagH1 == null ? "" : tagH1.text();

            Element tagMetaDescription = doc.selectFirst("meta[name=description]");
            String description = tagMetaDescription == null ? "" : tagMetaDescription.attr("content");

            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description);
            url.getUrlChecks().add(urlCheck);
            url.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };
}
