	public MultiValueMap<String, String> toMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		if (limit != null) { map.set("limit", String.valueOf(limit)); }
		if (offset != null) { map.set("offset", String.valueOf(offset)); }
		if (since != null) { map.set("since", String.valueOf(since)); }
		if (until != null) { map.set("until", String.valueOf(until)); }
		if (after != null) { map.set("after", after); }
		if (before != null) { map.set("before", before); }
		if (pagingToken != null) { map.set("__paging_token", pagingToken); }
		return map;
	}