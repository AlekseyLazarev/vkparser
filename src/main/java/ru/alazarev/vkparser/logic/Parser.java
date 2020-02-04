package ru.alazarev.vkparser.logic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.alazarev.vkparser.entity.Picture;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parse downloaded by vkOpt html VK page and download all pictures from there.
 * <p>
 * Date: 04.02.2020
 *
 * @author a.lazarev
 */
@Component
@PropertySource(value = "app.properties", encoding = "UTF-8")
public class Parser {
    @Value("${picPath}")
    private String picPath;
    @Value("${filePath}")
    private String filePath;
    @Value("${threads}")
    private int numberOfThreads;
    @Value("${dateTimePattern}")
    private String dateTimeReg;
    @Value("${linkPattern}")
    private String picReg;
    @Value("${filePattern}")
    private String fileReg;
    @Value("${fileExt}")
    private String fileExt;
    @Value("${picExt}")
    private String picExt;
    @Value("${defaultCharset}")
    private String charSet;
    @Value("${messageClass}")
    private String messageClass;
    @Value("${attachmentClass}")
    private String attachmentClass;
    @Value("${linkAttribute}")
    private String linkAttribute;
    @Value("${innerElementClass}")
    private String innerElementClass;
    @Value("${userNameTag}")
    private String userNameTag;

    /**
     * Method get pictures from html file
     *
     * @param fileName file name.
     * @return List of pictures.
     */
    private List<Picture> getPicturesFromHtml(String fileName) {
        List<Picture> result = new LinkedList<>();
        Pattern dateTime = Pattern.compile(this.dateTimeReg);
        Pattern pic = Pattern.compile(this.picReg);
        try {
            File file = new File(this.filePath + fileName + "." + this.fileExt);
            Document html = Jsoup.parse(file, this.charSet);
            Elements msgs = html.getElementsByClass(this.messageClass);
            for (Element msg : msgs) {
                String date = null;
                String from = null;
                String http = null;
                boolean goNext = false;
                Elements attachments = msg.getElementsByClass(this.attachmentClass);
                for (Element e : attachments) {
                    String strElement = e.getElementsByAttribute(this.linkAttribute).outerHtml();
                    Matcher picMatcher = pic.matcher(strElement);
                    while (picMatcher.find()) {
                        http = strElement.substring(picMatcher.start(), picMatcher.end());
                        goNext = true;
                    }
                }
                if (goNext) {
                    Elements curr = msg.getElementsByClass(this.innerElementClass);
                    String currentString = curr.toString();
                    Matcher dateMatcher = dateTime.matcher(currentString);
                    while (dateMatcher.find()) {
                        date = currentString.substring(dateMatcher.start(), dateMatcher.end());
                    }
                    for (Element currEl : curr) {
                        from = currEl.getElementsByTag(this.userNameTag).text();
                    }
                }
                if (date != null && from != null && http != null) {
                    result.add(new Picture(from, http, date));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Method create folder if not exists.
     *
     * @param fullPath full path to file.
     */
    private void createDirIfNeed(String fullPath) {
        File file = new File(fullPath);
        while (!file.exists()) {
            if (!file.mkdir()) {
                createDirIfNeed(fullPath.substring(0, fullPath.lastIndexOf("\\")));
            }
        }
    }

    /**
     * Method gets pictures from a file, then download them to folder, using some threads.
     */
    public void parse() {
        String[] files = new File(this.filePath).list();
        Pattern pattern = Pattern.compile(this.fileReg);
        for (String file : files) {
            System.out.println("Start downloading . . . " + file);
            Matcher m = pattern.matcher(file);
            if (m.find()) {
                file = file.substring(m.start(), m.end() - 1);
            }
            createDirIfNeed(this.picPath + file);
            List<Picture> pictures = getPicturesFromHtml(file);
            ExecutorService service = Executors.newFixedThreadPool(this.numberOfThreads);
            for (Picture pic : pictures) {
                service.submit(new Downloader(pic, this.picPath + file));
            }
            service.shutdown();
        }
    }
}
