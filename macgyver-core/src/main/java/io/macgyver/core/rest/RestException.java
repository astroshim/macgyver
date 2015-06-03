/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.core.rest;

import io.macgyver.core.MacGyverException;

@Deprecated
public class RestException extends MacGyverException {

	int statusCode;
	
	public RestException(Exception e) {
		super(e);
		statusCode=400;
	}
	public RestException(int statusCode) {
		super("status: "+statusCode);
		this.statusCode = statusCode;
	}
	
	public RestException(int statusCode, String message) { 
		super("status: " + statusCode + "; message: " + message);
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
}
