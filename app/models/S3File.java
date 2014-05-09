package models;

/**
 * Created by AppBuddy on 04.04.2014.
 */

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import plugins.S3Plugin;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import play.Logger;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class S3File{

    private static enum ImageFormat {
        THUMB, THUMB2X;
    }


    private String bucket = "onair";
    private String urlPrefix = "https://"+ bucket+ "." +"s3.amazonaws.com";
    public String name;
    private String pathPrefix = "moments/attachments/";

    public String pathExtension;

    private String generatedPath = null;

    private ImageFormat[] imageFormats = {ImageFormat.THUMB, ImageFormat.THUMB2X};

    public File file;

    public String save(File saveFile, String type) {
        if(generatedPath == null){
            generatePath();
        }
        if(type.equals("none")){
            Logger.error("No ImageFormat provided");
        }

        String fileUrl = urlPrefix + "/";
        String actualFileName = pathPrefix + pathExtension + "/" + generatedPath + "/" + type + "/" + name;
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not save because amazonS3 was null");
            throw new RuntimeException("Could not save");
        }
        else {
            Logger.debug("ACTUALFILENAME: "+actualFileName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, actualFileName, saveFile);
            putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
            S3Plugin.amazonS3.putObject(putObjectRequest);
        }

        return fileUrl+actualFileName;
    }


    public void generatePath(){
        this.generatedPath = UUID.randomUUID().toString();

    }

    public void convert(String momentID) {
       long starttime = System.currentTimeMillis();
        BufferedImage bufferedImage = null;
        String type = "none";
        try {
            bufferedImage = ImageIO.read(file);
            Logger.info("Able to fetch BufferedImage from file: "+file.getName());
        } catch (IOException e) {
            Logger.error("Could not get BufferedImage: "+e.getMessage());
        }
        if(bufferedImage != null) {
            for (ImageFormat imgFormat : imageFormats) {
                double sideLength;

                switch (imgFormat) {
                    case THUMB:
                        type="thumb";
                        sideLength = 45;
                        break;
                    case THUMB2X:
                        type="thumb2x";
                        sideLength = 90;
                        break;
                    default:
                        return;
                }

                double scaleFactorX = sideLength/bufferedImage.getWidth();
                double scalefactorY = sideLength/bufferedImage.getHeight();
                Logger.debug("sidelength: "+sideLength);
                Logger.debug("scale X: "+scaleFactorX);
                Logger.debug("scale Y: "+scalefactorY);


                BufferedImage after = new BufferedImage((int) sideLength, (int) sideLength, BufferedImage.TYPE_INT_ARGB);
                AffineTransform at = new AffineTransform();
                at.scale(scaleFactorX, scalefactorY);
                AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                after = scaleOp.filter(bufferedImage, after);

                Logger.debug("After image for imagetype "+ type +" with length X: "+after.getWidth());
                Logger.debug("After image for imagetype "+ type +" with length Y: "+after.getHeight());

                File scaled = new File(name);
                try {
                    ImageIO.write(after,"png",scaled);
                    String url = save(scaled, type);
                    Moment.addAttachment(momentID, "image", type, url);
                    Logger.debug("Image has been written to file");
                    if(scaled.exists()) {
                        scaled.delete();
                    }
                } catch (IOException e) {
                    Logger.error("Could not write to file: "+e.getMessage());
                }
                long duration = System.currentTimeMillis()-starttime;
                Logger.debug("Scaling took "+duration+ " milliseconds");

            }
        }
    }



    public void delete(String filename) {
        if (S3Plugin.amazonS3 == null) {
            Logger.error("Could not delete because amazonS3 was null");
            throw new RuntimeException("Could not delete");
        }
        else {
            S3Plugin.amazonS3.deleteObject(bucket, filename);
        }
    }

}