package com.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

public class Comment {
	
	private List<String> comments;

	public Comment() {
		this.comments = new ArrayList<>();
	}
	
	public void add(final String comment) {
		this.comments.add(comment);
	}

	@Override
	public String toString() {
		return "Comments " + comments + "]";
	}


}
