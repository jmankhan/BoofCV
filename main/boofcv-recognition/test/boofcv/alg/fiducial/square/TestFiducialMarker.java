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

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by Jalal on 6/17/2017.
 */
public class TestFiducialMarker {

	@Test
	public void testRotateOnce() {
		FiducialWord[] words = {new FiducialWord("101"), new FiducialWord("101"), new FiducialWord("101")};
		FiducialWord[] expected = {new FiducialWord("111"), new FiducialWord("000"), new FiducialWord("111")};
		FiducialMarker marker = new FiducialMarker(words);
		FiducialWord[] actual = marker.rotate(1);

		Assert.assertTrue(Arrays.equals(expected, actual));
	}

	@Test
	public void testRotateTwice() {
		FiducialWord[] words = {new FiducialWord("101"), new FiducialWord("101"), new FiducialWord("101")};
		FiducialWord[] expected = {new FiducialWord("101"), new FiducialWord("101"), new FiducialWord("101")};
		FiducialMarker marker = new FiducialMarker(words);
		FiducialWord[] actual = marker.rotate(2);

		Assert.assertTrue(Arrays.equals(expected, actual));
	}

	@Test
	public void testHammingDistance() {
		//1-hamming
		FiducialWord[] data1 = {new FiducialWord("100"), new FiducialWord("100"), new FiducialWord("100")};
		FiducialWord[] data2 = {new FiducialWord("000"), new FiducialWord("000"), new FiducialWord("000")};
		FiducialMarker marker1 = new FiducialMarker(data1);
		FiducialMarker marker2 = new FiducialMarker(data2);
		Assert.assertTrue(marker1.calcHammingDistance(data2) == 1);
		Assert.assertTrue(marker2.calcHammingDistance(data1) == 1);

		//3-hamming
		FiducialWord[] data3 = {new FiducialWord("100"), new FiducialWord("000"), new FiducialWord("001")};
		FiducialWord[] data4 = {new FiducialWord("000"), new FiducialWord("000"), new FiducialWord("010")};
		FiducialMarker marker3 = new FiducialMarker(data3);
		FiducialMarker marker4 = new FiducialMarker(data4);
		Assert.assertTrue(marker3.calcHammingDistance(data4) == 3);
		Assert.assertTrue(marker4.calcHammingDistance(data3) == 3);

		//mismatched data
		FiducialWord[] data5 = {new FiducialWord("100"), new FiducialWord("000"), new FiducialWord("000")};
		FiducialWord[] data6 = {new FiducialWord("000"), new FiducialWord("000")};
		FiducialMarker marker5 = new FiducialMarker(data5);
		Assert.assertTrue(marker5.calcHammingDistance(data6) == -1);
	}

	@Test
	public void testCalcMinHammingDistance() {
		FiducialWord[] data1 = {new FiducialWord("101"), new FiducialWord("101"), new FiducialWord("101")};
		FiducialWord[] data2 = {new FiducialWord("111"), new FiducialWord("111"), new FiducialWord("111")};
		FiducialMarker marker = new FiducialMarker(data1);
		FiducialMarker marker2 = new FiducialMarker(data2);

		Assert.assertEquals(3, marker.calcMinHammingDistance(marker2));
	}

	@Test
	public void testCalcSelfHammingDistance() {
		FiducialWord[] data = {new FiducialWord("100"), new FiducialWord("010"), new FiducialWord("101")};
		FiducialMarker marker = new FiducialMarker(data);

		Assert.assertEquals(2, marker.calcSelfHammingDistance());
	}

	@Test
	public void testImage() {
		FiducialWord[] data = {new FiducialWord("111"), new FiducialWord("101"), new FiducialWord("111")};
		FiducialMarker marker = new FiducialMarker(data);

		Image img = marker.toImage();
		img.getHeight(null);
	}
}
