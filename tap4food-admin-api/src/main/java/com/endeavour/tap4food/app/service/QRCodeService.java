package com.endeavour.tap4food.app.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Service("qrCodeService")
public class QRCodeService {
	
	private OutputStream generatedFileStream;
	private File logoFile;
	
	@Value("${foodcourt.home.qrcode.url}")
	private String foodCourtUrl;

	@Value("${images.base.path}")
	private String imagesPath;
	
	@Value("${images.server}")
	private String imagesServer;
	
	public void generatQRCode() {

	}

	public String generateQRCodeImage(String foodCourtId)
			throws WriterException, IOException {
		
		String qrCodePath = imagesPath + File.separator + "QRCodes";
		
		imagesServer = imagesServer + "/QRCodes/" + foodCourtId + ".png";
		
		File qrCodeDirPath = new File(qrCodePath);
		
		qrCodeDirPath.mkdirs();

		generateColoredQRCode(foodCourtUrl + foodCourtId, qrCodePath + File.separator + foodCourtId + ".png");
		
		return imagesServer;
	}

	public void generateColoredQRCode(String data, String filePath) {
//		BufferedReader appInput = new BufferedReader(new InputStreamReader(System.in));

		while (this.getLogoFile() == null) {
			try {
				File resource = new ClassPathResource("logo.png").getFile();
				this.setLogoFile(resource);
				System.out.println("Logo File is set.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		try {
			this.setGeneratedFileStream(new FileOutputStream(new File(filePath)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		this.createQrCode(data, 300, "png");
		System.out.println("done.");
		try {
			this.getGeneratedFileStream().close();
		} catch (IOException ignored) {

		}

	}

	private void createQrCode(String foodCourtUrl, int qrCodeSize, String imageFormat) {
		try {
			// Correction level - HIGH - more chances to recover message
			Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

			// Generate QR-code
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(foodCourtUrl, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

			// Start work with picture
			int matrixWidth = bitMatrix.getWidth();
			BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
			graphics.setFont(font);
			graphics.setColor(Color.white);
			graphics.fillRect(0, 0, matrixWidth, matrixWidth);
			Color mainColor = new Color(51, 102, 153);
			graphics.setColor(mainColor);
			// Write message under the QR-code
			graphics.drawString(foodCourtUrl, 30, image.getHeight() - graphics.getFont().getSize());

			// Write Bit Matrix as image
			for (int i = 0; i < matrixWidth; i++) {
				for (int j = 0; j < matrixWidth; j++) {
					if (bitMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}

			// Add logo to QR code
			BufferedImage logo = ImageIO.read(this.getLogoFile());

			// scale logo image and insert it to center of QR-code
			double scale = calcScaleRate(image, logo);
			logo = getScaledImage(logo, (int) (logo.getWidth() * scale), (int) (logo.getHeight() * scale));
			graphics.drawImage(logo, image.getWidth() / 2 - logo.getWidth() / 2,
					image.getHeight() / 2 - logo.getHeight() / 2, image.getWidth() / 2 + logo.getWidth() / 2,
					image.getHeight() / 2 + logo.getHeight() / 2, 0, 0, logo.getWidth(), logo.getHeight(), null);

			// Check correctness of QR-code
			if (isQRCodeCorrect(foodCourtUrl, image)) {
				ImageIO.write(image, imageFormat, this.getGeneratedFileStream());
				System.out.println("Your QR-code was succesfully generated.");
			} else {
				System.out.println("Sorry, your logo has broke QR-code. ");
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Calc scale rate of logo. It is 30% of QR-code size
	 * 
	 * @param image
	 * @param logo
	 * @return
	 */
	private double calcScaleRate(BufferedImage image, BufferedImage logo) {
		double scaleRate = logo.getWidth() / image.getWidth();
		if (scaleRate > 0.3) {
			scaleRate = 0.3;
		} else {
			scaleRate = 1;
		}
		return scaleRate;
	}

	/**
	 * Check is QR-code correct
	 * 
	 * @param content
	 * @param image
	 * @return
	 */
	private boolean isQRCodeCorrect(String content, BufferedImage image) {
		boolean result = false;
		Result qrResult = decode(image);
		if (qrResult != null && content != null && content.equals(qrResult.getText())) {
			result = true;
		}
		return result;
	}

	/**
	 * Decode QR-code.
	 * 
	 * @param image
	 * @return
	 */
	private Result decode(BufferedImage image) {
		if (image == null) {
			return null;
		}
		try {
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Result result = new MultiFormatReader().decode(bitmap, Collections.EMPTY_MAP);
			return result;
		} catch (NotFoundException nfe) {
			nfe.printStackTrace();
			return null;
		}
	}

	/**
	 * Scale image to required size
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	private BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleX = (double) width / imageWidth;
		double scaleY = (double) height / imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

		return bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
	}

	public OutputStream getGeneratedFileStream() {
		return generatedFileStream;
	}

	public void setGeneratedFileStream(OutputStream generatedFileStream) {
		this.generatedFileStream = generatedFileStream;
	}
	
	public void closeFileStream() {
		
		if(this.generatedFileStream != null) {
			try {
				this.generatedFileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public File getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(File logoFile) {
		this.logoFile = logoFile;
	}

}
