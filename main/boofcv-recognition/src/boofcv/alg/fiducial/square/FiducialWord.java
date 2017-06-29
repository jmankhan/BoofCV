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

/**
 * Created by Jalal on 6/25/2017.
 */
public class FiducialWord {
	private String bits;
	private double probability = 0.0;

	public FiducialWord(String bitSequence) {
		this.bits = bitSequence;
		this.probability = 0.0;
	}

	/**
	 * Gets the bit transitions in this word.
	 *
	 * @return 1 - pairs / (n-1)
	 */
	public int getBitTransitions() {
		int pairs = 0;
		for (int i = 0; i < bits.length() - 2; i++) {
			if (bits.charAt(i + 1) == bits.charAt(i))
				pairs++;
		}
		return 1 - pairs / (bits.length() - 1);
	}

	public double getWordAppearences(FiducialDictionary dict) {
		if (dict.getSize() == 0)
			return 1;

		int occurrences = 0;
		int totalWords = 0;
		for (FiducialMarker marker : dict.getBackingArray()) {
			if (marker.contains(bits))
				occurrences++;

			totalWords += marker.getWords().length;
		}

		return 1.0 - (1.0 * occurrences / totalWords);
	}

	public char charAt(int i) {
		return bits.charAt(i);
	}

	public boolean equalsString(String obj) {
		return bits.equals(obj);
	}

	public String getBits() {
		return bits;
	}

	public void updateProbability(double p) {
		this.probability = p;
	}

	public double getProbability() {
		return probability;
	}
}
