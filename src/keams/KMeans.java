package keams;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class KMeans {
	BufferedImage original;
	BufferedImage result;
	Cluster[] clusters;
	

	public static void main(String[] args) {

		// parse arguments
		String src = "C:\\Users\\JIHOON\\Desktop\\����\\���û���\\qweqwe.jpg";
		int k = 3;
		// String m = "-1";
		
		/*
		 * if (m.equals("-c")) { mode = MODE_ITERATIVE; } else if (m.equals("-c")) {
		 * mode = MODE_CONTINUOUS; }
		 */

		// create new KMeans object
		KMeans kmeans = new KMeans();
		// call the function to actually start the clustering
		BufferedImage dstImage = kmeans.calculate(loadImage(src), k);
		// save the resulting image
		JFrame frame = new JFrame();
		JFrame frame2 = new JFrame();

		JLabel lblimage = new JLabel(new ImageIcon(dstImage));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		// add more components here
		frame.add(mainPanel);
		frame.setVisible(true);
		frame.setSize(500, 500);

		JLabel lblimage2 = new JLabel(new ImageIcon(src));
		JPanel mainPanel2 = new JPanel(new BorderLayout());
		mainPanel2.add(lblimage2);
		frame2.add(mainPanel2);
		frame2.setVisible(true);
		frame2.setSize(500, 500);

		// saveImage(dst, dstImage);
	}

	
	public KMeans() {
	}

	
	public BufferedImage calculate(BufferedImage image, int k) {
		long start = System.currentTimeMillis(); // ���� ���� �ð�
		int w = image.getWidth();
		int h = image.getHeight();
		// clusters ����
		clusters = createClusters(image, k);
		// cluster ��ȸ ���̺� ����
		int[] lut = new int[w * h];
		Arrays.fill(lut, -1); // lut �迭 ��ü �ʱ�ȭ

		// at first loop all pixels will move their clusters
		// ó�� �������� ��� pixel�� cluster�� �̵�
		boolean pixelChangedCluster = true;
		// loop until all clusters are stable!
		// ��� cluster�� ������ ������ ����
		int loops = 0;

		while (pixelChangedCluster == true) {
			pixelChangedCluster = false;
			loops++;// �ð� ���, ���X

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y); // ������ pixel�� RGB��
					Cluster cluster = findMinimalCluster(pixel); // cluster�� ���� ���� ��

					if (lut[w * y + x] != cluster.getId()) { // cluster(distance) ��� ���� �ƴϸ�
						// cluster changed, cluster ����
						
							if (lut[w * y + x] != -1) {
								// remove from possible previous
								// cluster
								// Pixel RGB ���� ������, pixelCount ����
								clusters[lut[w * y + x]].removePixel(pixel); //cluster�� ��� �۾�����
							}
							
							// add pixel to cluster
							cluster.addPixel(pixel);
						
						
						// ��� looping
						pixelChangedCluster = true;
						// update lut
						lut[w * y + x] = cluster.getId();
						
					} // if((lut[w * y + x] != cluster.getId())
					
				} // for(x < w)
				
			} // for(y < h)
			

		} // while()
		

		
		// ��� �̹��� ����
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int clusterId = lut[w * y + x];
				result.setRGB(x, y, clusters[clusterId].getRGB());
			}
		}

		long end = System.currentTimeMillis(); // ���� �ð�
		System.out.println("Clustered to " + k + " clusters in " + loops + " loops in " + (end - start) + " ms.");
		return result;
		
	}// BufferedImage calculate()
	

	
	
	public Cluster[] createClusters(BufferedImage image, int k) {
		// Here the clusters are taken with specific steps,
		// so the result looks always same with same image.
		// You can randomize the cluster centers, if you like.
		// Ư�� �ܰ迡 ���� cluster ����
		// ����� �׻� �̹����� ��Ÿ��
		// ���ϴ� ��� cluster �߽� ����ȭ ����
		Cluster[] result = new Cluster[k]; // ����ȭ 3��
		int x = 0;
		int y = 0;
		int dx = image.getWidth() / k;
		int dy = image.getHeight() / k;
		for (int i = 0; i < k; i++) {
			result[i] = new Cluster(i, image.getRGB(x, y)); // id�� 0, 1, 2 (�߽ɰ�)
			x += dx;
			y += dy;
		}
		return result;
	}// createClusters
	

	
	public Cluster findMinimalCluster(int rgb) {
		Cluster cluster = null;
		int min = Integer.MAX_VALUE; // �ִ� �ڸ��� ���ڸ� ����
		for (int i = 0; i < clusters.length; i++) {
			int distance = clusters[i].distance(rgb); // ������ ��� ������ �ֱ�
			if (distance < min) {
				min = distance;
				cluster = clusters[i];
			}
		}
		return cluster; // ���� ���� �� �ֱ�
	}// findMinimalCluster()


	
	public static BufferedImage loadImage(String filename) {
		BufferedImage result = null; // �̹��� �ҷ�����
		try {
			result = ImageIO.read(new File(filename));
		} catch (Exception e) {
			System.out.println(e.toString() + " Image '" + filename + "' not found.");
		}
		return result;
	}// loadImage()
	

	
	
	class Cluster {
		int id;
		int pixelCount;
		int red;
		int green;
		int blue;
		int reds;
		int greens;
		int blues;

		public Cluster(int id, int rgb) { // 2byte->1pixel, R(5) G(6) B(5)
			int r = rgb >> 16 & 0x000000FF; // rgb�� ���������� 16 & 0x000000FF ��ŭ ����Ʈ �Ѵ�
			int g = rgb >> 8 & 0x000000FF; // 8 & 0x000000FF -> 8������ 256��Ʈ
			int b = rgb >> 0 & 0x000000FF;
			red = r;
			green = g;
			blue = b;
			this.id = id;
			addPixel(rgb);
		}

	
		int getId() {
			return id;
		}

		int getRGB() {
			int r = reds / pixelCount;
			int g = greens / pixelCount;
			int b = blues / pixelCount;
			return 0xff000000 | r << 16 | g << 8 | b; // r�� << �����ڷ� ���� 16��Ʈ ����Ʈ
		}

		void addPixel(int color) { 
			int r = color >> 16 & 0x000000FF;
			int g = color >> 8 & 0x000000FF;
			int b = color >> 0 & 0x000000FF;
			reds += r;
			greens += g;
			blues += b;
			pixelCount++; // Update ����
			red = reds / pixelCount; 
			green = greens / pixelCount;
			blue = blues / pixelCount;
		}

		void removePixel(int color) {
			int r = color >> 16 & 0x000000FF;
			int g = color >> 8 & 0x000000FF;
			int b = color >> 0 & 0x000000FF;
			reds -= r;
			greens -= g;
			blues -= b;
			pixelCount--;
			red = reds / pixelCount;
			green = greens / pixelCount;
			blue = blues / pixelCount;
		}

		int distance(int color) { // ������ ���� ���� �ּ�ȭ�� �Ǵ� �߽��� ã��
			int r = color >> 16 & 0x000000FF;
			int g = color >> 8 & 0x000000FF;
			int b = color >> 0 & 0x000000FF;
			int rx = Math.abs(red - r); // ������ �����
			int gx = Math.abs(green - g);
			int bx = Math.abs(blue - b);
			int d = (rx + gx + bx) / 3; // ������ ��� ������
			return d;
		}
	}// Cluster()

	
}
