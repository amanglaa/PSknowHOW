db.getCollection('kpi_master').remove({});
db.getCollection('kpi_master').insert(
[
  {
    "kpiId": "kpi14",
    "kpiName": "Defect Injection Rate",
    "maxValue": "200",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT INJECTION RATE measures the total number of defects (bugs) detected for a story",
      "formula": [
        {
          "lhs": "DIR for a sprint",
          "operator": "division",
          "operands": [
            "No. of defects tagged to all stories closed in a sprint",
            "Total no. of stories closed in the sprint"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect injection rate between last 2 sprints. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "25%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "75-25%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "125%-75%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "175% -125%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 175%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-175","175-125","125-75","75-25","25-"]
  },
  {
    "kpiId": "kpi82",
    "kpiName": "First Time Pass Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "FIRST TIME PASS RATE measures the percentage of tickets that pass QA first time (without stimulating a return transition or defect tagged)",
      "formula": [
        {
          "lhs": "First time pass rate (FTPR) for a Sprint",
          "operator": "division",
          "operands": [
            "No. of issues closed in a sprint which do not have a return transition or any defects tagged",
            "Total no. of issues closed in the sprint"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of first time pass rate between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">=90%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": ">=75-90%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": ">=50-75%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": ">=25-50%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 25%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-25","25-50","50-75","75-90","90-"]
  },
  {
    "kpiId": "kpi111",
    "kpiName": "Defect Density",
    "maxValue": "500",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT DENSITY measures the total number of defects against the size of a story",
      "formula": [
        {
          "lhs": "Defect Density",
          "operator": "division",
          "operands": [
            "No. of defects tagged to all stories closed in a sprint",
            "Total size of stories closed in the sprint"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "The KPI is applicable only if the estimation is being done in 'STory Points'"
        },
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect density between last 2 sprints. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<10%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "10%-25%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "25%-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "60% -90%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">90%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-90","90-60","60-25","25-10","10-"]
  },
  {
    "kpiId": "kpi35",
    "kpiName": "Defect Seepage Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT SEEPAGE RATE measures the percentage of defects leaked from the current testing stage to the subsequent stage",
      "formula": [
        {
          "lhs": "DSR for a sprint",
          "operator": "division",
          "operands": [
            "No. of  valid defects reported at a stage (e.g. UAT)",
            " Total no. of defects reported in the current stage and previous stage (UAT & QA)"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Lesser the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect seepage rate between last 2 sprints. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<25%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": ">=25-50%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": ">=50-75%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": ">=75-90%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">=90%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-90","90-75","75-50","50-25","25-"]
  },
  {
    "kpiId": "kpi34",
    "kpiName": "Defect Removal Efficiency",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "90",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT REMOVAL EFFICIENCY gives a measure of the development team ability to remove defects prior to release",
      "formula": [
        {
          "lhs": "DRE for a sprint",
          "operator": "division",
          "operands": [
            "No. of defects fixed in a sprint",
            "Total no. of defects reported in a sprint"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect removal efficiency between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">=90%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": ">=75-90%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": ">=50-75%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": ">=25-50%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "<25%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-25","25-50","50-75","75-90","90-"]
  },
  {
    "kpiId": "kpi37",
    "kpiName": "Defect Rejection Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT REJECTION RATE measures the effectiveness of a testing team",
      "formula": [
        {
          "lhs": "DRR for a sprint",
          "operator": "division",
          "operands": [
            "No. of defects rejected in a sprint",
            "Total no. of defects reported in a sprint"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Lesser the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect injection rate between last 2 sprints. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<10%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": ">=10-30%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": ">=30-50%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": ">=50-75%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">=75%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-75","75-50","50-30","30-10","10-"]
  },
  {
    "kpiId": "kpi28",
    "kpiName": "Defect Count By Priority",
    "maxValue": "90",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT COUNT BY PRIORITY measures number of defects for each priority defined in a project",
      "formula": [
        {
          "lhs": "Defect Count By Priority"
        },
        {
          "rhs": "No. of defects linked to stories grouped by priority"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as ‘Count’. Lower the count, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect count by priority between last 2 sprints. A downward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi36",
    "kpiName": "Defect Count By RCA",
    "maxValue": "100",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiSource": "Jira",
    "groupId": 3,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEFECT COUNT BY RCA measures number of defects along with the root cause of defects",
      "formula": [
        {
          "lhs": "It is calculated as",
          "rhs": "No. of defects linked to stories grouped by Root Cause"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as ‘Count’. Lower the count, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of defect count by RCA between last 2 sprints. A downward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi126",
    "kpiName": "Created vs Resolved defects",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "Created vs Resolved defects gives a view of closed defects in an iteration vs planned + added defects in the iteration. The aim is to close all the defects that are in the iteration.",
      "details": [
        {
          "type": "paragraph",
          "value": "If the No. of defects resolved are equal to the No. of defects created in the latest sprint, the KPI is considered having a positive trend."
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "isTrendCalculative": true,
    "trendCalculation": [
      {
        "type": "Upwards",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "<"
      },
      {
        "type": "Neutral",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "<"
      },
      {
        "type": "Downwards",
        "lhs": "value",
        "erhs": "lineValue",
        "operator": ">"
      }
    ],
    "showTrend": true,
    "lineLegend": "Resolved Defects",
    "barLegend": "Created Defects",
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "maxValue": "300",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi42",
    "kpiName": "Regression Automation Coverage",
    "isDeleted": "False",
    "defaultOrder": 10,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "REGRESSION AUTOMATION COVERAGE measures progress of automation of regression test cases",
      "formula": [
        {
          "lhs": "Regression Automation Coverage ",
          "operator": "division",
          "operands": [
            "No. of regression test cases automated",
            "Total no. of regression test cases"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of regression automation coverage between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">= 80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 20%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi16",
    "kpiName": "In-Sprint Automation Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiSource": "Zypher",
    "groupId": 1,
    "thresholdValue": "80",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "IN-Sprint AUTOMATION COVERAGE measures progress of automation of test cases created within the Sprint",
      "formula": [
        {
          "lhs": "In-Sprint Automation Coverage ",
          "operator": "division",
          "operands": [
            "No. of in-sprint test cases automated",
            "Total no. of in-sprint test cases created"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of in-sprint automation coverage between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">= 80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 20%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi17",
    "kpiName": "Unit Test Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 12,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "UNIT TEST COVERAGE is a measurement of the amount of code that is run by unit tests - either lines, branches, or methods.",
      "formula": [
        {
          "lhs": "The calculation is done directly in Sonarqube"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Unit test coverage between last 2 weeks. An upward trend is considered positive"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "<20%"
        }
      ]
    },
    "yAxisLabel": "Percentage",
    "xAxisLabel": "Weeks",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi38",
    "kpiName": "Sonar Violations",
    "maxValue": "",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 13,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "SONAR VIOLATIONS highlight count of issues that exist every time a piece of code breaks a coding rule. The set of coding rules is defined through the associated Quality profile for each programming language in the project.",
      "formula": [
        {
          "lhs": "Issues are categorized in 3 types: Bug, Vulnerability and Code Smells"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "All these issues are categorized into 5 type of severity: Blocker, Critical, Major, Minor, Info   "
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Unit test coverage between last 2 weeks. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi27",
    "kpiName": "Sonar Tech Debt",
    "maxValue": "90",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "SONAR TECH DEBT explains the estimated time required to fix all maintainability Issues/ code smells",
      "formula": [
        {
          "lhs": "It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day=8 Hours."
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Days’. Lower the days, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Sonar Tech Debt between last 2 weeks. A downward trend is considered positive."
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<10 days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "10-30 days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "30-50 days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "50-100 days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">100 days"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-100","100-50","50-30","30-10","10-"]
  },
  {
    "kpiId": "kpi116",
    "kpiName": "Change Failure Rate",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 15,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "CHANGE FAILURE RATE measures the proportion of builds that have failed for whatever reason over a given period of time",
      "formula": [
        {
          "lhs": "CHANGE FAILURE RATE",
          "operator": "division",
          "operands": [
            "Total number of failed Builds",
            "Total number of Builds"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Lower the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Change Failure Rate between last 2 weeks. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 weeks, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<10%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": ">=10-20%,"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": ">=20-30%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": ">=30-50%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">50%"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-50","50-30","30-20","20-10","10-"]
  },
  {
    "kpiId": "kpi70",
    "kpiName": "Test Execution and pass percentage",
    "isDeleted": "False",
    "defaultOrder": 16,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "TEST EXECUTION AND PASS PERCENTAGE measures the percentage of test cases that have been executed & the percentage that have passed.",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as "
        },
        {
          "type": "paragraph",
          "value": "No. of executed test cases out of total test cases in the latest execution of a sprint"
        },
        {
          "type": "paragraph",
          "value": "No. of pass test cases out of executed test cases in  the latest execution of a sprint"
        },
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Unit test coverage between last 2 Sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "> 80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 20%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "lineLegend": "Passed",
    "barLegend": "Executed",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi40",
    "kpiName": "Story Count",
    "maxValue": "",
    "kpiUnit": "Stories",
    "isDeleted": "False",
    "defaultOrder": 17,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "STORY COUNT measures the overall work taken in a sprint",
      "formula": [
        {
          "lhs": "No. of stories tagged to a Sprint"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi72",
    "kpiName": "Commitment Reliability",
    "maxValue": "200",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 18,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "10",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Commitment reliability measures the percentage work completed at the end of a sprint in comparison to the total work in the sprint",
      "formula": [
        {
          "lhs": "Commitment reliability",
          "operator": "division",
          "operands": [
            "No. of issues or Size of issues completed",
            "No. of issues or Size of issues committed"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a 'Percentage'. Higher the percentage during a sprint, better forecasting can be done for future sprints"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Commitment reliability between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        }
      ],
      "maturityLevels": [
        {
          "level": "M1",
          "bgColor": "#6cab61",
          "range": "<40%"
        },
        {
          "level": "M2",
          "bgColor": "#AEDB76",
          "range": "40-60%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "60%-75%"
        },
        {
          "level": "M4",
          "bgColor": "#ffc35b",
          "range": "75% -90%"
        },
        {
          "level": "M5",
          "bgColor": "#F06667",
          "range": "> 90%"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "kpiFilter": "radioButton",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "maturityRange": ["-40","40-60","60-75","75-90","90-"]
  },
  {
    "kpiId": "kpi39",
    "kpiName": "Sprint Velocity",
    "maxValue": "300",
    "kpiUnit": "SP",
    "isDeleted": "False",
    "defaultOrder": 19,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "SPRINT VELOCITY measures the rate at which a team can deliver every Sprint",
      "formula": [
        {
          "lhs": "Sum of story points of all stories completed within a Sprint"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi46",
    "kpiName": "Sprint Capacity Utilization",
    "maxValue": "500",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 20,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "SPRINT CAPACITY UTILIZATION depicts the maximum amount of time a team can commit within sprint",
      "details": [
        {
          "type": "paragraph",
          "value": "This KPI is calculated based on 2 parameters"
        },
        {
          "type": "paragraph",
          "value": "Estimated Hours: It explains the total hours required to complete Sprint backlog. The capacity is defined in KnowHOW"
        },
        {
          "type": "paragraph",
          "value": "Logged Work: The amount of time team has logged within a Sprint. It is derived as sum of all logged work against issues tagged to a Sprint in Jira"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Hours",
    "lineLegend": "Logged",
    "barLegend": "Estimated",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi83",
    "kpiName": "Average Resolution Time",
    "maxValue": "100",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 21,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "AVERAGE RESOLUTION TIME measures average time taken to complete an issue that could be a story or bug etc.",
      "formula": [
        {
          "lhs": "Sum of resolution times of all issues completed in the Sprint/No. of issues completed within a sprint"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Days’. Fewer the days better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Average Resolution Time between last 2 sprints. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 sprints"
        },
        {
          "type": "paragraph",
          "value": "*If the KPI data is not available for last 5 sprints, the Maturity level will not be shown"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<= 3 days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "3-5 days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "5-8 days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "8-10 days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "=> 10 days"
        }
      ]
    },
    "xAxisLabel": "Sprints",
    "yAxisLabel": "Count(Days)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-10","10-8","8-5","5-3","3-"]
  },
  {
    "kpiId": "kpi84",
    "kpiName": "Mean Time To Merge",
    "maxValue": "10",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 22,
    "groupId": 1,
    "kpiSource": "BitBucket",
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "MEAN TIME TO MERGE measures the efficiency of the code review process in a team",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Hours’. Fewer the Hours better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Mean time to merge in last 2 weeks. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of the last 5 weeks"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<4 Hours"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "4-8 Hours"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "8-16 Hours"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "16-48 Hours"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">48 Hours"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count(Hours)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-16","16-8","8-4","4-2","2-"]
  },
  {
    "kpiId": "kpi11",
    "kpiName": "Check-Ins & Merge Requests",
    "maxValue": "10",
    "kpiUnit": "MRs",
    "isDeleted": "False",
    "defaultOrder": 23,
    "kpiSource": "BitBucket",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": false,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down. NUMBER OF MERGE REQUESTS when looked at along with commits highlights the efficiency of the review process",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "> 16"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "8-16"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "4-8"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "2-4"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "0-2"
        }
      ]
    },
    "xAxisLabel": "Days",
    "yAxisLabel": "Count",
    "lineLegend": "Merge Requests",
    "barLegend": "Commits",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-2","2-4","4-8","8-16","16-"]
  },
  {
    "kpiId": "kpi8",
    "kpiName": "Code Build Time",
    "maxValue": "100",
    "kpiUnit": "min",
    "isDeleted": "False",
    "defaultOrder": 24,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "CODE BUILD TIME measures the time a job takes to build the code.",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in  ‘Mins’. Lesser the time better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Code Build Time between last 2 weeks. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the average of 5 weeks"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<5 mins"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "5-15 mins"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "15-30 mins"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": " 30-45 mins"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">45 mins"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count(Mins)",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-45","45-30","30-15","15-5","5-"]
  },
  {
    "kpiId": "kpi3",
    "kpiName": "Lead Time",
    "isDeleted": "False",
    "kpiInAggregatedFeed": "True",
    "kpiOnDashboard": [
      "Aggregated"
    ],
    "kpiBaseLine": "0",
    "thresholdValue": "",
    "defaultOrder": 25,
    "kpiUnit": "Days",
    "kpiSource": "Jira",
    "groupId": 3,
    "kanban": false,
    "chartType": "table",
    "kpiInfo": {
      "definition": "LEAD TIME is the time from the moment when the request was made by a client and placed on a board to when all work on this item is completed and the request was delivered to the client.",
      "formula": [
        {
          "lhs": "It is calculated as the sum Ideation time, Development time & Release time"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "Ideation time (Intake to DOR): Time taken from issue creation to it being ready for Sprint."
        },
        {
          "type": "paragraph",
          "value": "Development time (DOR to DOD): Time taken from start of work on an issue to it being completed in the Sprint as per DOD."
        },
        {
          "type": "paragraph",
          "value": "Release time (DOD to Live): Time taken between story completion to it going live."
        },
        {
          "type": "paragraph",
          "value": "Each of the KPIs are calculated in 'Days' . Lower the time, better is the speed & efficiency of that phase"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 2 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "2-5 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "5-15 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "15-30 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 30 Days"
        },
        {
          "level": "DOD to Live"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 3 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "3-7 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "7-10 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "10-20 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 20 Days"
        },
        {
          "level": "DOR to DOD"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 5 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "5-10 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "10-20 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-30 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 30 Days"
        },
        {
          "level": "Intake to DOR"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 10 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "10-30 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "30-45 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "45-60 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 60 Days"
        },
        {
          "level": "Lead Time"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "kpiFilter": "radioButton",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-60","60-45","45-30","30-10","10-"],

     	"maturityLevel": [{
     			"level": "LeadTime",
     			"range": ["-60", "60-45", "45-30", "30-10", "10-"]
     		},
     		{
     			"level": "Intake-DoR",
     			"range": ["-30", "30-20", "20-10", "10-5", "5-"]
     		},
     		{
     			"level": "DoR-DoD",
     			"range": ["-20", "20-10", "10-7", "7-3", "3-"]
     		},
     		{
     			"level": "DoD-Live",
     			"range": ["-30", "30-15", "15-5", "5-2", "2-"]
     		}
     	]
     	},
  {
    "kpiId": "kpi118",
    "kpiName": "Deployment Frequency",
    "maxValue": "100",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 26,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": 0,
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "DEPLOYMENT FREQUENCY measures how often code is deployed to production",
      "formula": [
        {
          "lhs": "DEPLOYMENT FREQUENCY for a month",
          "rhs": "It is calculated as No. of deployments done on a environment in a month"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a 'Count'. Higher the count during a month, more valuable it is for the Business or a Project"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Deployment frequency between last 2 months. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">10"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "5-10"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "2-5"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "1-2"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "<1"
        }
      ]
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-1","1-2","2-5","5-10","10-"]
   },
  {
    "kpiId": "kpi73",
    "kpiName": "Release Frequency",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 27,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Release Frequency highlights the number of releases done in a month",
      "formula": [
        {
          "lhs": "Release Frequency for a month",
          "rhs": "Number of fix versions in JIRA for a project that have a release date falling in a particular month"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Count’. Higher the Release Frequency, more valuable it is for the Business or a Project"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Release Frequency between last 2 months. An upward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi113",
    "kpiName": "Value delivered (Cost of Delay)",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 28,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Cost of delay (CoD) is a prioritization framework that helps a business quantify the economic value of completing a project sooner as opposed to later.",
      "formula": [
        {
          "lhs": "COD for a Epic or a Feature",
          "rhs": "User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement."
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Days’. Higher the CoD for a feature or an Epic, more valuable it is for the Business or a Project"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of CoD between last 2 months. An upward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count(Days)",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi55",
    "kpiName": "Ticket Open vs Closed rate by type",
    "kpiUnit": "Tickets",
    "defaultOrder": 1,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "Ticket open vs closed rate by type gives a comparison of new tickets getting raised vs number of tickets getting closed grouped by issue type during a defined period.",
      "details": [
        {
          "type": "paragraph",
          "value": "This can be filtered based on issue type"
        },
        {
          "type": "paragraph",
          "value": "If the No. of tickets closed are more than the No. of tickets opened in the latest time period, the KPI is considered having a positive trend."
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": true,
    "trendCalculation": [
      {
        "type": "Upwards",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "<"
      },
      {
        "type": "Neutral",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "="
      },
      {
        "type": "Downwards",
        "lhs": "value",
        "erhs": "lineValue",
        "operator": ">"
      }
    ],
    "showTrend": true,
    "lineLegend": "Closed Tickets",
    "barLegend": "Open Tickets",
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi54",
    "kpiName": "Ticket Open vs Closed rate by Priority",
    "kpiUnit": "Tickets",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "Ticket open vs closed rate by priority gives a comparison of new tickets getting raised vs number of tickets getting closed grouped by priority during a defined period.",
      "details": [
        {
          "type": "paragraph",
          "value": "This can be filtered based on priority."
        },
        {
          "type": "paragraph",
          "value": "If the No. of tickets closed are more than the No. of tickets opened in the latest time period, the KPI is considered having a positive trend."
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": true,
    "trendCalculation": [
      {
        "type": "Upwards",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "<"
      },
      {
        "type": "Neutral",
        "lhs": "value",
        "rhs": "lineValue",
        "operator": "="
      },
      {
        "type": "Downwards",
        "lhs": "value",
        "erhs": "lineValue",
        "operator": ">"
      }
    ],
    "showTrend": true,
    "lineLegend": "Closed Tickets",
    "barLegend": "Open Tickets",
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi50",
    "kpiName": "Net Open Ticket Count by Priority",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "NET OPEN TICKET COUNT BY PRIORITY measures the overall open tickets during a defined period grouped by priority. It considers the gross open and closed count during a period and then plots the net count",
      "formula": [],
      "details": [
        {
          "type": "paragraph",
          "value": "Decrease in net open ticket count is considered as positive"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "maxValue": "",
    "kpiUnit": "Number",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi51",
    "kpiName": "Net Open Ticket Count By RCA",
    "isDeleted": "False",
    "defaultOrder": 4,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "NET OPEN TICKET COUNT BY RCA measures the overall open tickets during a defined period grouped by RCA. It considers the gross open and closed count during a period and then plots the net count",
      "details": [
        {
          "type": "paragraph",
          "value": "Decrease in net open ticket count is considered as positive"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "maxValue": "",
    "kpiUnit": "Number",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi48",
    "kpiName": "Net Open Ticket By Status",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Net open ticket count by status measures the overall open tickets during a defined period grouped by Status. It considers the gross open and closed count during a period and then plots the net count",
      "details": [
        {
          "type": "paragraph",
          "value": "Decrease in net open ticket count is considered as positive"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "isTrendCalculative": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi997",
    "kpiName": "Open Ticket Ageing By Priority",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "OPEN TICKETS AGEING BY PRIORITY groups all the open tickets based on their ageing and priority"
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "multiSelectDropDown",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi63",
    "kpiName": "Regression Automation Coverage",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "REGRESSION AUTOMATION COVERAGE measures progress of automation of regression test cases",
      "formula": [
        {
          "lhs": "Regression Automation Coverage ",
          "operator": "division",
          "operands": [
            "No. of regression test cases automated",
            "Total no. of regression test cases"
          ]
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of regression automation coverage between last 2 sprints. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">= 80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 20%"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi62",
    "kpiName": "Unit Test Coverage",
    "maxValue": "100",
    "kpiUnit": "%",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "UNIT TEST COVERAGE is a measurement of the amount of code that is run by unit tests - either lines, branches, or methods.",
      "formula": [
        {
          "lhs": "The calculation is done directly in Sonarqube"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Unit test coverage between last 2 weeks. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60%-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "<20%"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "isPositiveTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "showTrend": true,
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi64",
    "kpiName": "Sonar Violations",
    "maxValue": "",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "SONAR VIOLATIONS highlight count of issues that exist every time a piece of code breaks a coding rule. The set of coding rules is defined through the associated Quality profile for each programming language in the project.",
      "formula": [
        {
          "lhs": "The calculation is done directly in Sonarqube."
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "Issues are categorized in 3 types: Bug, Vulnerability and Code Smells."
        },
        {
          "type": "paragraph",
          "value": "All these issues are categorized into 5 type of severity: Blocker, Critical, Major, Minor, Info."
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "multiSelectDropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi67",
    "kpiName": "Sonar Tech Debt",
    "maxValue": "90",
    "kpiUnit": "Days",
    "isDeleted": "False",
    "defaultOrder": 10,
    "kpiSource": "Sonar",
    "groupId": 1,
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "SONAR TECH DEBT explains the estimated time required to fix all maintainability Issues/ code smells",
      "formula": [
        {
          "lhs": "It is calculated as effort to fix all Code Smells. The effort is calculated in minutes and converted to days by assuming 1 Day=8 Hours."
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Days’. Lower the days, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Sonar Tech Debt between last 2 weeks. A downward trend is considered positive."
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<10 days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "10-30 days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "30-50 days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "50-100 days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">100 days"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-100","100-50","50-30","30-10","10-"]
  },
  {
    "kpiId": "kpi71",
    "kpiName": "Test Execution and pass percentage",
    "isDeleted": "False",
    "defaultOrder": 11,
    "kpiSource": "Zypher",
    "groupId": 1,
    "maxValue": "100",
    "kpiUnit": "%",
    "kanban": true,
    "chartType": "grouped_column_plus_line",
    "kpiInfo": {
      "definition": "TEST EXECUTION AND PASS PERCENTAGE measures the percentage of test cases that have been executed & the percentage that have passed.",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as "
        },
        {
          "type": "paragraph",
          "value": "No. of executed test cases out of total test cases in the latest execution during a day/week/month"
        },
        {
          "type": "paragraph",
          "value": "No. of pass test cases out of executed test cases in  the latest execution during a day/week/month"
        },
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Percentage’. Higher the percentage, better is the ‘Quality’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Test Execution and pass percentage between last 2 days/weeks/months. An upward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "> 80%"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "60-80%"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "40-60%"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-40%"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "< 20%"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Percentage",
    "lineLegend": "Passed",
    "barLegend": "Executed",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "maturityRange": ["-20","20-40","40-60","60-80","80-"]
  },
  {
    "kpiId": "kpi49",
    "kpiName": "Ticket Velocity",
    "isDeleted": "False",
    "defaultOrder": 12,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "maxValue": "300",
    "kpiUnit": "SP",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Ticket velocity measures the size of tickets (in story points) completed in a defined duration"
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Story Points",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": true,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi58",
    "kpiName": "Team Capacity",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 13,
    "kpiSource": "Jira",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Team Capacity is sum of capacity of all team member measured in hours during a defined period."
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Hours",
    "isPositiveTrend": true,
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi66",
    "kpiName": "Code Build Time",
    "maxValue": "100",
    "kpiUnit": "min",
    "isDeleted": "False",
    "defaultOrder": 14,
    "kpiSource": "Jenkins",
    "groupId": 1,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "CODE BUILD TIME measures the time a job takes to build the code.",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in  ‘Mins’. Lesser the time better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Code Build Time between last 2 weeks. A downward trend is considered positive"
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "<5 mins"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "5-15 mins"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "15-30 mins"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": " 30-45 mins"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": ">45 mins"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Min",
    "isPositiveTrend": false,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-45","45-30","30-15","15-5","5-"]
  },
  {
    "kpiId": "kpi65",
    "kpiName": "Number of Check-ins",
    "maxValue": "10",
    "kpiUnit": "check-ins",
    "isDeleted": "False",
    "defaultOrder": 15,
    "groupId": 1,
    "kpiSource": "BitBucket",
    "thresholdValue": "55",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "NUMBER OF CHECK-INS helps in measuring the transparency as well the how well the tasks have been broken down.",
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a Count. Higher the count better is the ‘Speed’"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Number of Check-ins & Merge requests between last 2 days. An upward trend is considered positive."
        },
        {
          "type": "paragraph",
          "value": "Maturity of the KPI is calculated based on the latest value"
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": ">16"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "8-16"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "4-8"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "2-4"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "0-2"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "kpiFilter": "dropDown",
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
    "hideOverallFilter" : true,
    "maturityRange": ["-2","2-4","4-8","8-16","16-"]
  },
  {
    "kpiId": "kpi53",
    "kpiName": "Lead Time",
    "isDeleted": "False",
    "kpiInAggregatedFeed": "True",
    "kpiOnDashboard": [
      "Aggregated"
    ],
    "kpiBaseLine": "0",
    "thresholdValue": "",
    "defaultOrder": 16,
    "kpiUnit": "Days",
    "kpiSource": "Jira",
    "groupId": 3,
    "kanban": true,
    "chartType": "table",
    "kpiInfo": {
      "definition": "LEAD TIME is the time from the moment when the request was made by a client and placed on a board to when all work on this item is completed and the request was delivered to the client.",
      "formula": [
        {
          "lhs": "It is calculated as the sum following"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "Open to Triage: Time taken from ticket creation to it being refined & prioritized for development."
        },
        {
          "type": "paragraph",
          "value": "Triage to Complete: Time taken from start of work on a ticket to it being completed by team."
        },
        {
          "type": "paragraph",
          "value": "Complete to Live: Time taken between ticket completion to it going live."
        },
        {
          "type": "paragraph",
          "value": "Each of the KPIs are calculated in 'Days."
        }
      ],
      "maturityLevels": [
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 2 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "2-5 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "5-15 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "15-30 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 30 Days"
        },
        {
          "level": "Complete to Live"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 3 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "3-7 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "7-10 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "10-20 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 20 Days"
        },
        {
          "level": "Triage to Complete"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 5 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "5-10 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "10-20 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "20-30 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 30 Days"
        },
        {
          "level": "Open to Triage"
        },
        {
          "level": "M5",
          "bgColor": "#6cab61",
          "range": "< 10 Days"
        },
        {
          "level": "M4",
          "bgColor": "#AEDB76",
          "range": "10-30 Days"
        },
        {
          "level": "M3",
          "bgColor": "#eff173",
          "range": "30-45 Days"
        },
        {
          "level": "M2",
          "bgColor": "#ffc35b",
          "range": "45-60 Days"
        },
        {
          "level": "M1",
          "bgColor": "#F06667",
          "range": "> 60 Days"
        },
        {
          "level": "Lead Time"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "kpiFilter": "radioButton",
    "aggregationCriteria": "average",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": true,
     "maturityRange": ["-60","60-45","45-30","30-10","10-"],

     	"maturityLevel": [{
     			"level": "LeadTime",
     			"range": ["-60", "60-45", "45-30", "30-10", "10-"]
     		},
     		{
     			"level": "Open-Triage",
     			"range": ["-30", "30-20", "20-10", "10-5", "5-"]
     		},
     		{
     			"level": "Triage-Complete",
     			"range": ["-20", "20-10", "10-7", "7-3", "3-"]
     		},
     		{
     			"level": "Complete-Live",
     			"range": ["-30", "30-15", "15-5", "5-2", "2-"]
     		}
     	]
  },
  {
    "kpiId": "kpi74",
    "kpiName": "Release Frequency",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 17,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Release Frequency highlights the number of releases done in a month",
      "formula": [
        {
          "lhs": "Release Frequency for a month",
          "rhs": "Number of fix versions in JIRA for a project that have a release date falling in a particular month"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated as a ‘Count’. Higher the Release Frequency, more valuable it is for the Business or a Project"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of Release Frequency between last 2 months. An upward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi114",
    "kpiName": "Value delivered (Cost of Delay)",
    "maxValue": "300",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 18,
    "kpiSource": "Jira",
    "groupId": 4,
    "thresholdValue": "",
    "kanban": true,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Cost of delay (CoD) is a prioritization framework that helps a business quantify the economic value of completing a project sooner as opposed to later.",
      "formula": [
        {
          "lhs": "COD for a Epic or a Feature",
          "rhs": "User-Business Value + Time Criticality + Risk Reduction and/or Opportunity Enablement."
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "It is calculated in ‘Days’. Higher the CoD for a feature or an Epic, more valuable it is for the Business or a Project"
        },
        {
          "type": "paragraph",
          "value": "A progress indicator shows trend of CoD between last 2 months. An upward trend is considered positive"
        }
      ]
    },
    "xAxisLabel": "Weeks",
    "yAxisLabel": "Days",
    "isPositiveTrend": true,
    "showTrend": true,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi121",
    "kpiName": "Capacity",
    "maxValue": "",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Planned capacity is the development team's available time."
        },
        {
          "type": "paragraph",
          "value": "Source of this is KnowHOW"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "boxType": "1_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi119",
    "kpiName": "Work Remaining",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Work Remaining KPI gives a depiction of the pending work in an iteration from three available dimensions"
        },
        {
          "type": "paragraph",
          "value": "Issue count - Total no. of issues that are not completed based on DOD in the iteration."
        },
        {
          "type": "paragraph",
          "value": "Story Points - Sum of story points of all issues not completed based on DOD in the iteration"
        },
        {
          "type": "paragraph",
          "value": "Hours - Sum of remaining hours as mentioned in Jira of all issues not completed based on DOD in the iteration"
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi75",
    "kpiName": "Estimate vs Actual",
    "maxValue": "",
    "kpiUnit": "Hours",
    "isDeleted": "False",
    "defaultOrder": 5,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Estimate vs Actual gives a comparative view of the sum of estimated hours of all issues in an iteration as against the total time spent on these issues"
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira fields - Original Estimate and Logged Work"
        },
        {
          "type": "paragraph",
          "value": "To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column",
    "calculateMaturity": true
  },
  {
    "kpiId": "kpi123",
    "kpiName": "Issues likely to Spill",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 6,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Issues likely to spill gives intelligence to the team about number of issues that could potential not get completed. It also represents the corresponding size of work likely to spill."
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "3_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi122",
    "kpiName": "Closure Possible Today",
    "maxValue": "",
    "kpiUnit": "Story Point",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Closures possible today gives intelligence to users about how many issues can be completed on a particular day of iteration."
        },
        {
          "type": "paragraph",
          "value": "An issues is included as a possible closure based on 2 criteria"
        },
        {
          "type": "paragraph",
          "value": "1. If the remaining hours of an issues is less than 8 hrs OR"
        },
        {
          "type": "paragraph",
          "value": "2. If an issue is in Testing status (as defined in KnowHOW)"
        },
        {
          "type": "paragraph",
          "value": "Source of KPI is Jira and KnowHOW"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi120",
    "kpiName": "Scope Change",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 7,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Scope change KPI highlights change in iteration scope since the start of iteration."
        },
        {
          "type": "paragraph",
          "value": "It showcases added as well as removed issue count and the corresponding story points"
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column_big",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi124",
    "kpiName": "Estimation Hygiene",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 8,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": null,
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Estimation Hygiene acts as an indicator to identify issues which are either not estimated and the issues which do not have logged work."
        },
        {
          "type": "paragraph",
          "value": "*This is just to measure the hygiene of Jira usage by a team"
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "multiSelectDropDown",
    "boxType": "2_column_big",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi125",
    "kpiName": "Daily Closures",
    "maxValue": "",
    "kpiUnit": "Count",
    "isDeleted": "False",
    "defaultOrder": 9,
    "kpiCategory": "Iteration",
    "kpiSource": "Jira",
    "groupId": 8,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "stackedColumn",
    "kpiInfo": {
      "details": [
        {
          "type": "paragraph",
          "value": "Daily Closures KPI gives a graphical representation of no. of issues closed on a daily basis."
        },
        {
          "type": "paragraph",
          "value": "This KPI is very useful for retrospective meetings and it clearly illustrates how the iteration was when looked in combination to other iteration board KPIs"
        },
        {
          "type": "paragraph",
          "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
        }
      ]
    },
    "xAxisLabel": "Days",
    "yAxisLabel": "Count",
    "isPositiveTrend": true,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "kpiFilter": "",
    "boxType": "chart",
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi79",
    "kpiName": "Test Cases Without Story Link",
    "maxValue": "5000",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Backlog",
    "kpiSource": "Zypher",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "progress-bar",
    "kpiInfo": {
      "formula": [
        {
          "lhs": "Testcases without story link",
          "rhs": "# of total non-regression test cases without story link"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "This KPI works only at project level. Graph shows data of the project with the latest sprint from the selected filters."
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi80",
    "kpiName": "Defects Without Story Link",
    "maxValue": "500",
    "kpiUnit": "",
    "isDeleted": "False",
    "defaultOrder": 2,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "progress-bar",
    "kpiInfo": {
      "formula": [
        {
          "lhs": "Defect Count Without Story Link",
          "rhs": "# of total defects without Story link"
        }
      ],
      "details": [
        {
          "type": "paragraph",
          "value": "This Kpi works only on Project level."
        }
      ]
    },
    "xAxisLabel": "",
    "yAxisLabel": "",
    "isPositiveTrend": false,
    "showTrend": false,
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false,
    "kpiFilter": "multiSelectDropDown"
  },
  {
    "kpiId": "kpi127",
    "kpiName": "Production Defects Ageing",
    "kpiUnit": "Number",
    "isDeleted": "False",
    "defaultOrder": 3,
    "kpiCategory": "Backlog",
    "kpiSource": "Jira",
    "groupId": 2,
    "thresholdValue": "",
    "kanban": false,
    "chartType": "line",
    "kpiInfo": {
      "definition": "Production Defects ageing KPI groups all the production defects which are not closed from those created in last 15 months in standard buckets that are  <1 Month, 1-3 Month, 3-6 Month, 6-12 Month, > 12 Month. These defects can be filtered based on 'Priority'. The KPI measures the effectiveness of Defect Backlog prioritization and is useful for Product Owner, Scrum Master and the development team"
    },
    "xAxisLabel": "Months",
    "yAxisLabel": "Count",
    "isPositiveTrend": false,
    "kpiFilter": "multiSelectDropDown",
    "showTrend": false,
    "aggregationCriteria": "sum",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false
  },
  {
    "kpiId": "kpi989",
    "kpiName": "Kpi Maturity",
    "isDeleted": "False",
    "defaultOrder": 1,
    "kpiCategory": "Kpi Maturity",
    "isAdditionalFilterSupport": false,
    "calculateMaturity": false,
    "kanban": false
  },
  {
      "kpiId": "kpi128",
      "kpiName": "Work Completed",
      "maxValue": "",
      "kpiUnit": "Hours",
      "isDeleted": "False",
      "defaultOrder": 4,
      "kpiCategory": "Iteration",
      "kpiSource": "Jira",
      "groupId": 8,
      "thresholdValue": "",
      "kanban": false,
      "chartType": null,
      "kpiInfo": {
        "details": [
          {
            "type": "paragraph",
            "value": "Work Completed KPI gives a depiction of the work completed in an iteration from two available dimensions"
          },
          {
            "type": "paragraph",
            "value": "Issue count - Total no. of issues that are completed based on DOD in the iteration."
          },
          {
            "type": "paragraph",
            "value": "Story Points - Sum of story points of all issues completed based on DOD in the iteration"
          },
          {
            "type": "paragraph",
            "value": "Source of this KPI is Jira. To see the latest data, run the Jira processor from KnowHOW settings"
          }
        ]
      },
      "xAxisLabel": "",
      "yAxisLabel": "",
      "isPositiveTrend": true,
      "showTrend": false,
      "isSquadSupport": false,
      "kpiFilter": "multiSelectDropDown",
      "boxType": "3_column",
      "calculateMaturity": false
    }
]);