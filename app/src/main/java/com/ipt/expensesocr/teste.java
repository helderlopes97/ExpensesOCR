package com.ipt.expensesocr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.clearcut.ClearcutLogger;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class teste  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teste);

        byte[] byteArray;
        Bitmap bit;

                Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Cria Bitmap da imagem pelo path
            String path = extras.getString("path");
            bit = BitmapFactory.decodeFile(path);
            // Bitmap transformado
            Bitmap transformed = transform(bit);
            // Bitmap threshed
            Bitmap threshed = threshold(transformed);
            // Display
            ImageView imgTeste = findViewById(R.id.imgTeste);
            imgTeste.setImageBitmap(threshed);
        }
    }


    /**
     * Esta função recebe um Bitmap e devolve-lo depois de ser feito thresholding
     *
     * @param bit Bitmap para fazer o thresholding
     * @return Bitmap com thresholding
     */
    public Bitmap threshold(Bitmap bit) {
        // Cria um novo Mat a partir do Bitmap
        Mat src = new Mat(bit.getWidth(), bit.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bit, src);
        // Transforma o Mat em grayscale
        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);

        // Noise reduction
        //Imgproc.erode(src,src,new Mat());
        //Imgproc.dilate(src,src,new Mat());
        Imgproc.erode(src,src,new Mat(), new Point(-1,-1),10,1,new Scalar(1));
        Imgproc.dilate(src,src,new Mat(), new Point(-1,-1),10,1,new Scalar(1));

        /*
            De entre as possíveis funções de Threshold vai ser utilizada a ADAPTIVE_THRESH_GAUSSIAN_C.
            Esta é a mais indicada para fazer OCR.
        */

        // SIMPLE THRESHOLDING
        //Imgproc.threshold(tmp,tmp,100,255,Imgproc.THRESH_BINARY);

        // ADAPTIVE THRESHOLDING
        Imgproc.adaptiveThreshold(src,src,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, 31,2);
        //Imgproc.adaptiveThreshold(tmp,tmp,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY, 31,2);

        // OTSU'S THRESHOLDING
        //Imgproc.threshold(tmp,tmp,0,255,Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        // Converte o Mat final para o Bitmap
        Utils.matToBitmap(src, bit);
        // Devolve o Bitmap
        return bit;
    }

    /**
     * Descobre os contornos de uma imagem
     *
     * @param bit Bitmap da imagem
     * @return Bitmap da imagem com os contornos a verde
     */
    public Bitmap edges(Bitmap bit) {
        // Cria um novo Mat a partir do Bitmap
        Mat src = new Mat(bit.getWidth(), bit.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bit, src);
        // Transforma o Mat em grayscale
        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);
        // Cria um tamanho
        Size size = new Size(5, 5);
        // Faz Blur da imagem
        Imgproc.GaussianBlur(src, src, size, 0);
        // Transforma a imagem em apenas contornos
        Imgproc.Canny(src, src, 75, 20);
        // Cria uma lista para os contornos
        List<MatOfPoint> contours = new ArrayList<>();
        // Encontra os contornos e guarda-os na lista
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // Cria um novo Mat das mesmas dimensões que o inicial
        Mat d = new Mat();
        d.create(src.rows(),src.cols(),CvType.CV_8UC3);
        // Desenha os contornos a verde
        Imgproc.drawContours(d,contours,-1,new Scalar(0,255,0),1);
        // Converte o Mat a Bitmap
        Utils.matToBitmap(d, bit);
        // Devolve o Bitmap
        return bit;
    }


    /**
     * Calcula a inclinação de um texto perfeito
     *
     * @param bit Bitmap da imagem do texto
     * @return Bitmap com as linhas da inclinação
     */
    public Bitmap calcSkew(Bitmap bit){
        // Cria um novo Mat a partir do Bitmap
        Mat src = new Mat(bit.getWidth(), bit.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bit, src);
        // Transforma o Mat em grayscale
        Imgproc.cvtColor(src,src,Imgproc.COLOR_BGR2GRAY);

        //Imgproc.adaptiveThreshold(src,src,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV, 31,2);

        // guarda o tamanho da imagem
        Size size = src.size();
        // inverte os bits da imagem
        Core.bitwise_not(src,src);
        // Cria um novo Mat
        Mat lines = new Mat();
        // Descobre as linhas
        Imgproc.HoughLinesP(src, lines, 1, Math.PI/180, 100, 50, 30);
        // Novo Mat em preto
        Mat disp_lines = new Mat(size, CvType.CV_8UC1, new Scalar(0,0,0));
        // Inicia o ângulo
        double angle = 0;
        // Percorre as linas
        for (int i = 0; i < lines.rows(); i++) {
            // Coordenadas dos pontos que formam a linha
            double[] val = lines.get(i,0);
            // Desenha a linha
            Imgproc.line(disp_lines, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(255,0,0),1);
            // Calcula o ângulo e soma
            angle += Math.atan2(val[3] - val[1], val[2] - val[0]);
        }
        // Média do ângulo
        angle /= lines.rows();
        // Faz log do ângulo
        Log.e("RESULT", "calcSkew: "+ angle * 180 / Math.PI );
        // Tranforma em bitmap
        Utils.matToBitmap(disp_lines, bit);
        // Devolve o bitmap
        return bit;
    }

    /**
     * Encontra os 4 pontos que delimitam a forma fazem a sua transformação
     *
     * @param bit Bitmap da imagem original
     * @return Bitmap da imagem transformada
     */
    public Bitmap transform(Bitmap bit){
        // Transforma o Bitmap em Mat
        Mat src = new Mat(bit.getWidth(), bit.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bit, src);
        // Calcula o ratio da imagem para dar resize para 500px de altura
        double ratio = 500.0 / src.height();
        // Clone do Mat original
        Mat origin = src.clone();
        // Resize da imagem para 500px de altura
        Imgproc.resize(src, src, new Size((int) src.width()*ratio, 500));
        // Transforma a imagem em grayscale
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2GRAY);
        // Faz Blur da imagem
        Imgproc.GaussianBlur(src, src, new Size(5,5),0);
        // Contornos da imagem
        Imgproc.Canny(src,src,75,20);
        // Lista para contornos
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        // Mat temporário
        Mat hierarchy = new Mat();
        // Encontra os contornos e guarda na lista de contornos
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        // Liberta a memória do Mat temporário
        hierarchy.release();
        // Ordena os contornos por maior área de contorno
        sortContoursByArea(contours);
        // Percorre os contornos
        for(int i = 0; i < contours.size(); i++)
        {
            // Descobre forma
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
            double approxDistance = Imgproc.arcLength(contour2f,  true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            // Se a forma tem 4 pontos
            if (approxCurve.toArray().length==4){
                // Lista de pontos
                MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
                // Cria retângulo com a área dos pontos
                RotatedRect rect = Imgproc.minAreaRect(approxCurve);
                // Array de pontos
                Point[] pts = new Point[4];
                // Pontos que fazem o retângulo
                rect.points(pts);
                // Ordena os pontos: top left -> top right -> bottom left -> bottom right
                pts = orderPoints(pts);
                // Transformação dos pontos para a imagem original com o ratio
                for(int j = 0; j < pts.length; j++){
                    pts[j].x = pts[j].x / ratio;
                    pts[j].y = pts[j].y / ratio;
                }
                // Devolve a transformação pelos 4 pontos
                return four_point_transform(origin,pts);
            }
        }
        // Cria um novo Bitmap
        Bitmap bit2 = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        // Tranforma o Mat da imagem original no Bitmap
        Utils.matToBitmap(origin, bit2);
        // Devolve o bitmap
        return bit2;
    }

    /**
     * Faz a transformação da imagem com os 4 Pontos
     *
     * @param original Mat original
     * @param pts Array com os 4 pontos
     * @return Bitmap transformado
     */
    public Bitmap four_point_transform(Mat original, Point[] pts) {
        // Calcula a largura dos pontos inferiores
        double widthA = Math.sqrt(
                Math.pow((pts[2].x - pts[3].x),2) + Math.pow((pts[2].y - pts[3].y),2)
        );
        // Calcula a largura dos pontos superiores
        double widthB = Math.sqrt(
                Math.pow((pts[0].x - pts[1].x),2) + Math.pow((pts[0].y - pts[1].y),2)
        );
        // Largura máxima
        double maxWidth = Math.max(widthA,widthB);
        // Calcula a altura dos pontos da direita
        double heightA = Math.sqrt(
                Math.pow((pts[1].x - pts[3].x),2) + Math.pow((pts[1].y - pts[3].y),2)
        );
        // Calcula a altura dos pontos da esquerda
        double heightB = Math.sqrt(
                Math.pow((pts[0].x - pts[2].x),2) + Math.pow((pts[0].y - pts[2].y),2)
        );
        // Altura máxima
        double maxHeight = Math.max(heightA,heightB);

        // Pontos ordenados
        MatOfPoint2f src = new MatOfPoint2f(
                pts[0],
                pts[1],
                pts[2],
                pts[3]
        );
        // Pontos destino
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0,0),
                new Point(maxWidth - 1,0),
                new Point(0,maxHeight - 1),
                new Point(maxWidth - 1,maxHeight - 1)
        );
        // Mat com a perspetiva transformada
        Mat warped = Imgproc.getPerspectiveTransform(src,dst);
        // Nova imagem
        Mat newImage = new Mat();
        // Nova imagem com a perspetiva alterada da imagem original
        Imgproc.warpPerspective(original, newImage, warped, new Size(maxWidth,maxHeight) );
        // Cria novo Bitmap
        Bitmap bit = Bitmap.createBitmap(newImage.cols(), newImage.rows(), Bitmap.Config.ARGB_8888);
        // Transforma o Mat da nova imagem em Bitmap
        Utils.matToBitmap(newImage, bit);
        // Devolve Bitmap
        return bit;
    }

    /**
     * Ordena os 4 pontos
     * pts[0] = top left
     * pts[1] = top right
     * pts[2] = bottom left
     * pts[3] = bottom right
     *
     * @param pts Os 4 pontos a ordenar
     * @return Os pontos ordenados
     */
    public Point[] orderPoints(Point[] pts){
        Point[] ord = new Point[4];
        // inicia as variáveis
        double max_sum = 0;
        double min_sum = pts[0].x + pts[0].y;
        double max_dif = Math.abs(pts[0].x - pts[0].y);
        double min_dif = Math.abs(pts[0].x - pts[0].y);

        // percorre os pontos
        for (int i = 0; i < 4; i++) {
            // soma das coordenadas do ponto
            double sum = pts[i].x + pts[i].y;
            // a maior soma é bottom right
            if( sum >= max_sum ){
                ord[3] = pts[i];
                max_sum = sum;
            }
            // a menor soma é top left
            if( sum <= min_sum){
                ord[0] = pts[i];
                min_sum = sum;
            }
        }
        // percorre novamente os pontos
        for (int j = 0; j < 4; j++) {
            // diferença das coordenadas do ponto
            double dif = Math.abs(pts[j].x - pts[j].y);
            // a mair defirença que não é um dos anteriores é bottom left
            if( dif >= max_dif && pts[j]!=ord[3] && pts[j]!=ord[0]){
                ord[2] = pts[j];
                max_dif = dif;
            }
            // a menor defirença que não é um dos anteriores é top right
            if( dif <= min_dif  && pts[j]!=ord[3] && pts[j]!=ord[0]){
                ord[1] = pts[j];
                min_dif = dif;
            }
        }
        return ord;
    }


    /**
     * Ordena a lista de contornos pela maior área contornada
     *
     * @param contours Lista de contornos
     */
    public static void sortContoursByArea(List<MatOfPoint> contours) {
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint a, MatOfPoint b) {
                double areaA = Imgproc.contourArea(a);
                double areaB = Imgproc.contourArea(b);

                if (areaA > areaB) {
                    return -1;
                } else if (areaA < areaB) {
                    return 1;
                }
                return 0;
            }
        });
    }


}
