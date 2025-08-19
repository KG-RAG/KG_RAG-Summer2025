package com.warmer.web.request;

import lombok.Data;
import java.util.List;

@Data
public class ImportTriplesRequest {
	private String domain;
	private String source = "agent";
	private List<TripleItem> triples;
	
	@Data
	public static class TripleItem {
		private String subject;
		private String predicate;
		private String object;
		private Double confidence;
		private String subjectType;
		private String objectType;
	}
} 