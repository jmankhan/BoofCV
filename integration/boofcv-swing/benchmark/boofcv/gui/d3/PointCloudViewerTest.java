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

package boofcv.gui.d3;

import boofcv.io.UtilIO;
import boofcv.io.calibration.CalibrationIO;
import boofcv.struct.calib.CameraPinholeRadial;
import org.ddogleg.struct.FastQueue;
import org.ejml.data.DMatrix;
import org.ejml.data.DMatrixRMaj;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by Jalal on 5/29/2017.
 */
public class PointCloudViewerTest {

	private JFrame frame;
	private PointCloudViewer viewer;
	private Random random;
	private int w, h, d;

	@Before
	public void setUp() throws Exception {
		frame = new JFrame();
		random = new Random();
		w = 1280;
		h = 720;
		d = 100;

		CameraPinholeRadial intrinsic =
				CalibrationIO.load(UtilIO.pathExample("calibration/mono/Sony_DSC-HX5V_Chess/intrinsic.yaml"));

		viewer = new PointCloudViewer(intrinsic, .01);


		frame.setPreferredSize(new Dimension(w, h));
		frame.add(viewer);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}


	@Test
	public void addPoint() throws Exception {
		for(int i=0; i<5000000; i++)
			viewer.addPoint(random.nextInt(w), random.nextInt(h), random.nextInt(d), Color.RED.getRGB());
	}

	@Test
	public void getPoint() throws Exception {
		for(int i=0; i<5000000; i++)
			viewer.addPoint(random.nextInt(w), random.nextInt(h), random.nextInt(d), Color.RED.getRGB());

		long start = System.currentTimeMillis();
		FastQueue cloud = viewer.getCloud();
		for(Object o : cloud.toList()) {
			String s = o.toString();
		}

		long end = System.currentTimeMillis();
		System.out.println("DEBUG: getPoint() took " + (end - start) + " MilliSeconds");

	}

	@Test
	public void view() throws Exception {
		System.out.println("limos");
	}

	@After
	public void tearDown() throws Exception {
		viewer = null;
		random = null;
		w = -1;
		h = -1;
		d = -1;
	}
}