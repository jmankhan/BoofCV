/*
 * Copyright (c) 2011-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.fiducial.square;

import org.ejml.simple.SimpleMatrix;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Jalal on 6/4/2017.
 */
public class FiducialMarker {

	private String[] wordStrings;
	private FiducialWord[] words;
	private double probability = 0.0;
	private int id;


	public FiducialMarker(FiducialWord[] words) {
		this.words = words;
	}

	/**
	 * Check if the given word is equal to any words in this marker
	 * @param word
	 * @return true if there is a matching word in this marker
	 */
	public boolean contains(String word) {
		for(FiducialWord w : words)
			if(w.equalsString(word))
				return true;
		return false;
	}

	public boolean matches(FiducialMarker other) {
		if(other.getWords().length != words.length)
			return false;

		for(int i=0; i<words.length; i++)
			if(!words[i].getBits().equals(other.getWords()[i].getBits()))
				return false;

		return true;
	}

	public int calcSelfHammingDistance() {
		int h1 = calcHammingDistance(rotate(1));
		int h2 = calcHammingDistance(rotate(2));
		int h3 = calcHammingDistance(rotate(3));
		return Math.min(h1, Math.min(h2, h3));
	}

	public int calcMinHammingDistance(FiducialMarker other) {
		int h1 = calcHammingDistance(other.getWords());
		int h2 = calcHammingDistance(other.rotate(1));
		int h3 = calcHammingDistance(other.rotate(2));
		int h4 = calcHammingDistance(other.rotate(3));

		return Math.min(h1, Math.min(h2, Math.min(h3, h4)));
	}

	/**
	 * Calculates the hamming distance between two markers
	 * @param other
	 * @return
	 */
	public int calcHammingDistance(FiducialWord[] other) {
		if(words.length != other.length)
			return -1;

		int dist = 0;
		for(int i=0; i<words.length; i++) {
			for(int j=0; j<words.length; j++) {

				if(words[i].charAt(j) != other[i].charAt(j))
					dist++;
			}
		}

		return dist;
	}

	/**
	 * Rotates to the left by 90 degrees n times
	 * @return rotated String[]
	 */
	public FiducialWord[] rotate(int times) {
		String[] copy = new String[words.length];
		for(int i=0; i<words.length; i++)
			copy[i] = words[i].getBits();

		for(int i=0; i<times%4; i++) {
			copy = transpose(copy);
			copy = swapRows(copy);
		}

		FiducialWord[] ret = new FiducialWord[words.length];
		for(int i=0; i<words.length; i++)
			ret[i] = new FiducialWord(copy[i]);
		return ret;
	}

	private String[] swapRows(String[] m) {
		for (int  i = 0, k = m.length - 1; i < k; ++i, --k) {
			String x = m[i];
			m[i] = m[k];
			m[k] = x;
		}

		return m;
	}

	private String[] transpose(String[] m) {
		char[][] tmp = new char[m.length][m.length];
		for(int i=0; i<m.length; i++)
			tmp[i] = m[i].toCharArray();

		for(int i=0; i<tmp.length; i++) {
			for(int j=i; j<tmp[i].length; j++) {
				char x = tmp[i][j];
				tmp[i][j] = tmp[j][i];
				tmp[j][i] = x;
			}
		}

		String[] transposed = new String[tmp.length];
		for(int i=0; i<tmp.length; i++)
			transposed[i] = new String(tmp[i]);
		return transposed;
	}

	public int getId() {
		return this.id;
	}

	public FiducialWord[] getWords() {
		return this.words;
	}

	public Image toImage() {
		int n = words[0].getBits().length();
		BufferedImage img = new BufferedImage((n+2), (n+2), BufferedImage.TYPE_INT_ARGB);
		int rgb = Color.BLACK.getRGB();
		for(int i=0; i<n+2; i++) {
			img.setRGB(i,0,rgb);
			img.setRGB(i,img.getHeight()-1,rgb);
			img.setRGB(0, i, rgb);
			img.setRGB(img.getWidth()-1, i, rgb);
		}
		for(int i=0;i<words.length;i++) {
			for(int j=0; j<words[i].getBits().length(); j++) {
				Color color = words[i].charAt(j) == '0' ? Color.BLACK : Color.WHITE;
				img.setRGB(i+1,j+1, color.getRGB());
			}
		}


		return img.getScaledInstance(300, 300, BufferedImage.SCALE_AREA_AVERAGING);
	}

	public void updateProbability(FiducialDictionary dictionary) {

	}

	public double getProbability() {
		return this.probability;
	}

	public void setProbability(double p) {
		this.probability = p;
	}
}
