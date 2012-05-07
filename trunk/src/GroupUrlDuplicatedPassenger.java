public enum GroupUrlDuplicatedPassenger {

	GUDP("A01051923", 728);

	private String groupUrl;
	private Integer totalRecords;

	private GroupUrlDuplicatedPassenger(final String groupUrl, final Integer totalRecords) {
		this.groupUrl = groupUrl;
		this.totalRecords = totalRecords;
	}

	public String getGroupUrl() {
		return this.groupUrl;
	}

	public Integer getTotalRecords() {
		return this.totalRecords;
	}

	public static Integer getTotalRecords(final String groupUrl, final Integer totalRecords) {
		for (final GroupUrlDuplicatedPassenger gudp : values()) {
			if (gudp.getGroupUrl().equals(groupUrl)) {
				return gudp.getTotalRecords();
			}
		}

		return totalRecords;
	}

	public static String getTotalRecords(final String groupUrl, final String totalRecords) {
		return getTotalRecords(groupUrl, Integer.valueOf(totalRecords)).toString();
	}
}