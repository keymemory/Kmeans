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
		String src = "C:\\Users\\JIHOON\\Desktop\\사진\\샘플사진\\qweqwe.jpg";
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
		long start = System.currentTimeMillis(); // 실행 시작 시간
		int w = image.getWidth();
		int h = image.getHeight();
		// clusters 생성
		clusters = createClusters(image, k);
		// cluster 조회 테이블 생성
		int[] lut = new int[w * h];
		Arrays.fill(lut, -1); // lut 배열 전체 초기화

		// at first loop all pixels will move their clusters
		// 처음 루프에서 모든 pixel이 cluster로 이동
		boolean pixelChangedCluster = true;
		// loop until all clusters are stable!
		// 모든 cluster가 안정될 때까지 루프
		int loops = 0;

		while (pixelChangedCluster == true) {
			pixelChangedCluster = false;
			loops++;// 시간 계산, 상관X

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					int pixel = image.getRGB(x, y); // 각각의 pixel의 RGB값
					Cluster cluster = findMinimalCluster(pixel); // cluster의 가장 작은 값

					if (lut[w * y + x] != cluster.getId()) { // cluster(distance) 평균 값이 아니면
						// cluster changed, cluster 변경
						
							if (lut[w * y + x] != -1) {
								// remove from possible previous
								// cluster
								// Pixel RGB 값이 없으면, pixelCount 감소
								clusters[lut[w * y + x]].removePixel(pixel); //cluster값 계속 작아지기
							}
							
							// add pixel to cluster
							cluster.addPixel(pixel);
						
						
						// 계속 looping
						pixelChangedCluster = true;
						// update lut
						lut[w * y + x] = cluster.getId();
						
					} // if((lut[w * y + x] != cluster.getId())
					
				} // for(x < w)
				
			} // for(y < h)
			

		} // while()
		

		
		// 결과 이미지 생성
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int clusterId = lut[w * y + x];
				result.setRGB(x, y, clusters[clusterId].getRGB());
			}
		}

		long end = System.currentTimeMillis(); // 종료 시간
		System.out.println("Clustered to " + k + " clusters in " + loops + " loops in " + (end - start) + " ms.");
		return result;
		
	}// BufferedImage calculate()
	

	
	
	public Cluster[] createClusters(BufferedImage image, int k) {
		// Here the clusters are taken with specific steps,
		// so the result looks always same with same image.
		// You can randomize the cluster centers, if you like.
		// 특정 단계에 따라 cluster 수행
		// 결과는 항상 이미지로 나타냄
		// 원하는 경우 cluster 중심 랜덤화 가능
		Cluster[] result = new Cluster[k]; // 군집화 3개
		int x = 0;
		int y = 0;
		int dx = image.getWidth() / k;
		int dy = image.getHeight() / k;
		for (int i = 0; i < k; i++) {
			result[i] = new Cluster(i, image.getRGB(x, y)); // id는 0, 1, 2 (중심값)
			x += dx;
			y += dy;
		}
		return result;
	}// createClusters
	

	
	public Cluster findMinimalCluster(int rgb) {
		Cluster cluster = null;
		int min = Integer.MAX_VALUE; // 최대 자리의 숫자를 받음
		for (int i = 0; i < clusters.length; i++) {
			int distance = clusters[i].distance(rgb); // 군집의 평균 데이터 넣기
			if (distance < min) {
				min = distance;
				cluster = clusters[i];
			}
		}
		return cluster; // 가장 작은 값 넣기
	}// findMinimalCluster()


	
	public static BufferedImage loadImage(String filename) {
		BufferedImage result = null; // 이미지 불러오기
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
			int r = rgb >> 16 & 0x000000FF; // rgb를 오른쪽으로 16 & 0x000000FF 만큼 시프트 한다
			int g = rgb >> 8 & 0x000000FF; // 8 & 0x000000FF -> 8진수로 256비트
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
			return 0xff000000 | r << 16 | g << 8 | b; // r은 << 연산자로 인해 16비트 쉬프트
		}

		void addPixel(int color) { 
			int r = color >> 16 & 0x000000FF;
			int g = color >> 8 & 0x000000FF;
			int b = color >> 0 & 0x000000FF;
			reds += r;
			greens += g;
			blues += b;
			pixelCount++; // Update 갯수
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

		int distance(int color) { // 각점의 합이 가장 최소화가 되는 중심점 찾기
			int r = color >> 16 & 0x000000FF;
			int g = color >> 8 & 0x000000FF;
			int b = color >> 0 & 0x000000FF;
			int rx = Math.abs(red - r); // 음수를 양수로
			int gx = Math.abs(green - g);
			int bx = Math.abs(blue - b);
			int d = (rx + gx + bx) / 3; // 군집의 평균 데이터
			return d;
		}
	}// Cluster()

	
}
