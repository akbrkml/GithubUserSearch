package com.akbrkml.githubusersearch.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ResponseAPI{

	@SerializedName("total_count")
	private int totalCount;

	@SerializedName("incomplete_results")
	private boolean incompleteResults;

	@SerializedName("items")
	private List<UsersItem> items;

	public void setTotalCount(int totalCount){
		this.totalCount = totalCount;
	}

	public int getTotalCount(){
		return totalCount;
	}

	public void setIncompleteResults(boolean incompleteResults){
		this.incompleteResults = incompleteResults;
	}

	public boolean isIncompleteResults(){
		return incompleteResults;
	}

	public void setItems(List<UsersItem> items){
		this.items = items;
	}

	public List<UsersItem> getItems(){
		return items;
	}
}