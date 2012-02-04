/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.alg.feature.detect.calibgrid;

import georegression.metric.Distance2D_F64;
import georegression.metric.UtilAngle;
import georegression.struct.line.LineParametric2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import pja.sorting.QuickSort_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a set of unordered pixels that form the outside edge of a convex blob.  The first two corner points
 * are found by finding the two points which are farthest part.  The next two points are found by maximizing
 * the number of inlier points between a corner already found and a line between defined by another point
 * in the contour.
 *
 * @author Peter Abeles
 */
public class FindQuadCorners {

	// how close a pixel can be to be considered part of the same line
	double lineTolerance;
	
	// size of the local region for corner refinement
	int refinementRadius;

	/**
	 *
	 * @param lineTolerance Defines how close a point is to be an inlier.  Euclidean distance
	 * @param refinementRadius After candidate corners points have been found they are refined by searching the local
	 *                         region for points which have a smaller acute angle
	 */
	public FindQuadCorners(double lineTolerance , int refinementRadius ) {
		this.lineTolerance = lineTolerance*lineTolerance;
		this.refinementRadius = refinementRadius;
	}

	/**
	 * Finds corners from list of contour points and orders contour points into clockwise order.
	 *
	 * @param contour An unordered list of contour points.  Is modified to be on clockwise order.
	 * @return List of 4 best corner points in clockwise order.
	 */
	public List<Point2D_I32> process( List<Point2D_I32> contour ) {
		// order points in clockwise order
		Point2D_I32 center = findAverage(contour);
		sortByAngleCCW(center, contour);

		// find the first corner
		int corner0 = findFarthest( contour.get(0) , contour );
		// and the second
		int corner1 = findFarthest( contour.get(corner0) , contour );

		// now the other corners are harder
		List<Point2D_I32> corners = new ArrayList<Point2D_I32>();

		corners.add( contour.get(corner0));
		corners.add( contour.get(corner1));

		// find points which maximize the inlier to a line model and are not close to existing points
		int corner2 = findCorner(corner0,corner1,contour,corners);
		int corner3 = findCorner(corner1,corner0,contour,corners);

		// refine the corner estimates by maximizing the acute angle
		corner0 = refineCorner(corner0, refinementRadius,contour);
		corner1 = refineCorner(corner1, refinementRadius,contour);
		corner2 = refineCorner(corner2, refinementRadius,contour);
		corner3 = refineCorner(corner3, refinementRadius,contour);

		corners.clear();
		corners.add(contour.get(corner0));
		corners.add(contour.get(corner1));
		corners.add(contour.get(corner2));
		corners.add(contour.get(corner3));

		// sort the corners to make future calculations easier
		sortByAngleCCW(center, corners);

		return corners;
	}

	/**
	 * Returns the index of the point farthest away from the sample point
	 */
	protected static int findFarthest( Point2D_I32 a , List<Point2D_I32> contour ) {
		int best = -1;
		int index = -1;
		
		for( int i = 0; i < contour.size(); i++ ) {
			Point2D_I32 b = contour.get(i);

			int d = a.distance2(b);
			
			if( d > best ) {
				best = d;
				index = i;
			}
		}
		
		return index;
	}

	/**
	 * Find the average of all the points in the list.
	 *
	 * @param contour
	 * @return
	 */
	protected static Point2D_I32 findAverage(List<Point2D_I32> contour) {

		int x = 0;
		int y = 0;
		
		for( Point2D_I32 p : contour ) {
			x += p.x;
			y += p.y;
		}
		
		x /= contour.size();
		y /= contour.size();

		return new Point2D_I32(x,y);
	}


	/**
	 * Sorts the points in counter clockwise direction around the provided point
	 *
	 * @param center Point that the angle is computed relative to
	 * @param contour List of all the points which are to be sorted by angle
	 */
	protected static void sortByAngleCCW(Point2D_I32 center, List<Point2D_I32> contour) {
		double angles[] = new double[ contour.size() ];
		int indexes[] = new int[ angles.length ];
		
		for( int i = 0; i < contour.size(); i++ ) {
			Point2D_I32 c = contour.get(i);
			int dx = c.x-center.x;
			int dy = c.y-center.y;

			angles[i] = Math.atan2(dy,dx);
		}

		QuickSort_F64 sort = new QuickSort_F64();
		sort.sort(angles,angles.length,indexes);
		
		List<Point2D_I32> sorted = new ArrayList<Point2D_I32>(contour.size());
		for( int i = 0; i < indexes.length; i++ ) {
			sorted.add( contour.get( indexes[i]));
		}
		
		contour.clear();
		contour.addAll(sorted);
	}

	/**
	 * Finds two candidate corners in the CW and CCW direction given two already found corners.
	 * Selects the corner which if farthest away from corners already in the corners list.  Corners
	 * are found by maximizing the number of inliers along a line defined from an existing corner
	 * to a candidate corner.
	 *
	 * @param start First corner point already found
	 * @param stop Second corner point already found.
	 * @param contour List of pixels around the blob
	 * @param corners List of corners already found
	 */
	private int findCorner( int start , int stop , List<Point2D_I32> contour , List<Point2D_I32> corners )
	{
		int candidate0 = findMaxInlier(start,stop,1,contour);
		int candidate1 = findMaxInlier(start,stop,-1,contour);
		
		Point2D_I32 c0 = contour.get(candidate0);
		Point2D_I32 c1 = contour.get(candidate1);
		
		int dist0 = 0, dist1 = 0;
		
		for( Point2D_I32 p : corners ) {
			dist0 += Math.sqrt(p.distance2(c0));
			dist1 += Math.sqrt(p.distance2(c1));
		}

		if( dist0 > dist1 ) {
			corners.add(c0);
			return candidate0;
		} else {
			corners.add(c1);
			return candidate1;
		}
	}

	/**
	 * Finds the line which has the most inliers.  All the lines which begin at 'start' and have the other end
	 * point at 'start' to 'stop' are considered.  The direction through the circular list is specified by dir.
	 *
	 * @param start First index considered and the first end point in the line.
	 * @param stop List index considered
	 * @param dir Direction of stepping through the list
	 * @param contour Circular list of points.
	 * @return
	 */
	private int findMaxInlier(int start, int stop, int dir, List<Point2D_I32> contour) {
		
		int bestCount = -1;
		int bestIndex = 0;
		
		for( int i = start; i != stop; i = incrementCircle(i,dir,contour.size()) ) {

			int count = countInliers(start,i,dir,contour,lineTolerance);
//			System.out.println(" i = "+i+" count = "+count);
			
			if( count > bestCount ) {
				bestCount = count;
				bestIndex = i;
			}
		}
		
		return bestIndex;
	}

	/**
	 * Counts the number of points between start and stop which are within tolerance of
	 * the line defined by the points at start and stop.
	 *
	 * @param start Start of the line
	 * @param stop End of the line
	 * @param dir Direction of increment
	 * @param contour List of all the points in the cyclical list
	 * @param lineTolerance How close a point needs to be to be considered an inlier
	 * @return Number of inliers
	 */
	protected static int countInliers( int start , int stop , int dir ,
									   List<Point2D_I32> contour ,
									   double lineTolerance ) {
		int count = 0;

		Point2D_I32 a = contour.get(start);
		Point2D_I32 b = contour.get(stop);

		LineParametric2D_F64 line = new LineParametric2D_F64(a.x,a.y,b.x-a.x,b.y-a.y);

		Point2D_F64 p = new Point2D_F64();
		for( int i = start; i != stop;  i = incrementCircle(i,dir,contour.size())) {
			Point2D_I32 c = contour.get(i);
			p.x = c.x;
			p.y = c.y;
			
			double d = Distance2D_F64.distanceSq(line,p);
			if( d <= lineTolerance ) {
				count++;
			}
		}
		
		return count;
	}

	/**
	 * Searches the local region around cornerIndex to find the point which has the smallest acute angle
	 * with points locaRadius away from it.
	 *
	 * @param cornerIndex  Center point in area being examined
	 * @param localRadius size of the region being eamined
	 * @param contour list of ordered points
	 * @return index of the best one found
	 */
	protected static int refineCorner( int cornerIndex , int localRadius ,
									   List<Point2D_I32> contour ) {
		int N = contour.size();
		int start = incrementCircle(cornerIndex,-localRadius,N);
		int stop = incrementCircle(cornerIndex,localRadius,N);
		
		double bestAngle = Double.MAX_VALUE;
		int bestIndex = -1;
		
		for( int i = start; i != stop; i = incrementCircle(i,1,N) ) {
			int i0 = incrementCircle(i,-localRadius,N);
			int i1 = incrementCircle(i,localRadius,N);

			Point2D_I32 p = contour.get(i);
			Point2D_I32 p0 = contour.get(i0);
			Point2D_I32 p1 = contour.get(i1);

			double angle0 = Math.atan2(p0.y-p.y,p0.x-p.x);
			double angle1 = Math.atan2(p1.y-p.y,p1.x-p.x);
			
			double acute = UtilAngle.dist(angle0,angle1);
			
			if( acute < bestAngle ) {
				bestAngle = acute;
				bestIndex = i;
			}
		}

		return bestIndex;
	}
	
	
	/**
	 * Returns the next point in the list assuming a cyclical list
	 * @param i current index
	 * @param dir Direction and amount of increment
	 * @param size Size of the list
	 * @return i+dir taking in account the list's cyclical nature
	 */
	public static int incrementCircle( int i , int dir , int size ) {
		i += dir;
		if( i < 0 ) i = size+i;
		else if( i >= size ) i = i - size;
		return i;
	}
}
