package Utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataHandler {
    public static ImageView blobToImage(Blob blob, int width, int height) {
        ImageView image = null;
        try {
            if (blob != null && blob.length() != 0) {
                byte[] byteHex = blob.getBytes(1, (int) blob.length());
                try (InputStream inputStream = new ByteArrayInputStream(byteHex)) {
                    image = new ImageView(new Image(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.setPreserveRatio(true);
                image.setFitWidth(width);
                image.setFitHeight(height);
                return image;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ImageView();
    }

    public static byte[] imageToBlob(ImageView image) {
        if (image.getImage() != null) {
            BufferedImage bImage = SwingFXUtils.fromFXImage(image.getImage(), null);
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                ImageIO.write(bImage, "png", stream);
                return stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Calendar parseDate(String time, String format) throws ParseException {
        if (time == null || time.equals(""))
            return null;

        SimpleDateFormat SDF = new SimpleDateFormat(format);
        Date date = SDF.parse(time);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return calendar;
    }

    public static ImageView loadImage(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(new File(path).toPath());
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return new ImageView(new Image(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
