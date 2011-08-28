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

package gecv.alg.transform.ii.impl;

import gecv.alg.transform.ii.SparseIntegralGradient_NoBorder;
import gecv.struct.deriv.GradientValue_I32;
import gecv.struct.image.ImageSInt32;


/**
 * Computes the gradient from an integral image.  Does not check for border conditions.
 *
 * @author Peter Abeles
 */
public class SparseIntegralGradient_NoBorder_I32
		extends SparseIntegralGradient_NoBorder<ImageSInt32, GradientValue_I32>
{

	private GradientValue_I32 ret = new GradientValue_I32();

	public SparseIntegralGradient_NoBorder_I32(int radius) {
		super(radius);
	}

	@Override
	public GradientValue_I32 compute(int x, int y) {

		int horizontalOffset = x-r-1;
		int indexSrc1 = ii.startIndex + (y-r-1)*ii.stride + horizontalOffset;
		int indexSrc2 = ii.startIndex + (y-1)*ii.stride + horizontalOffset;
		int indexSrc3 = ii.startIndex + y*ii.stride + horizontalOffset;
		int indexSrc4 = ii.startIndex + (y+r)*ii.stride + horizontalOffset;

		int p0 = ii.data[indexSrc1];
		int p1 = ii.data[indexSrc1+r];
		int p2 = ii.data[indexSrc1+r+1];
		int p3 = ii.data[indexSrc1+w];
		int p11 = ii.data[indexSrc2];
		int p4 = ii.data[indexSrc2+w];
		int p10 = ii.data[indexSrc3];
		int p5 = ii.data[indexSrc3+w];
		int p9 = ii.data[indexSrc4];
		int p8 = ii.data[indexSrc4+r];
		int p7 = ii.data[indexSrc4+r+1];
		int p6 = ii.data[indexSrc4+w];

		int left = p8-p9-p1+p0;
		int right = p6-p7-p3+p2;
		int top = p4-p11-p3+p0;
		int bottom = p6-p9-p5+p10;

		ret.x = right-left;
		ret.y = bottom-top;

		return ret;
	}
}