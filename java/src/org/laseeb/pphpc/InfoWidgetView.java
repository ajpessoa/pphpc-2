/*
 * Copyright (c) 2015, Nuno Fachada
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the Instituto Superior Técnico nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.laseeb.pphpc;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class InfoWidgetView extends AbstractModelEventObserver implements IView {

	private JLabel label;
	private JProgressBar progressBar;
	private IModelQuerier model;

	private void createAndShowGUI() {
		
		/* Create and set up the window. */
		JFrame frame = new JFrame("Predator-Prey HPC");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		/* Add the label. */
		this.label = new JLabel("No iterations");
		frame.getContentPane().add(this.label, BorderLayout.NORTH);

		/* Add the progress bar. */
		this.progressBar = new JProgressBar(0, model.getParams().getIters());
		this.progressBar.setValue(0);
		this.progressBar.setStringPainted(true);
		frame.getContentPane().add(this.progressBar, BorderLayout.CENTER);

		/* Display the window. */
		frame.pack();
		frame.setVisible(true);
	}

	public InfoWidgetView() {}

	@Override
	public void init(IModelQuerier model, IController controller, PredPrey pp) {

		this.model = model;
		model.registerObserver(ModelEvent.START, this);
		model.registerObserver(ModelEvent.STOP, this);
		model.registerObserver(ModelEvent.NEW_ITERATION, this);
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		
	}

	@Override
	public void updateOnNewIteration() {
		/* Get current iteration within allowable time. */
		final int iter = model.getCurrentIteration();

		/* Then enqueue widget update. */
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setText("Iter: " + iter);
				progressBar.setValue(iter);
			}
		});
	}

	@Override
	public ViewType getType() {
		return ViewType.PASSIVE;
	}


}
