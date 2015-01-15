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

public class SingleThreadWorkProvider implements IWorkProvider {
	
	/**
	 * A class which represents the state of work performed by 
	 * the single available worker.
	 */
	private class SingleThreadWork extends AbstractWork {

		int counter;
		
		public SingleThreadWork(int wId) {
			super(wId);
			this.counter = 0;
		}
		
	}
	
	private int workSize;

	public SingleThreadWorkProvider(int workSize) {
		this.workSize = workSize;
	}

	@Override
	public IWork newWork(int wId) {
		return new SingleThreadWork(wId);
	}

	@Override
	public int getNextToken(IWork work) {

		/* Set the nextToken to -1, which means no more work
		 * is available. */
		int token = -1;
		
		SingleThreadWork stWork = (SingleThreadWork) work;
		if (stWork.counter < this.workSize) {
			token = stWork.counter;
			stWork.counter++;
		}
		return token;
	}

	@Override
	public void resetWork(IWork work) {
		SingleThreadWork stWork = (SingleThreadWork) work;
		stWork.counter = 0;
	}

}
