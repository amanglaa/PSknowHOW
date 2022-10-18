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
var kpi126 = {
           "kpiId" : "kpi126",
           "kpiName" : "Created vs Resolved defects",
           "isEnabled" : true,
           "isShown"  : true,
           "order" : NumberInt(9)
          }

// sample data for add and remove any kpi , you have to write this way
// parameter should be first boardName , boardId , kpiId , KpiOrder , Operation
//addNewOrUpdateKPIInAnyBoard(boardScrum, 5, "kpi76", 1,  "kpi76" , operationDELETE);
//addNewOrUpdateKPIInAnyBoard(boardScrum, 1, "kpi126", 9, kpi126 , operationADD);
//addNewOrUpdateKPIInAnyBoard(boardScrum, 3, "kpi126", 9, kpi126 , operationADD);