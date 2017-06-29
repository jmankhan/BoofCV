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

import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;

/**
 * Creates and decodes arbitrary fiducial dictionaries using the algorithm described here:
 *
 * <a href="http://www.sciencedirect.com/science/article/pii/S0031320314000235"></a>
 *
 *
 * Created by Jalal on 6/3/2017.
 */
public class FiducialDictionary {

	/**
	 * described as tau in the algorithm, this is the minimum distance between a newly generated marker
	 * and those already in the dictionary.
	 */
	private int minDistance;

	private ArrayList<FiducialMarker> backingArray;

	/**
	 * size of the dictionary
	 */
	private int size;

	/**
	 * Width of each word in the fiducial marker
	 */
	private int n;


	public FiducialDictionary(int size, int n) {

		backingArray = new ArrayList<>(size);
		this.size = size;
		this.n = n;
		this.minDistance = 2* (int) Math.floor(n*n*2/3);

		FiducialMarkerGenerator.generate(this);
	}

	public int getSize() {
		return this.backingArray.size();
	}

	public ArrayList<FiducialMarker> getBackingArray() {
		return this.backingArray;
	}


	public int getN() {
		return this.n;
	}

	public void add(FiducialMarker marker) {
		backingArray.add(marker);
	}

	//think of a better name for this
	public double getSum() {
		double sum = 0;
		for(FiducialMarker marker : getBackingArray()) {
			for(FiducialWord word : marker.getWords()) {
				int t = word.getBitTransitions();
				double o = word.getWordAppearences(this);
				sum += t*o;
			}
		}

		return sum;
	}

	/**
	 * Finds the closest hamming distance of any element in dictionary compared to parameter
	 * @param marker Marker m to compare
	 * @return int smallest hamming distance
	 */
	public int getDistanceFromDictionary(FiducialMarker marker) {
		int selfDistance = marker.calcSelfHammingDistance();
		int lowest = Integer.MAX_VALUE;
		for(FiducialMarker m : backingArray) {
			int hamming = marker.calcMinHammingDistance(m);
			if(hamming < lowest) {
				lowest = hamming;
			}
		}

		return Math.min(lowest, selfDistance);
	}

	public boolean contains(FiducialMarker marker) {
		for(FiducialMarker m : backingArray) {
			if(marker.matches(m))
				return true;
		}
		return false;
	}
}
