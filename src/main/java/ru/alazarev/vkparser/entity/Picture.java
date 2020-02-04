package ru.alazarev.vkparser.entity;

/**
 * Class describes picture.
 */
public class Picture {
    private String name;
    private String http;
    private String date;

    public Picture(String name, String http, String date) {
        this.name = name;
        this.http = http;
        this.date = date.replace(":", ".");
    }

    public String getName() {
        return name;
    }

    public String getHttp() {
        return http;
    }

    private String getJpgName() {
        return this.http.substring(1 + this.http.lastIndexOf("/"));
    }

    @Override
    public String toString() {
        return date + " " + name + " (" + getJpgName() + ")";
    }
}
