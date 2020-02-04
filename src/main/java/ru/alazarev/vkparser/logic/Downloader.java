package ru.alazarev.vkparser.logic;

import ru.alazarev.vkparser.entity.Picture;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * Class download pictures to a folder using thread.
 */
public class Downloader implements Runnable {
    private Picture picture;
    private String root;

    /**
     * Constructor.
     *
     * @param picture picture for download.
     * @param root    download path.
     */
    public Downloader(Picture picture, String root) {
        this.picture = picture;
        this.root = root;
    }

    /**
     * Method download picture in current thread.
     */
    @Override
    public void run() {
        Thread.currentThread().getName();
        try {
            String fullPath = this.root + "\\" + this.picture.getName();
            new File(fullPath).mkdir();
            fullPath += "\\" + this.picture.toString() + ".jpg";
            URL url = new URL(this.picture.getHttp());
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();
            File f = new File(fullPath);
            if (!f.exists()) {
                Files.copy(inputStream, f.toPath());
            }
            System.out.println(this.picture.toString() + " downloaded!");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("FAIL download " + this.picture.toString());
        }
    }
}
