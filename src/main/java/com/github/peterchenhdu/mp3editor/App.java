package com.github.peterchenhdu.mp3editor;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) throws Exception {
        Mp3File mp3file = new Mp3File("D:/mp3/张芸京 - 偏爱.mp3");
        if (mp3file.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3file.getId3v2Tag();
            System.out.println("唱片歌曲数量: " + id3v2Tag.getTrack());
            System.out.println("艺术家: " + id3v2Tag.getArtist());
            System.out.println("歌曲名: " + id3v2Tag.getTitle());
            System.out.println("唱片名: " + id3v2Tag.getAlbum());
            System.out.println("歌曲长度:" + mp3file.getLengthInSeconds() + "秒");
            System.out.println("码率: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
            System.out.println("专辑插画 : " + id3v2Tag.getAlbumImage());
            System.out.println("专辑插画类型" + id3v2Tag.getAlbumImageMimeType());
            System.out.println("发行时间: " + id3v2Tag.getYear());
            System.out.println("流派: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
            System.out.println("注释: " + id3v2Tag.getComment());
            System.out.println("歌词: " + id3v2Tag.getLyrics());
            System.out.println("作曲家: " + id3v2Tag.getComposer());
            System.out.println("发行公司: " + id3v2Tag.getPublisher());
            System.out.println("Original artist: " + id3v2Tag.getOriginalArtist());
            System.out.println("Album artist: " + id3v2Tag.getAlbumArtist());
            System.out.println("版权: " + id3v2Tag.getCopyright());
            System.out.println("URL: " + id3v2Tag.getUrl());
            System.out.println("编码格式: " + id3v2Tag.getEncoder());
            byte[] albumImageData = id3v2Tag.getAlbumImage();
            if (albumImageData != null) {
                System.out.println("专辑插图长度: " + albumImageData.length + " bytes");
                System.out.println("专辑插图类型: " + id3v2Tag.getAlbumImageMimeType());
                reSize(new ByteArrayInputStream(albumImageData),  500, 500, true, id3v2Tag);
//                saveFile("测试一下.jpg", albumImageData);
            }


//            id3v2Tag.setAlbumImage();

            mp3file.save("D:/mp3/张芸京 - 偏爱2.mp3");
        }
    }

    public static void saveFile(String filename, byte[] data) throws Exception {
        if (data != null) {
            String filepath = "D:\\" + filename;
            File file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        }
    }


    /**
     * @param is     原图片
     * @param width      期望宽
     * @param height     期望高
     * @param equalScale 是否等比例缩放
     */
    public static void reSize(InputStream is, int width,
                              int height, boolean equalScale, ID3v2 id3v2Tag) {
//        String type = getImageType(is);
//        if (type == null) {
//            return;
//        }
        if (width < 0 || height < 0) {
            return;
        }

        BufferedImage srcImage = null;
        try {

            srcImage = ImageIO.read(is);
            System.out.println("is size=" + srcImage.getWidth() + "X" + srcImage.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // targetW，targetH分别表示目标长和宽
        BufferedImage target = null;
        double sx = (double) width / srcImage.getWidth();
        double sy = (double) height / srcImage.getHeight();
        // 等比缩放
        if (equalScale) {
            if (sx > sy) {
                sx = sy;
                width = (int) (sx * srcImage.getWidth());
            } else {
                sy = sx;
                height = (int) (sy * srcImage.getHeight());
            }
        }
        System.out.println("destImg size=" + width + "X" + height);
        ColorModel cm = srcImage.getColorModel();
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean alphaPremultiplied = cm.isAlphaPremultiplied();

        target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(srcImage, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();
        // 将转换后的图片保存
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(target, "jpg", baos);
            id3v2Tag.setAlbumImage(baos.toByteArray(),"image/jpeg");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}




