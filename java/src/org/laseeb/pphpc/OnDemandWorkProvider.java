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

import java.util.concurrent.atomic.AtomicInteger;

public class OnDemandWorkProvider implements IWorkProvider {

	/**
	 * A class which represents the state of on-demand work performed by 
	 * each worker.
	 */
	private class OnDemandWork extends AbstractWork {

		private int current;
		private int last;
		
		public OnDemandWork(int wId) {
			super(wId);
			this.current = 0;
			this.last = 0;
		}
	}
	
	private AtomicInteger counter;
	private int blockSize;
	private int workSize;

	public OnDemandWorkProvider(int blockSize, int workSize) {
		this.counter = new AtomicInteger(0);
		this.blockSize = blockSize;
		this.workSize = workSize;
	}

	@Override
	public IWork newWork(int wId) {
		return new OnDemandWork(wId);
	}

	@Override
	public int getNextToken(IWork work) {
		
		OnDemandWork odWork = (OnDemandWork) work;
		
		/* Set the nextToken to -1, which means no more work
		 * is available. */
		int nextIndex = -1;
		
		if (odWork.current >= odWork.last) {

			odWork.current = this.counter.getAndAdd(this.blockSize);
			odWork.last = Math.min(odWork.current + this.blockSize, this.workSize);
			
		}
		
		if (odWork.current < this.workSize) {

			nextIndex = odWork.current;
			odWork.current++;
			
		}

		return nextIndex;
	}

	@Override
	public void resetWork(IWork work) {
		
		OnDemandWork odWork = (OnDemandWork) work;
		odWork.current = 0;
		odWork.last = 0;

	}

	void resetWorkCounter() {
		this.counter.set(0);
	}
}
