// Replace with new format if vertical Ids not present or collection is empty 
if( db.getCollection('global_config').find({}).count() == 0 || (
 db.getCollection('global_config').find({ "jiraFieldMapping": { $exists: false } }).count() >0)){
			
	        db.getCollection('global_config').remove({});
			db.getCollection('global_config').insert(
			[
			{
			   
				"env": "dojo",    
				"jiraFieldMapping": {
						"teamName": "",
						"sprintName": "customfield_12700",
						"epicName": "",
						"jiraBackLogStatusLabel": "",
						"jiradefecttype": ["Defect"],
						"jiraIssueTypeNames": [
							"Story",
							"Defect"
						],
						"storyFirstStatus": "Open",
						"linkDefectToStoryField": [
							"discovered while testing",
							"relates to",
							"testing discovered",
							"blocks",
							"belongs to",
							"is blocked by"
						],
						"jiraIssueTypeId": "Defect,Story",
						"jiraDoneStatus": [
							"Done",
							"Closed",
							"Resolved"
						],
						"doingStatuses": [
							"Open",
							"In Analysis",
							"Ready For Testing",
							"In Testing",
							"In Development",
							"In Progress"
						],
						"envImpacted": "",
						"rootCause": "",
						"testAutomated": "",
						"jiraTestCaseType": [
							""
						],
						"jiraDefectInjectionIssueType": [
							"Defect"
						],
						"jiraDod": [
							"Closed"
						],
						"jiraDefectCreatedStatus": "Open",
						"jiraTechDebtIssueType": [
							"Story"
						],
						"jiraTechDebtIdentification": "IssueType/Label",
						"jiraTechDebtCustomField": "",
						"jiraTechDebtValue": [
							"Story"
						],
						"jiraDefectRejectionStatus": "Rejected",
						"jiraBugRaisedByIdentification": "CustomField",
						"jiraBugRaisedByValue": [
							""
						],
						"jiraDefectSeepageIssueType": [
							"Defect"
						],
						"jiraBugRaisedByCustomField": "customfield_15001",
						"jiraDefectRemovalStatus": [
							"Closed"
						],
						"jiraDefectRemovalIssueType": [
							"Defect"
						],
						"jiraBufferedEstimationCustomField": "",
						"jiraStoryPointsCustomField": "customfield_10002",
						"jiraTestAutomationIssueType": [
							"Story",
							"Defect"
						],
						"jiraAutomatedTestValue": [
							""
						],
						"jiraCanNotAutomatedTestValue": [
							""
						],
						"jiraSprintVelocityIssueType": [
							"Story",
							"Defect"
						],
						"jiraDefectRejectionlIssueType": [
							"Defect"
						],
						"jiraDefectCountlIssueType": [
							"Defect"
						],
						"jiraIssueDeliverdStatus": [
							"Closed",
							"Resolved"
						],
						"jiraDor": "Ready for Delivery",
						"jiraIntakeToDorIssueType": [
							"Story",
							"Defect"
						],
						"jiraStoryIdentification": [
							"Story",
							"Defect"
						],
						"jiraLiveStatus": "Closed ",
						"jiraSubProjectCustomField": ""
					}
			}
			]
			);
}