import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import util.FileInfo;

import java.io.File;
import java.io.IOException;

public class TestRemoveBg {
    public static void main(String[] args) {
        FileInfo fileInfo = new FileInfo();

        try {
            Response response = Request.Post("https://api.remove.bg/v1.0/removebg")
                    .addHeader("X-Api-Key", "ituKqhsZFxfEFBT1cB2Y2jmL")
                    .body(
                            MultipartEntityBuilder.create()
                                    .addBinaryBody("image_file", new File(fileInfo.getPath()))
                                    .addTextBody("size", "auto")
                                    .build()
                    ).execute();
            response.saveContent(new File(fileInfo.getDirectory() + fileInfo.getName() + "_no-bg.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished");
    }
}
