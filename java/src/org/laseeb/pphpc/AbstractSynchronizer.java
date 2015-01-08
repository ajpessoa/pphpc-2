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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract simulation synchronizer which implements the IObservable
 * interface.
 * 
 * @author Nuno Fachada
 */
public abstract class AbstractSynchronizer implements ISynchronizer {

	/* List of observers. */
	private List<IObserver> observers;
	
	/* Simulation event associated with this synchronizer. */
	private SimEvent event;
	
	/**
	 * Constructor called by concrete implementations.
	 * 
	 * @param event Simulation event to associate with this simulation
	 * synchronizer. 
	 */
	public AbstractSynchronizer(SimEvent event) {
		this.event = event;
		this.observers = new ArrayList<IObserver>();
	}
	
	/**
	 * @see IObservable#registerObserver(IObserver)
	 */
	@Override
	public void registerObserver(IObserver observer) {
		this.observers.add(observer);
	}

	/**
	 * Helper method which notifies the registered observers.
	 */
	protected void notifyObservers(IModel model) {
		for (IObserver o : this.observers) {
			o.update(this.event, model);
		}
	}
}
