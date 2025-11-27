package ocr;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;



public class TesseractOCRService implements OCRService {

    @SuppressWarnings("deprecation")
    @Override
    public String extractText(String imagePath) throws Exception {
        BufferedImage input = ImageIO.read(new File(imagePath));
        if(input==null) throw new IOException("Could not read image file: " + imagePath);

        BufferedImage preprocessed = preprocessForOCR(input);
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata"); // folder containing eng.traineddata
        tesseract.setLanguage("eng");

        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
        
        tesseract.setTessVariable("user_defined_dpi", "300");

        return tesseract.doOCR(preprocessed);
    }

    private BufferedImage preprocessForOCR(BufferedImage src){
        BufferedImage gray = toGrayscale(src);

        int minDim = Math.min(gray.getWidth(), gray.getHeight());
        BufferedImage scaled = gray;
        if(minDim < 300){
            double scale = 2.0;
            scaled = scaleImage(gray, scale);
        }
        BufferedImage contrasted = contrastStretch(scaled);
        BufferedImage binary = otsuBinarize(contrasted);
        return binary;
    }

    private BufferedImage toGrayscale(BufferedImage src){
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(),
        BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g= gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage scaleImage(BufferedImage src, double scale){
        int w = (int) Math.round(src.getWidth()*scale);
        int h = (int) Math.round(src.getHeight()*scale);
        BufferedImage scaled = new BufferedImage(w, h,src.getType());
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp ato = new AffineTransformOp (at, AffineTransformOp.TYPE_BILINEAR);
        ato.filter(src, scaled);
        return scaled;

    }

    private BufferedImage contrastStretch(BufferedImage src){
        Raster r = src.getRaster();
        int w = src.getWidth(), h=src.getHeight();
        int[]pixels =new int [w*h];
        r.getPixels(0, 0, w, h, pixels);

        int min = 255, max = 0;
        for(int p: pixels){
            if(p < min) min = p;
            if(p > max) max = p;
        }
        if(max==min) return src;

        float scale = 255f / (max - min);
        for(int i = 0; i<pixels.length; i++){
            int v = Math.round((pixels[i]- min)*scale);
            if(v<0) v=0;
            if (v>255) v =255;
            pixels[i]=v;
        }

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        out.getRaster().setPixels(0, 0, w, h, pixels);
        return out;
    }


    private BufferedImage otsuBinarize (BufferedImage src){
            int w = src.getWidth();
            int h = src.getHeight();
            Raster raster = src.getRaster();
            int[] pixels = new int[w*h];
            raster.getPixels(0, 0, w, h, pixels);

            int[]hist = new int[256];
            for(int v : pixels ) hist[v]++;

            int total = w*h;
            float sum =0;
            for(int t= 0; t<256;t++) sum += t*hist[t];

            float sumB = 0;
            int wB = 0;
            int wF;
            float varMax= 0;
            int threshold=0;

            for(int t = 0; t<256; t++){
                wB += hist[t];
                if(wB==0) continue;
                wF =total -wB;
                if(wF==0) break;

                sumB +=(float) (t*hist[t]);
                float mB = sumB/wB;
                float mF= (sum -sumB)/wF;

                float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
                if(varBetween > varMax){
                    varMax = varBetween;
                    threshold = t;
                }
            }

            BufferedImage bin = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
            WritableRaster wr = bin.getRaster();
            int idx=0;
            for(int y =0; y<h; y++){
                for(int x = 0; x<w; x++){
                    int v =pixels[idx++];
                    int val = (v> threshold)? 255: 0;
                    wr.setSample(x,y,0,val);
                }
            }
            return bin;
        }

}