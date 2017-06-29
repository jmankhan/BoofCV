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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by Jalal on 6/3/2017.
 */
public class TestFiducialMarkerGenerator {

	@Test
	public void testBitTransitions() {
		FiducialDictionary dict = new FiducialDictionary(3, 3);
		FiducialMarkerGenerator.generate(dict);
		System.out.println(dict.getSize());
		assertTrue(dict.getSize() > 0);
		for(FiducialMarker marker : dict.getBackingArray()) {
			Image i = marker.toImage();
			i.toString();

		}
	}
}
