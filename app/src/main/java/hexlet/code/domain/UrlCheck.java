package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.GenerationType;

import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private int statusCode;
    private String title;
    private String h1;
    @Lob
    private String description;
    @WhenCreated
    private Instant createdAt;
    @ManyToOne
    private Url url;

    public long getId() {
        return id;
    }

    public UrlCheck(int statusCode, String title, String h1, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
