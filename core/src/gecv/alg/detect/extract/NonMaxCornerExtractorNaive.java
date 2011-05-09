/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.detect.extract;

import gecv.struct.QueueCorner;
import gecv.struct.image.ImageFloat32;
import pja.geometry.struct.point.Point2D_I16;


/**
 * <p/>
 * Extracts corners at local maximums that are above a threshold.  Basic unoptimized implementation.
 * <p/>
 *
 * @author Peter Abeles
 */
public class NonMaxCornerExtractorNaive implements NonMaxCornerExtractor {

	// size of the search area
	protected int radius;
	// the threshold which points must be above to be a feature
	protected float thresh;

	public NonMaxCornerExtractorNaive(int minSeparation, float thresh) {
		this.radius = minSeparation;
		this.thresh = thresh;
	}

	@Override
	public void setMinSeparation(int minSeparation) {
		this.radius = minSeparation;
	}

	@Override
	public void setThresh(float thresh) {
		this.thresh = thresh;
	}

	/**
	 * Detects corners in the image while excluding corners which are already contained in the corners list.
	 *
	 * @param intensityImage Feature intensity image. Can be modified.
	 * @param corners		Where found corners are stored.  Corners which are already in the list will not be added twice.
	 */
	@Override
	public void process(ImageFloat32 intensityImage, QueueCorner corners) {
		// mark corners which have already been found
		for (int i = 0; i < corners.num; i++) {
			Point2D_I16 pt = corners.get(i);
			intensityImage.set(pt.x, pt.y, Float.MAX_VALUE);
		}

		final int imgWidth = intensityImage.getWidth();
		final int imgHeight = intensityImage.getHeight();
		final int stride = intensityImage.stride;

		final float inten[] = intensityImage.data;

		for (int y = radius; y < imgHeight - radius; y++) {
			for (int x = radius; x < imgWidth - radius; x++) {
				int center = intensityImage.startIndex + y * stride + x;

				float val = inten[center];
				if (val < thresh) continue;

				boolean max = true;

				escape:
				for (int i = -radius; i <= radius; i++) {
					int index = center + i * stride - radius;
					for (int j = -radius; j <= radius; j++, index++) {
						// don't compare the center point against itself
						if (i == 0 && j == 0)
							continue;

						if (val <= inten[index]) {
							max = false;
							break escape;
						}
					}
				}

				// add points which are local maximums and are not already contained in the corners list
				if (max && val != Float.MAX_VALUE) {
					corners.add(x, y);
				}
			}
		}
	}
}