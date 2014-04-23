(function(u, s){
    var restfulCallDataSource = function(datasource, anchor,container, model){
        return function(){
            s.ajax({
                success: function(response){
                    container.success(response, anchor, model);
                },
                error: function(){
                    container.failure(anchor);
                },
                type: datasource.type(),
                url: datasource.restUrl()
            });
        };
    };
    var restfulDataWrite = function(datasource, success, failure){
        return function(){
            s.ajax({
                success: function(response){
                    success(response);
                },
                error: function(){
                    failure();
                },
                type: datasource.type(),
                url: datasource.restUrl()
            });
        };
    };
    var executor = function(datasource, anchor, container, model){
        var loadData = restfulCallDataSource(datasource, anchor, container, model);
        container["loadData"] = loadData;
        console.log("loading data I think");
        container.loadData();
    };
    var datasourceMaker = function(url, type){
        return {
            "restUrl" : function(){
                return url;
            },
            "type" : function(){
                return type;
            }
        };
    };
    var modelMaker = function(render, addClickListeners){
        return {
            "render" : function(data, anchor, reload){
                anchor.append(render(data));
                addClickListeners(data, reload);
            }
        };
    };
    var companyDetailsContainer = {
        "success" : function(response, anchor, model){
            var loadData = this.loadData;
            anchor.html("");
            u.forEach(response, function(r){
                model.render(r, anchor, loadData);
            });
        },
        "failure" : function(anchor){
            console.log("failure");
            anchor.html = "";
            anchor.append("failed to load company details...");
        }
    };
    var companyDetailsModel = modelMaker(
        function(data){
            console.log(data);
            var result = "</br>";
            result = result + "<span class='company_details'>";
            result = result + "<span class='company_details_title'>"+data.title+"</span>";
            result = result + "</br>";
            result = result + "<span class='company_details_content'>"+data.content+"</span>";
            result = result + "</br>";
            result = result + "<button id='company_detail_"+data._id+"'>DELETE</button>"
            result = result + "</span>";
            result = result + "</br>";
            return result;
        },
        function(data, reload){
            var deleteDetailDatasource = datasourceMaker("/job-details/"+data._id, "DELETE");
            var success = function(data){
                console.log(data);
                reload();
            };
            var failure = function(){
                console.log("failed to delete company detail: "+data._id);
            };
            var deleteDetail = restfulDataWrite(deleteDetailDatasource, success, failure);
            s("#company_detail_"+data._id).click(deleteDetail);
        }
    );
    var companyContainer = {
        "success" : function(response, anchor, model){
            var loadData = this.loadData;
            var button_id = "button_id";
            anchor.html("");
            u.forEach(response, function(r){
                model.render(r, anchor, loadData);
            });
            anchor.append("</br>");
            var reloadCompanies = "reloadCompanies";
            anchor.append("<button id='"+reloadCompanies+"'>RELOAD</button>");
            s("#"+reloadCompanies).click(function(){
                loadData();
            });
        },
        "failure" : function(anchor){
            console.log("failure");
            anchor.html = "";
            anchor.append("failed to load company data...");
        }
    };
    var companyModel = modelMaker(function(data){
        console.log(data);
        var result = "<span class='company_name'>"+data.name+"</span>";
        result = result + "</br>";
        result = result + "<span class='position_name'>"+data.position+"</span>";
        result = result + "</br>";
        result = result + "<a href=''>position link</a>";
        result = result + "</br>";
        result = result + "<button id='show_button_"+data._id+"'>Show Details</button>";
        result = result + "<button id='hide_button_"+data._id+"'>Hide Details</button>";
        result = result + "<button id='add_detail_"+data._id+"'>Add Detail</button>";
        result = result + "<button id='edit_company_"+data._id+"'>Edit Company</button>";
        result = result + "</br>";
        result = result + "<div id='details_"+data._id+"'/>";
        return result;
    }, function(data, reload){
        var showButton = s("#show_button_"+data._id);
        var hideButton = s("#hide_button_"+data._id);
        var detailsDiv = s("#details_"+data._id);
        hideButton.hide();
        showButton.click(function(){
            showButton.hide();
            hideButton.show();
            if(detailsDiv.html()==""){
                var companyDetailsDatasource = datasourceMaker("/job-details/"+data._id,"GET");
                executor(companyDetailsDatasource, detailsDiv, companyDetailsContainer, companyDetailsModel);
            }else{
                detailsDiv.show();
            }
        });
        hideButton.click(function(){
            hideButton.hide();
            showButton.show();
            detailsDiv.hide();
        });
    });
    s(document).ready(function(){
        console.log("should be doing something....");
        var companyDatasource = datasourceMaker("/jobs", "GET");
        var companyAnchor = s("#data");
        executor(companyDatasource, companyAnchor, companyContainer, companyModel);
    });
/*
    var restfulCallNoParams = function(restUrl, successFn, failureFn, type){
        return function(){
            s.ajax({
                success: function(response){
                    successFn(response);
                },
                error: function(){
                    failureFn();
                },
                type: type,
                url: restUrl
            });
        };
    };
    var getCompanyList = function(model){
        s.ajax({
            contentType: 'application/json',
            success: function(response){
                console.log(response);
                u.forEach(response, function(r){
                    var rObj = model(r);
                    rObj.render();
                });
            },
            error: function(){
                console.log("Device control failed");
            },
            type: 'GET',
            url: '/jobs'
        });
    };
    var getCompanyDetails = function(id, model){
        s.ajax({
            contentType: 'application/json',
            data: {},
            dataType: 'json',
            success: function(response){
                u.forEach(response, function(r){
                    var rObj = model(r);
                    rObj.render();
                });
            },
            error: function(){
                console.log("Failed to retrieve company details for company id "+id);
            },
            processData: false,
            type: 'GET',
            url: '/job-details/'+id
        });
    };
    var companyDetailsModel = function(anchor){
        return function(companyDetail){
            return {
                "render" : function(){
                    _.forEach(Object.keys(companyDetail), function(key){
                        anchor.append("</br><span>"+key+":"+companyDetail[key]+"</span");
                        console.debug(companyDetail[key]);
                    });
                    anchor.append("</br>");
                    anchor.append("<button id='"+this.deleteId()+"'>DELETE</button>");
                    anchor.append("</br>");
                    this.addClickListeners();
                },
                "addClickListeners" : function(){
                    var button = s("#"+this.deleteId());
                    var uri = "/job-details/"+this.id();
                    button.click(restfulCallNoParams(
                        uri,
                        function(result){
                            console.log(result);
                            parent.renderChild();
                        }, function(){
                            console.log("call failed");
                        },"DELETE"));
                },
                "id" : function(){
                    return companyDetail._id;
                },
                "deleteId" : function(){
                    return "delete_"+this.id();
                }
            };
        };
    };
    var companyObj = function(anchor){
        return function(company){
            return {
                "name" : function(){
                    return company.name;
                },
                "id" : function(){
                    return company._id;
                },
                "render" : function(){
                    var result = "</br>";
                    result = result + "<span id='"+this.spanClass()+"'>";
                    result = result + this.name();
                    result = result + "</br>";
                    result = result + this.id();
                    result = result + "</br>";
                    result = result + "<button id='"+this.detailsButtonId()+"'>show details</button>";
                    result = result + "<div id='"+this.detailsAnchorId()+"'/>";
                    result = result + "</span>";
                    anchor.append(result);
                    this.attachClickListeners();
                },
                "detailsButtonId" : function(){
                    return "details_button_"+this.id();
                },
                "detailsAnchorId" : function(){
                    return "details_anchor_"+this.id();
                },
                "spanClass" : function(){
                    return this.id();
                },
                "attachClickListeners" : function(){
                    console.log("attaching click listeners");
                    var button = s("#"+this.detailsButtonId());
                    button.click(this.renderChild);
                },
                "renderChild" : function(){
                    var anchor = s("#"+this.detailsAnchorId());
                    var model = companyDetailsModel(anchor);
                    var id = this.id();
                    anchor.html("");
                    getCompanyDetails(id, model);
                }
            };
        };
    };

    s(document).ready(function(){
        var anchor = s("#data");
        getCompanyList(companyObj(anchor));
    });
*/
})(_,$);
