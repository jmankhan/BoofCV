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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jalal on 6/3/2017.
 */
public class FiducialMarkerGenerator {

	/**
	 * Generate all Markers of size n. Note that n represents the internal grid size and not the border
	 * The returned array includes an outside border of 1 elements around the entire internal grid, so the size
	 * of the marker will be (n+2) * (n+2).
	 */
	public static void generate(FiducialDictionary dictionary) {
		int n = dictionary.getN();
		//generate all byte sequences possible for markers of size n
		int size = (int) Math.pow(2, n);
		String[] byteSequences = new String[size];
		List<FiducialWord> allWords = new LinkedList<>();

		Comparator<FiducialWord> comp = new Comparator<FiducialWord>() {
			@Override
			public int compare(FiducialWord o1, FiducialWord o2) {
				return Double.compare(o2.getProbability(), o1.getProbability());
			}
		};

		for(int i=0; i<size; i++) {
			byteSequences[i] = Integer.toBinaryString(i);
			if(byteSequences[i].length() < n) {
				String prepend = "";
				for(int j=0; j<n-byteSequences[i].length(); j++)
					prepend += "0";

				byteSequences[i] = prepend + byteSequences[i];
			}
			allWords.add(new FiducialWord(byteSequences[i]));

		}

		int tau = 4;
		int failures = 0;
		boolean first = true;

		while(dictionary.getSize() != n) {
			if(failures > 2) {
				tau--;
				failures = 0;
			}

			for(FiducialWord word : allWords) {
				int t = word.getBitTransitions();
				double o = word.getWordAppearences(dictionary);

				word.updateProbability( t*o/dictionary.getSum());
			}

			Collections.sort(allWords, comp);
			if(first) {
				Collections.shuffle(allWords);
				first = false;
			}

			if(allWords.size() < n)
				return;

			FiducialWord[] candidates = new FiducialWord[n];
			for(int i=0; i<n; i++)
				candidates[i] = allWords.get(i);
			FiducialMarker marker = new FiducialMarker(candidates);
			int dist = dictionary.getDistanceFromDictionary(marker);
			if(dist >= tau) {
				dictionary.add(marker);
			}
			else {
				failures++;
			}
		}

	}
}
