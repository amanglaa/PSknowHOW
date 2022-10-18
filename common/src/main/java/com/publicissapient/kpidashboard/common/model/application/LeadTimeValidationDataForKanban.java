package com.publicissapient.kpidashboard.common.model.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Lead time validation data to show in excel.
 */

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LeadTimeValidationDataForKanban {

	private String issueNumber;
	private String intakeDate;
	private String triageDate;
	private String completedDate;
	private String liveDate;
}
