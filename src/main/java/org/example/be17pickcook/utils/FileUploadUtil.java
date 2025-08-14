package org.example.be17pickcook.utils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUploadUtil {
    public static String makeUploadPath() {
//        String basePath = "c:\\upload\\";

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File dir = new File(date);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                return date + "/"+ UUID.randomUUID() + "_";
            }
        }
        return date + "/"+UUID.randomUUID() + "_";
    }
}
