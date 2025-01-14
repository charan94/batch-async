/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skanl.batch.persistence;

/**
 * @author Mahmoud Ben Hassine
 */
public class JobInstance {

	private String id;

	private Long jobInstanceId;

	private String jobName;

	private String jobKey;

	public JobInstance() {
	}

	public String getId() {
		return id;
	}

	public Long getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(Long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobKey() {
		return jobKey;
	}

	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}

	@Override
	public String toString() {
		return "JobInstance{" +
				"id='" + id + '\'' +
				", jobInstanceId=" + jobInstanceId +
				", jobName='" + jobName + '\'' +
				", jobKey='" + jobKey + '\'' +
				'}';
	}
}
