package saim.com.afcdemoapps.Model;

/**
 * Created by NREL on 2/17/18.
 */

public class ModelImage {

    public String url, title, upload_at;
    public int height, width;

    public ModelImage(String url, String title, String upload_at, int height, int width) {
        this.url = url;
        this.title = title;
        this.upload_at = upload_at;
        this.height = height;
        this.width = width;
    }
}
