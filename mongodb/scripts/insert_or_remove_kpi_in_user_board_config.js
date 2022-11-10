function loopThroughBoardCheckKpiExist(board, kpiId) {
    var absent = true;
    board["kpis"].forEach(kpis => {
        if (kpis["kpiId"] == kpiId) {
            absent = false;
        }
    });
    return absent;
}

// as per boardId and order apply as per operation and maintain order also
function addNewOrUpdateKPIInAnyBoard(boardType , boardId, kpiId, order, kpiObj, operation){

if(operation == "ADD"){
    print("add loop start for kpi :" , kpiId);
    db.getCollection('user_board_config').find({}).forEach(
        userBoardConfig => {
            userBoardConfig[boardType].forEach(board => {
                var flag = true;
                if (board["boardId"] == boardId) {
                    flag = loopThroughBoardCheckKpiExist(board, kpiId);
                    if (flag) {
                        print(userBoardConfig['_id'] , "user have not this kpi exist");
                        db.user_board_config.update({
                            "_id": userBoardConfig['_id'],
                            [boardType] : {
                                "$elemMatch": {
                                    "boardId": boardId
                                }
                            }
                        }, {
                            $inc: {
                                [boardType.concat(".$[board].kpis.$[degree].order")] : 1

                            }
                        }, {
                            arrayFilters: [{
                                "degree.order": {
                                    $gt: order - 1
                                }
                            }, {
                                "board.boardId": boardId
                            }]
                        });
                            print(kpiId ,"is added started");
                        db.user_board_config.update({
                            "_id": userBoardConfig['_id'],
                            [boardType] : {
                                "$elemMatch": {
                                    "boardId": boardId
                                }
                            }
                        }, {
                            $push: {
                                [boardType.concat(".$.kpis")]: {
                                    $each: [kpiObj],
                                    $position: order-1
                                }
                            }

                        });
                    }
                    print(kpiId ,"kpi pushed successfully");
                }

    });
});
} else {
    print("delete kpi loop start for kpi :" , kpiId);
    db.getCollection('user_board_config').find({}).forEach(
        userBoardConfig => {
            userBoardConfig[boardType].forEach(board => {
                var flag = true;
                if (board["boardId"] == boardId) {
                    flag = loopThroughBoardCheckKpiExist(board, kpiId);
                    print(!flag);
                    if (!flag) {
                        db.user_board_config.update({
                            "_id": userBoardConfig['_id'],
                            [boardType] : {
                                "$elemMatch": {
                                    "boardId": boardId
                                }
                            }
                        }, {
                            $inc: {
                                [boardType.concat(".$[board].kpis.$[degree].order")] : -1
                            }
                        }, {
                            arrayFilters: [{
                                "degree.order": {
                                    $gt: order
                                }
                            }, {
                                "board.boardId": boardId
                            }]
                        });
                              print(kpiId ,"is deleted started");

                        db.user_board_config.update({
                            "_id": userBoardConfig['_id'],
                            [boardType] : {
                                "$elemMatch": {
                                    "boardId": boardId
                                }
                            }
                        }, {
                           $pull: {
                               [boardType.concat(".$.kpis")]: {
                                    "kpiId" : kpiId
                              }
                            }
                        });
                    }
                     print(kpiId ,"kpi deleted successfully");
                }

    });
});
}
}

var boardScrum = "scrum";
var boardKanban = "kanban";
var boardOthers = "others";
var operationADD = "ADD";
var operationDELETE = "DELETE";

// define var for added new kpi as per order
var kpi121 = {
			"kpiId" : "kpi121",
			"kpiName" : "Capacity",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 1
			}
var kpi119 = {
			"kpiId" : "kpi119",
			"kpiName" : "Work Remaining",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 2
		    }
var kpi128 = {
           "kpiId" : "kpi128",
           "kpiName" : "Work Completed",
           "isEnabled" : true,
           "isShown"  : true,
           "order" : 3
           }
var kpi75 = {
			"kpiId" : "kpi75",
			"kpiName" : "Estimate vs Actual",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 4
			}
var kpi123 = {
			"kpiId" : "kpi123",
			"kpiName" : "Issues likely to Spill",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 5
			}
var kpi122 = {
			"kpiId" : "kpi122",
			"kpiName" : "Closure Possible Today",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 6
			}
var kpi120 = {
			"kpiId" : "kpi120",
			"kpiName" : "Scope Change",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 7
			}
var kpi124 = {
			"kpiId" : "kpi124",
			"kpiName" : "Estimation Hygiene",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 8
			}
var kpi125 = {
			"kpiId" : "kpi125",
			"kpiName" : "Daily Closures",
			"isEnabled" : true,
			"isShown" : true,
			"order" : 9
			}

// sample data for add and remove any kpi , you have to write this way
// parameter should be first boardName , boardId , kpiId , KpiOrder , Operation
//addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi76", 1,  "kpi76" , operationDELETE);
//addNewOrUpdateKPIInAnyBoard(boardScrum, 1, "kpi126", 9, kpi126 , operationADD);
//addNewOrUpdateKPIInAnyBoard(boardScrum, 3, "kpi126", 9, kpi126 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi121", 2, kpi121 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi119", 3, kpi119 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi75", 4, kpi75 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi123", 5, kpi123 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi122", 6, kpi122 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi120", 7, kpi120 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi124", 8, kpi124 , operationDELETE);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi125", 9, kpi125 , operationDELETE);

addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi121", 1, kpi121 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi119", 2, kpi119 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi128", 3, kpi128 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi75", 4, kpi75 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi123", 5, kpi123 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi122", 6, kpi122 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi120", 7, kpi120 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi124", 8, kpi124 , operationADD);
addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi125", 9, kpi125 , operationADD);
